#include "gc.c"

#define TYPES_LENGTH 2

#define ASSERT(EXPECTED, RESULT)\
  if(EXPECTED == RESULT){ printf("%-30s %4s\n", __func__, "ok"); } else { printf("%-30s\t %4s\n", __func__, "fail"); }

#define ASSERT_NE(EXPECTED, RESULT)\
 if(EXPECTED != RESULT){ printf("%-30s %4s\n", __func__, "ok"); } else { printf("%-30s %4s\n", __func__, "fail"); }

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
  before = current_space;
  alloc(User_type.id);
  after = current_space;
  ASSERT(User_type.size, (after - before));
}

void test_alloc_string()
{
  byte *before, *after;
  before = current_space;
  alloc_string(12);
  after = current_space;
  ASSERT(12, (after - before));
}

void test_add_root()
{
  void *s;
  s = alloc_string(4);
  add_root(s);
  ASSERT(s, roots[rp - 1]);
}

void test_remove_root()
{
  void *s;
  s = alloc_string(3);
  add_root(s);
  ASSERT(s, roots[rp - 1]);
  remove_root(s);
  ASSERT_NE(s, roots[rp - 1]);
}

void test_couple_add_removes() 
{
  void *a, *b, *c, *d;
  int ap, bp, cp;
  a = alloc_string(3);
  b = alloc_string(3);
  c = alloc_string(3);
  d = alloc_string(3);

  add_root(a);
  ap = rp - 1;
  add_root(b);
  bp = rp - 1;
  add_root(c);
  cp = rp - 1;
  remove_root(b);
  ASSERT(a, roots[ap]);
  ASSERT(c, roots[bp]);

  add_root(d);
  ASSERT(d, roots[cp]);
  remove_root(a);
  ASSERT(d, roots[ap]);
}

void test_gc()
{
  
}

void test_alloc_outofmemory()
{
  int heap_space_left, num_to_alloc;
  void *result;
  heap_space_left = end_of_heap - current_space;
  num_to_alloc = (heap_space_left / User_type.size) + 1;

  result = current_space;
  for( ; num_to_alloc > 0; num_to_alloc--)
    result = alloc(User_type.id);

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
  test_gc();
  test_alloc_outofmemory();
  
  return EXIT_SUCCESS;
}
