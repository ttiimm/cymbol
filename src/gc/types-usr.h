#include "types.h"

typedef struct User {
  TypeDescriptor *type;
  byte *forward;
  int id;
  String *name;
} User;

TypeDescriptor User_type;
User *new_User(int id, String *name);

/* Used for testing cycles */
typedef struct Node {
  TypeDescriptor *type;
  byte *forward;
  char *payload;
  struct Node *neighbor;
} Node;

TypeDescriptor Node_type;
