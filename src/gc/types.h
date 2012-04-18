typedef unsigned char byte;

typedef struct TypeDescriptor {
  char *name;
  int size;        /* size in bytes of struct */
  int num_fields;
  /* offset from ptr to object of only fields that are managed ptrs
     e.g., don't want to gc ptrs to functions, say */
  int *field_offsets; 
} TypeDescriptor;


/* An array of ints, floats, or chars.  
   Note, like in C, a string is just an 
   array of chars */
typedef struct PrimitiveArray {
  TypeDescriptor *type;
  byte *forward;
  int length;
  /* pointer to start of array 
     elements in heap */
  char *elements;
} PrimitiveArray;

int primarray_field_offsets[1] = {
  offsetof(PrimitiveArray, elements)
};

TypeDescriptor PrimitiveArray_type = {
  "prim_array",
  sizeof(PrimitiveArray),
  1, 
  primarray_field_offsets
};


typedef PrimitiveArray String;
/* String_type set to PrimitiveArray_type in 
   gc_init() */
TypeDescriptor String_type;


typedef struct ObjArray {
  TypeDescriptor *type;
  byte *forward;
  int length;
  /* A pointer to an array of pointers to managed objs. */
  void *(*p)[];
} ObjArray;

int objarray_field_offsets[1] = {offsetof(ObjArray, p)};

TypeDescriptor ObjArray_type = {
  "obj_array",
  sizeof(ObjArray),
  1, 
  objarray_field_offsets
};


typedef struct User {
  TypeDescriptor *type;
  byte *forward;
  int id;
  String *name;
} User;

int user_field_offsets[1] = {offsetof(User, name)};

/* sample def of User object (id, name) */
TypeDescriptor User_type = {
  "user",
  sizeof(User),          
  1,                            
  user_field_offsets
};

/* Used for testing cycles */ 
typedef struct Node {
  TypeDescriptor *type;
  byte *forward;
  char *payload;
  struct Node *neighbor;
} Node;

int node_field_offsets[1] = {offsetof(Node, neighbor)};

TypeDescriptor Node_type = {
  "node",
  sizeof(Node),
  1,
  node_field_offsets
};
