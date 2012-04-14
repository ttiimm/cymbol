#include "gc.c"

#define TYPES_LENGTH 2

#define ASSERT(EXPECTED, RESULT)\
  if(EXPECTED == RESULT){ printf("."); } else { printf("\n%-30s failure on line %d\n", __func__, __LINE__); }

#define ASSERT_NE(EXPECTED, RESULT)\
  if(EXPECTED != RESULT){ printf("."); } else { printf("\n%-30s failure on line %d\n", __func__, __LINE__); }

void init() 
{
  TypeDescriptor types[3];
  String_type = PrimitiveArray_type;
  types[PRIM_ARRAY] = PrimitiveArray_type;
  types[OBJ_ARRAY] = ObjArray_type;
  types[USER] = User_type;

  gc_init(types, TYPES_LENGTH);
}

void test_alloc_invalid_type_idx() 
{
  void *pos_idx, *neg_idx;
  print_type_table();
  pos_idx = alloc(3);
  ASSERT(NULL, pos_idx);
  print_type_table();
  neg_idx = alloc(-1);
  ASSERT(NULL, neg_idx);
}

void test_alloc_user() 
{
  byte *before, *after;
  User *u;
  before = next_free;

  u = alloc(User_type.id);
  after = next_free;

  ASSERT(align(User_type.size), (after - before));
  ASSERT(1, in_heap(u));
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

  ASSERT(str_length, (after - before));
  ASSERT(1, in_heap(s));
  ASSERT(0, strcmp("abcdefghijkl", s->elements));
  ASSERT(1, in_heap(s->elements));
}

void test_add_root()
{
  void *s, **p;
  rp = 0;
  s = alloc_string(4);
  add_root(&s);
  p = roots[rp - 1];

  ASSERT(s, *p);
}

void test_remove_root()
{
  void *s, **p;
  rp = 0;
  s = alloc_string(3);

  add_root(&s);
  p = roots[rp - 1];
  ASSERT(s, *p);

  remove_root(&s);
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

  add_root(&a);
  ap = rp - 1;
  add_root(&b);
  bp = rp - 1;
  add_root(&c);
  cp = rp - 1;

  remove_root(&b);

  ASSERT(a, *roots[ap]);
  ASSERT(c, *roots[bp]);

  add_root(&d);
  ASSERT(d, *roots[cp]);

  remove_root(&a);
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

  a->type = String_type.id;
  a->length = 4;
  strcpy(a->elements, "abcd");

  b = alloc_string(5);

  add_root(&a);
  add_root(&b);
  remove_root(&b);
  a_len = 4 + sizeof(String) + 1;

  ASSERT_NE(a_len, MAX_HEAP_SIZE - heap_size());

  gc();

  ASSERT(a_len, MAX_HEAP_SIZE - heap_size());
  ASSERT_NE(old_a, &*a);
  ASSERT(0, a->type);
  ASSERT(4, a->length);
  ASSERT(0, strcmp("abcd", a->elements));
}

void test_gc_user()
{
  void *old_a;
  User *a; 
  String *b;
  rp = 0; /* reset roots list */

  b = alloc_string(5);
  b->type = String_type.id;
  b->length = 4;
  strcpy(b->elements, "abcd");

  a = alloc(User_type.id);
  old_a = &*a;

  a->type = User_type.id;
  a->id = 103;
  a->name  = b;

  add_root(&a);
  add_root(&b);


  ASSERT_NE(User_type.size, MAX_HEAP_SIZE - heap_size());

  gc();

  ASSERT(User_type.size, MAX_HEAP_SIZE - heap_size());
  ASSERT_NE(old_a, &*a);
  ASSERT(103, a->id);
  ASSERT(1, a->type);
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
  for( ; num_to_alloc > 0; num_to_alloc--) {
    result = alloc(User_type.id);
  }

  ASSERT(NULL, result);
}

int main()
{
  init();
  if(!is_space_allocated()) 
    return EXIT_FAILURE;

  test_alloc_invalid_type_idx();
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
