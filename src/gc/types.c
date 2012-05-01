#include <stddef.h>

#include "types.h"

int array_field_offsets[1] = { offsetof(Array, elements)};

TypeDescriptor Array_type = {
  "Array",
  sizeof(Array),
  1,
  array_field_offsets
};

