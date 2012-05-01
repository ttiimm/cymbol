#include <stddef.h>

#include "types-usr.h"

int user_field_offsets[1] = {offsetof(User, name)};

/* sample def of User object (id, name) */
TypeDescriptor User_type = {
  "User",
  sizeof(User),
  1,
  user_field_offsets
};


int node_field_offsets[1] = {offsetof(Node, neighbor)};

TypeDescriptor Node_type = {
  "Node",
  sizeof(Node),
  1,
  node_field_offsets
};
