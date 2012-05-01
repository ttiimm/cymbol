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


typedef struct Object {
  TypeDescriptor *type;
  /* address of obj after copy */
  byte *forward;
} Object;


/**
 * Array
 */

#define ARRAY_CHAR    0
#define ARRAY_INT     1
#define ARRAY_FLOAT   2
#define ARRAY_POINTER 3

/* An array of ints, floats, chars, or pointers.

   All pointers must be to objects in the heap.  */
typedef struct Array {
  TypeDescriptor *type;
  byte *forward;
  int array_type;
  int length;
  /* pointer to start of array
     of elements in heap */
  void *elements;
} Array;

TypeDescriptor Array_type;
Array *new_Array();


/**
 * String
 */

/* Like in C, a string is just an array of chars */
typedef Array String;
/* String_type set to Array_type in gc_init() */
TypeDescriptor String_type;
String *new_String();
