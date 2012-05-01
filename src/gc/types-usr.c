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


User *new_User(int id, String *name)
{
  User *u;
  u = (User *) alloc(&User_type);
  u->id = 103;
  u->name = name;

  return u;
}


int node_field_offsets[1] = {offsetof(Node, neighbor)};

TypeDescriptor Node_type = {
  "Node",
  sizeof(Node),
  1,
  node_field_offsets
};

Node *new_Node(char *payload, Node *neighbor)
{
  Node *node;
  node = (Node *) alloc(&Node_type);
  node->payload = payload;
  node->neighbor = neighbor;
  return node;
}
