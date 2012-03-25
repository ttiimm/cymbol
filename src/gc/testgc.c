#include "gc.c"

#define TYPES_LENGTH 2

#define ASSERT(EXPECTED, RESULT) if(EXPECTED == RESULT){ printf("%s: ok\n", __func__); } else { printf("%s: fail\n", __func__); }

void test_allocate_outofbounds() {
  void *a, *b;
  a = alloc(3);
  ASSERT(NULL, a);
  b = alloc(-1);
  ASSERT(NULL, b);
}

void test_allocate_user() {
  byte *before, *after;
  before = current_space;
  alloc(User_type.id);
  after = current_space;
  ASSERT(8, (after - before));
}

int main()
{
  struct TypeDescriptor types[2];
  types[0] = string_type;
  types[1] = User_type;
  gc_init(types, TYPES_LENGTH);

  if(!space_allocated()) 
    return EXIT_FAILURE;

  test_allocate_outofbounds();
  test_allocate_user();
  
  return EXIT_SUCCESS;
}
