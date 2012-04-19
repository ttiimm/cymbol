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
  s = alloc_string(12);
  after = heap_address();
  strcpy(s->elements, "abcdefghijkl");
  str_length = sizeof_String(12);

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

  GC_SAVE_RP;
  gc();

  s = alloc_string(3);
  strcpy(s->elements, "tim");
  u = (User *) alloc(&User_type);
  u->name = s;
  ADD_ROOT(s);
//  ADD_ROOT(u);

  expected = "heap2[68,512]\n"
             "0000:String[32+3]=\"tim\"\n";

  result = malloc(1024);
  heap_dump(result);

  printf("\n%s", result);
  ASSERT_STR(expected, result);

  GC_RESTORE_RP;
  free(result);
}

void test_gc_string()
{
  void *old_a, *old_b;
  String *a, *b;
  int a_len;
  GC_SAVE_RP;

  a = alloc_string(4);
  old_a = &*a; 

  strcpy(a->elements, "abcd");

  b = alloc_string(5);
  old_b = &*b;

  ADD_ROOT(a);

  a_len = sizeof_String(4);

  ASSERT_NE(a_len, MAX_HEAP_SIZE - heap_size());

  gc();

  ASSERT(align(a_len), MAX_HEAP_SIZE - heap_size());
  ASSERT_NE(old_a, &*a);
  ASSERT(old_b, &*b);
  ASSERT_STR("prim_array", a->type->name);
  ASSERT(4, a->length);
  ASSERT_STR("abcd", a->elements);
  GC_RESTORE_RP;
}

void test_gc_user()
{
  void *old_a;
  User *a; 
  String *b;
  int userlen, strlen;

  GC_SAVE_RP;

  b = alloc_string(3);
  strcpy(b->elements, "tim");

  a = (User *) alloc(&User_type);
  old_a = &*a;

  a->id = 103;
  a->name  = b;

  ADD_ROOT(a);
  ADD_ROOT(b);

  ASSERT_NE(User_type.size, MAX_HEAP_SIZE - heap_size());

  gc();

  userlen = align(User_type.size);
  strlen = String_type.size + 3 + 1;
 
  ASSERT(align(userlen + strlen), MAX_HEAP_SIZE - heap_size());
  ASSERT_NE(old_a, &*a);
  ASSERT(103, a->id);
  ASSERT(&User_type, a->type);
  ASSERT_STR("tim", a->name->elements);
  ASSERT(NULL, a->forward);
  GC_RESTORE_RP;
}

void test_gc_user_single_root()
{
  void *old_a;
  User *a; 
  String *b;
  int userlen, strlen;

  GC_SAVE_RP;

  b = alloc_string(3);
  strcpy(b->elements, "tim");

  a = (User *) alloc(&User_type);
  old_a = &*a;

  a->id = 103;
  a->name  = b;
 
  ADD_ROOT(a);

  ASSERT_NE(User_type.size, MAX_HEAP_SIZE - heap_size());

  gc();

  userlen = align(User_type.size);
  strlen = String_type.size + 3 + 1;

  ASSERT(align(userlen + strlen), MAX_HEAP_SIZE - heap_size());
  ASSERT_NE(old_a, &*a);
  ASSERT(103, a->id);
  ASSERT(&User_type, a->type);
  ASSERT_STR("tim", a->name->elements);
  GC_RESTORE_RP;
}

void test_gc_with_cycle()
{
  Node *a, *b;

  GC_SAVE_RP;
  gc();

  a = (Node *) alloc(&Node_type);
  b = (Node *) alloc(&Node_type);
  a->payload = "a";
  b->payload = "b";
  a->neighbor = b;
  b->neighbor = a;
 
  ADD_ROOT(a);
  ADD_ROOT(b);

  ASSERT(align(2 * Node_type.size), MAX_HEAP_SIZE - heap_size());

  gc();

  ASSERT(align(2 * Node_type.size), MAX_HEAP_SIZE - heap_size());
  ASSERT_STR("a", b->neighbor->payload);
  ASSERT_STR("b", a->neighbor->payload);
  GC_RESTORE_RP;
}

/* void test_gc_objarray() */
/* { */
/*   void *old_a; */
/*   ObjArray *a; */
/*   String *s1, *s2, *strings[2]; */
/*   int total_size; */
/*   char * s1str; */
/*   gc_init(); */
/*   rp = 0; /\* reset roots list *\/ */

/*   a = (ObjArray *) alloc(&ObjArray_type); */
/*   old_a = &*a; */

/*   a->p = (void *) &strings; */

/*   s1 = alloc_string(3); */
/*   strcpy(s1->elements, "abc"); */
/*   strings[0] = s1; */

/*   s2 = alloc_string(3); */
/*   strcpy(s2->elements, "def"); */
/*   strings[1] = s2; */

/*   total_size = 2 * (sizeof(String) + 3 + 1) + sizeof(ObjArray); */

/*   ADD_ROOT((Object **) &a); */
  
/*   ASSERT(1, in_heap((Object *) &s1->elements)); */
/*   ASSERT(align(total_size), MAX_HEAP_SIZE - heap_size()); */

/*   gc(); */

/*   printf("\n%d", align(total_size)); */
/*   printf("\n%d", MAX_HEAP_SIZE - heap_size()); */

/*   ASSERT(align(total_size), MAX_HEAP_SIZE - heap_size()); */
/*   ASSERT_NE(old_a, &*a); */
/*   ASSERT(2, a->length); */
/*   s1str = ((String *) (*a->p)[0])->elements; */
/*   ASSERT(0, strcmp("abc", s1str)); */
/*   ASSERT(1, in_heap((Object *) &s1->elements)); */
/* } */

void test_alloc_outofmemory()
{
  int num_to_alloc;
  void *result;
  num_to_alloc = ( heap_size() / User_type.size) + 1;
  
  result = heap_address();
  for( ; num_to_alloc > 0; num_to_alloc--) 
    result = alloc(&User_type);

  ASSERT(NULL, result);
}

int main()
{
  if(!gc_init())
    return EXIT_FAILURE;

  test_alloc_user();
  test_alloc_string();
  test_root_management();
  test_heap_dump();
  test_gc_string();
  test_gc_user();
  test_gc_user_single_root();
  test_gc_with_cycle();
  /* test_gc_objarray(); */
  test_alloc_outofmemory();

  printf("\n");

  return EXIT_SUCCESS;
}
