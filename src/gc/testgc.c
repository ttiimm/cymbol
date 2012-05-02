#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "gc.h"

#define ASSERT(EXPECTED, RESULT)\
  if(EXPECTED == RESULT){ printf("."); } else { printf("\n%-30s failure on line %d\n", __func__, __LINE__); }

#define ASSERT_TRUE(RESULT)\
  if(1 == RESULT){ printf("."); } else { printf("\n%-30s failure on line %d\n", __func__, __LINE__); }

#define ASSERT_STR(EXPECTED, RESULT)\
   if(0 == strcmp(EXPECTED, RESULT)) { printf("."); } else { printf("\n%-30s failure on line %d\n", __func__, __LINE__); }

#define ASSERT_NE(EXPECTED, RESULT)\
  if(EXPECTED != RESULT){ printf("."); } else { printf("\n%-30s failure on line %d\n", __func__, __LINE__); }


void test_alloc_user() 
{
  byte *before, *after;
  User *u;
  before = heap_address();

  u = (User *) alloc(&User_type);
  after = heap_address();

  ASSERT(align(User_type.size), (after - before));
  ASSERT_TRUE(in_heap((Object *) u));
}

void test_alloc_string()
{
  int str_length;
  byte *before, *after;
  String *s;
  before = heap_address();
  s = alloc_String(12);
  after = heap_address();
  strcpy(s->elements, "abcdefghijkl");
  str_length = sizeof_array(12, ARRAY_CHAR);

  ASSERT(align(str_length), (after - before));
  ASSERT(12, s->length);
  ASSERT_TRUE(in_heap((Object *) s));
  ASSERT_STR("abcdefghijkl", s->elements);
  ASSERT_TRUE(in_heap((Object *) s->elements));
}

void test_root_management()
{
  Object *a, *b, *c;
  ASSERT(0, _rp);
  GC_SAVE_RP;

  ADD_ROOT(a);
  ADD_ROOT(b);
  ADD_ROOT(c);

  ASSERT(3, _rp);

  GC_RESTORE_RP;
  ASSERT(0, _rp);
}

void test_heap_dump()
{
  char *expected, *result;
  User *u;
  String *s;
  Array *a;
  GC_SAVE_RP;

  gc();

  s = new_String("tim");
  u = new_User(103, s);
  a = (Array *) alloc_Array(2, ARRAY_POINTER);
  ((void **) a->elements)[0] = s;
  ((void **) a->elements)[1] = u;

  ADD_ROOT(s);
  ADD_ROOT(u);
  ADD_ROOT(a);

  expected = "heap2[116,1000]\n"
             "0000:String[32+4]=\"tim\"\n"
             "0036:User[32]->[0]\n"
             "0068:Array[32+2]->[0, 36]\n";

  result = calloc(1000, sizeof(char));
  heap_dump(result);

//  printf("\n%s", result);
  ASSERT_STR(expected, result);

  GC_RESTORE_RP;
  free(result);
}

void test_gc_string()
{
  char *expected, *result;
  void *old_a, *old_b;
  String *a, *b;
  int a_len;
  GC_SAVE_RP;
  gc();

  a = new_String("abcd");
  old_a = &*a; 

  b = alloc_String(5);
  old_b = &*b;

  ADD_ROOT(a);

  a_len = sizeof_array(4, ARRAY_CHAR);

  ASSERT_NE(a_len, heap_allocated());

  gc();

  expected = "heap2[40,1000]\n"
             "0000:String[32+5]=\"abcd\"\n";
  result = calloc(1000, sizeof(char));
  heap_dump(result);
  ASSERT_STR(expected, result);
//  printf("%s\n", result);
  free(result);

  ASSERT(align(a_len), heap_allocated());
  ASSERT_NE(old_a, &*a);
  ASSERT(old_b, &*b);
  ASSERT_STR("Array", a->type->name);
  ASSERT(4, a->length);
  ASSERT_STR("abcd", a->elements);
  GC_RESTORE_RP;
}

void test_gc_user()
{
  char *expected, *result;
  void *old_a;
  User *a; 
  String *b;
  int userlen, strlen;

  GC_SAVE_RP;

  b = new_String("tim");
  a = new_User(103, b);
  old_a = &*a;

  ADD_ROOT(a);
  ADD_ROOT(b);

  ASSERT_NE(User_type.size, heap_allocated());

  gc();

  expected = "heap1[68,1000]\n"
             "0000:User[32]->[32]\n"
             "0032:String[32+4]=\"tim\"\n";
  result = calloc(1000, sizeof(char));
  heap_dump(result);
  ASSERT_STR(expected, result);
//  printf("%s\n", result);
  free(result);

  userlen = align(User_type.size);
  strlen = String_type.size + 3 + 1;
 
  ASSERT(align(userlen + strlen), heap_allocated());
  ASSERT_NE(old_a, &*a);
  ASSERT(103, a->id);
  ASSERT(&User_type, a->type);
  ASSERT_STR("tim", a->name->elements);
  ASSERT(NULL, a->forward);
  GC_RESTORE_RP;
}

