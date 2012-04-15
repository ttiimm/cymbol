#include "gc.c"

#define TYPES_LENGTH 2

#define ASSERT(EXPECTED, RESULT)\
  if(EXPECTED == RESULT){ printf("."); } else { printf("\n%-30s failure on line %d\n", __func__, __LINE__); }

#define ASSERT_NE(EXPECTED, RESULT)\
  if(EXPECTED != RESULT){ printf("."); } else { printf("\n%-30s failure on line %d\n", __func__, __LINE__); }


void test_alloc_user() 
{
  byte *before, *after;
  User *u;
  before = next_free;

  u = (User *) alloc(&User_type);
  after = next_free;

  ASSERT(align(User_type.size), (after - before));
  ASSERT(1, in_heap((Object *) u));
}

void test_alloc_string()
{
  int str_length;
  byte *before, *after;
  String *s;
  before = next_free;
  s = alloc_string(12);
  after = next_free;
  strcpy(s->elements, "abcdefghijkl");
  str_length = sizeof(String) + 12 + 1;

  ASSERT(align(str_length), (after - before));
  ASSERT(1, in_heap((Object *) s));
  ASSERT(0, strcmp("abcdefghijkl", s->elements));
  ASSERT(1, in_heap((Object *) s->elements));
}

void test_add_root()
{
  String *s, **p;
  rp = 0;
  s = alloc_string(4);
  add_root((Object **) &s);
  p = (String **) roots[rp - 1];

  ASSERT(s, *p);
}

void test_remove_root()
{
  String *s, **p;
  rp = 0;
  s = alloc_string(3);

  add_root((Object **) &s);
  p = (String **) roots[rp - 1];
  ASSERT(s, *p);

  remove_root((Object **) &s);
  ASSERT(0, rp);
}

void test_couple_add_removes() 
{
  void *a, *b, *c, *d;
  int ap, bp, cp;
  rp = 0; /* reset roots list */

  a = alloc_string(3);
  b = alloc_string(3);
  c = alloc_string(3);
  d = alloc_string(3);

  add_root((Object **) &a);
  ap = rp - 1;
  add_root((Object **) &b);
  bp = rp - 1;
  add_root((Object **) &c);
  cp = rp - 1;

  remove_root((Object **) &b);

  ASSERT(a, *roots[ap]);
  ASSERT(c, *roots[bp]);

  add_root((Object **) &d);
  ASSERT(d, *roots[cp]);

  remove_root((Object **) &a);
  ASSERT(d, *roots[ap]);
}

void test_gc_string()
{
  void *old_a;
  String *a, *b;
  int a_len;
  rp = 0; /* reset roots list */

  a = alloc_string(4);
  old_a = &*a; 

  a->length = 4;
  strcpy(a->elements, "abcd");

  b = alloc_string(5);

  add_root((Object **) &a);
  add_root((Object **) &b);
  remove_root((Object **) &b);
  a_len = 4 + sizeof(String) + 1;

  ASSERT_NE(a_len, MAX_HEAP_SIZE - heap_size());

  gc();

  ASSERT(align(a_len), MAX_HEAP_SIZE - heap_size());
  ASSERT_NE(old_a, &*a);
  ASSERT(0, strcmp("prim_array", a->type->name));
  ASSERT(4, a->length);
  ASSERT(0, strcmp("abcd", a->elements));
}

void test_gc_user()
{
  void *old_a;
  User *a; 
  String *b;
  int userlen, strlen;

  rp = 0; /* reset roots list */

  b = alloc_string(3);
  b->length = 3;
  strcpy(b->elements, "tim");

  a = (User *) alloc(&User_type);
  old_a = &*a;

  a->id = 103;
  a->name  = b;

  add_root((Object **) &a);
  add_root((Object **) &b);

  ASSERT_NE(User_type.size, MAX_HEAP_SIZE - heap_size());

  gc();
  userlen = align(User_type.size);
  strlen = String_type.size + 3 + 1;
  ASSERT(align(userlen + strlen), MAX_HEAP_SIZE - heap_size());
  ASSERT_NE(old_a, &*a);
  ASSERT(103, a->id);
  ASSERT(&User_type, a->type);
  ASSERT(0, strcmp(a->name->elements, "tim"));
}

/* void test_gc_array() */
/* { */
/*   void *old_a; */
/*   struct Array *a; */
/*   struct String *s1, *s2, *strings[2]; */
/*   int total_size; */
/*   char * s1str; */
/*   rp = 0; /\* reset roots list *\/ */

/*   a = alloc(Array_type.id); */
/*   old_a = &*a; */

/*   a->type = Array_type.id; */
/*   a->length = 2; */
/*   a->arr  = (void *) &strings; */
/*   /\* printf("\n%p", &*a); *\/ */
/*   /\* printf("\n%p", &(*a).arr); *\/ */

/*   s1 = alloc_string(3); */
/*   s1->type = String_type.id; */
/*   s1->length = 3; */
/*   strcpy(s1->str, "abc"); */
/*   strings[0] = s1; */

/*   s2 = alloc_string(3); */
/*   s2->type = String_type.id; */
/*   s2->length = 3; */
/*   strcpy(s2->str, "def"); */
/*   strings[1] = s2; */

/*   total_size = 2 * (sizeof(struct String) + 3 + 1) + sizeof(struct Array); */

/*   add_root(&a); */
/*   /\* add_root(&s1); *\/ */
/*   /\* add_root(&s2); *\/ */
/*   /\* remove_root(&s2); *\/ */
/*   ASSERT(1, on_heap(&s1->str)); */

/*   gc(); */

/*   ASSERT(total_size, MAX_HEAP_SIZE - heap_size()); */
/*   ASSERT_NE(old_a, &*a); */
/*   ASSERT(2, a->length); */
/*   s1str = ((struct String *) (*a->arr)[0])->str; */
/*   ASSERT(0, strcmp("abc", s1str)); */
/*   ASSERT(1, on_heap(&s1->str)); */
/* } */

void test_alloc_outofmemory()
{
  int heap_space_left, num_to_alloc;
  void *result;
  heap_space_left = end_of_heap - next_free;
  num_to_alloc = (heap_space_left / User_type.size) + 1;
  
  result = next_free;
  for( ; num_to_alloc > 0; num_to_alloc--) 
    result = alloc(&User_type);

  ASSERT(NULL, result);
}

int main()
{
  gc_init();
  if(!is_space_allocated()) 
    return EXIT_FAILURE;

  test_alloc_user();
  test_alloc_string();
  test_add_root();
  test_remove_root(); 
  test_couple_add_removes();
  test_gc_string();
  test_gc_user();
  /* test_gc_array();  */
  test_alloc_outofmemory();

  printf("\n");

  return EXIT_SUCCESS;
}
