#include <string.h>
#include <stddef.h>

#include "types.h"

int array_field_offsets[1] = { offsetof(Array, elements)};

TypeDescriptor Array_type = {
  "Array",
  sizeof(Array),
  1,
  array_field_offsets
};

Array *new_Array(int len, int array_type)
{
  return alloc_Array(len, array_type);
}

void add_to(Array *array, int index, Object *obj)
{
  ((void **) array->elements)[index] = obj;
}


String *new_String(char *str)
{
  int len;
  String *s;
  len = strlen(str);
  s = alloc_String(len);
  strcpy(s->elements, str);
  return s;
}
