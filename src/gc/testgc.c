#include "gc.c"

#define TYPES_LENGTH 2

#define ASSERT(EXPECTED, RESULT)\
  if(EXPECTED == RESULT){ printf("."); } else { printf("\n%-30s %4s line %d\n", __func__, "fail", __LINE__); }

#define ASSERT_NE(EXPECTED, RESULT)\
  if(EXPECTED != RESULT){ printf("."); } else { printf("\n%-30s %4s line %d\n", __func__, "fail", __LINE__); }

void init() 
{
  struct TypeDescriptor types[2];
  types[0] = string_type;
  types[1] = User_type;
  gc_init(types, TYPES_LENGTH);
}

void test_alloc_invalid_type_idx() 
{
  void *pos_idx, *neg_idx;

  pos_idx = alloc(3);
  ASSERT(NULL, pos_idx);

  neg_idx = alloc(-1);
  ASSERT(NULL, neg_idx);
}

void test_alloc_user() 
{
  byte *before, *after;
  before = next_free;
  alloc(User_type.id);
  after = next_free;

  ASSERT(User_type.size, (after - before));
}

void test_alloc_string()
{
  int str_length;
  byte *before, *after;
  struct String *s;
  before = next_free;
  s = alloc_string(12);
  after = next_free;
  strcpy(s->str, "abcdefghijkl");
  str_length = sizeof(struct String) + 12 + 1;

  ASSERT(str_length, (after - before));
  ASSERT(0, strcmp("abcdefghijkl", s->str));
  ASSERT(1, on_heap((byte *) s->str));
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
  struct String *a, *b;
  int a_len;
  rp = 0; /* reset roots list */

  a = alloc_string(4);
  old_a = &*a; 

  a->type = string_type.id;
  a->length = 4;
  strcpy(a->str, "abcd");

  b = alloc_string(5);

  add_root(&a);
  add_root(&b);
  remove_root(&b);
  a_len = 4 + sizeof(struct String) + 1;

  ASSERT_NE(a_len, MAX_HEAP_SIZE - heap_size());

  gc();

  ASSERT(a_len, MAX_HEAP_SIZE - heap_size());
  ASSERT_NE(old_a, &*a);
  ASSERT(0, a->type);
  ASSERT(4, a->length);
  ASSERT(0, strcmp("abcd", a->str));
}

void test_gc_user()
{
  void *old_a;
  struct User *a, *b;
  rp = 0; /* reset roots list */

  a = alloc(User_type.id);
  old_a = &*a;

  a->type = User_type.id;
  a->id = 103;
  a->user  = "tim";

  b = alloc_string(5);

  add_root(&a);
  add_root(&b);
  remove_root(&b);

  ASSERT_NE(User_type.size, MAX_HEAP_SIZE - heap_size());

  gc();

  ASSERT(User_type.size, MAX_HEAP_SIZE - heap_size());
  ASSERT_NE(old_a, &*a);
  ASSERT(103, a->id);
  ASSERT(1, a->type);
  ASSERT(0, strcmp(a->user, "tim"));
}

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
  test_alloc_outofmemory();

  printf("\n");

  return EXIT_SUCCESS;
}
