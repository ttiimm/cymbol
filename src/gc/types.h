typedef unsigned char byte;
typedef unsigned int bool;

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

TypeDescriptor PrimitiveArray_type;


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

TypeDescriptor ObjArray_type;


typedef struct User {
  TypeDescriptor *type;
  byte *forward;
  int id;
  String *name;
} User;

TypeDescriptor User_type;


/* Used for testing cycles */ 
typedef struct Node {
  TypeDescriptor *type;
  byte *forward;
  char *payload;
  struct Node *neighbor;
} Node;

TypeDescriptor Node_type;
