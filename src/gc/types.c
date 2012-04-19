#include <stddef.h>

#include "types.h"

int primarray_field_offsets[1] = {
  offsetof(PrimitiveArray, elements)
};

TypeDescriptor PrimitiveArray_type = {
  "prim_array",
  sizeof(PrimitiveArray),
  1,
  primarray_field_offsets
};


int objarray_field_offsets[1] = {offsetof(ObjArray, p)};

TypeDescriptor ObjArray_type = {
  "obj_array",
  sizeof(ObjArray),
  1,
  objarray_field_offsets
};


int user_field_offsets[1] = {offsetof(User, name)};

/* sample def of User object (id, name) */
TypeDescriptor User_type = {
  "user",
  sizeof(User),
  1,
  user_field_offsets
};


int node_field_offsets[1] = {offsetof(Node, neighbor)};

TypeDescriptor Node_type = {
  "node",
  sizeof(Node),
  1,
  node_field_offsets
};
