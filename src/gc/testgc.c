#include "gc.c"

#define TYPES_LENGTH 2

#define ASSERT(EXPECTED, RESULT) if(EXPECTED == RESULT){ printf("%s: ok\n", __func__); } else { printf("%s: fail\n", __func__); }

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
  test_alloc_outofmemory();
  
  return EXIT_SUCCESS;
}