void test_gc_walks_root_pointers()
{
  char *expected, *result;
  void *old_a;
  User *a; 
  String *b;
  int userlen, strlen;

  GC_SAVE_RP;

  b = new_String("tim");
  a = new_User(103, b);
  old_a = &*a;
 
  ADD_ROOT(a);

  ASSERT_NE(User_type.size, heap_allocated());

  gc();

  expected = "heap2[68,1000]\n"
             "0000:User[32]->[32]\n"
             "0032:String[32+4]=\"tim\"\n";
  result = calloc(1000, sizeof(char));
  heap_dump(result);
  ASSERT_STR(expected, result);
//  printf("%s\n", result);
  free(result);

  userlen = align(User_type.size);
  strlen = String_type.size + 3 + 1;

  ASSERT(align(userlen + strlen), heap_allocated());
  ASSERT_NE(old_a, &*a);
  ASSERT(103, a->id);
  ASSERT(&User_type, a->type);
  ASSERT_STR("tim", a->name->elements);
  GC_RESTORE_RP;
}

void test_gc_with_pointer_array()
{
  User *u, *t;
  String *s;
  Array *a;
  char *expected, *result_before, *result_after;

  gc();
  GC_SAVE_RP;
  s = new_String("tim");
  u = new_User(102, s);
  a = new_Array(2, ARRAY_POINTER);
  add_to(a, 0, s);
  add_to(a, 1, u);

  ADD_ROOT(a);

  expected = "heap1[116,1000]\n"
             "0068:Array[32+2]->[0, 36]\n"
             "0000:String[32+4]=\"tim\"\n"
             "0036:User[32]->[0]\n";
  result_before = calloc(1000, sizeof(char));
  heap_dump(result_before);
  ASSERT_STR(expected, result_before);
  free(result_before);

  gc();

  t = new_User(102, NULL);
  ADD_ROOT(t);

  expected = "heap2[148,1000]\n"
             "0000:Array[32+2]->[48, 84]\n"
             "0048:String[32+4]=\"tim\"\n"
             "0084:User[32]->[48]\n"
             "0116:User[32]->[-1]\n";
  result_after = calloc(1000, sizeof(char));
  heap_dump(result_after);
  ASSERT_STR(expected, result_after);
//  printf("\n%s", result_after);
  free(result_after);

  GC_RESTORE_RP;
}

void test_gc_with_cycle()
{
  Node *a, *b;
  char *result, *expected;

  GC_SAVE_RP;
  gc();

  a = new_Node("a", NULL);
  b = new_Node("b", a);
  a->neighbor = b;

  ADD_ROOT(a);
  ADD_ROOT(b);

  ASSERT(align(2 * Node_type.size), heap_allocated());

  gc();

  expected = "heap2[64,1000]\n"
             "0000:Node[32]->[32]\n"
             "0032:Node[32]->[0]\n";
  result = calloc(1000, sizeof(char));
  heap_dump(result);
  ASSERT_STR(expected, result);
//  printf("%s\n", result);
  free(result);

  ASSERT(align(2 * Node_type.size), heap_allocated());
  ASSERT_STR("a", b->neighbor->payload);
  ASSERT_STR("b", a->neighbor->payload);
  GC_RESTORE_RP;
}

void test_big_loop_doesnt_run_out_of_memory() {
   GC_SAVE_RP;

   User *tombu;
   ADD_ROOT(tombu);

   int i = 0;
   while (i < 10000000) {
       String *s = new_String("Tom");
       tombu = new_User(101, s);
       i++;
//       if ( i % 1000000 == 0 ) {
//           char *dump = calloc(500, sizeof(char));
//           heap_dump(dump);
//           printf("%s\n", dump);
//       }
   }

   GC_RESTORE_RP;
}

void test_alloc_outofmemory()
{
  int num_to_alloc;
  void *result;
  GC_SAVE_RP;
  gc();

  num_to_alloc = (heap_free() / User_type.size) + 1;
  
  result = heap_address();
  for( ; num_to_alloc > 0; num_to_alloc--) {
    void *tmp;
    result = alloc(&User_type);
    tmp = result;
    ADD_ROOT(tmp);
  }

  ASSERT(NULL, result);
  GC_RESTORE_RP;
}

int main1()
{
  if(!gc_init(1000))
    return EXIT_FAILURE;

  test_alloc_user();
  test_alloc_string();
  test_root_management();
  test_heap_dump();
  test_gc_string();
  test_gc_user();
  test_gc_walks_root_pointers();
  test_gc_with_pointer_array();
  test_gc_with_cycle();
  test_big_loop_doesnt_run_out_of_memory();
  test_alloc_outofmemory();

  printf("\n");

  return EXIT_SUCCESS;
}
