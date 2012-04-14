#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>

typedef struct TypeDescriptor {
  char *name;
  int id;          /* same as position in table */
  int size;        /* size in bytes of struct */
  int num_fields;
  /* offset from ptr to object of only fields that are managed ptrs
     e.g., don't want to gc ptrs to functions, say */
  int *field_offsets; 
} TypeDescriptor;

#define PRIM_ARRAY 0
#define OBJ_ARRAY  1
#define USER       2

/* An array of ints, floats, or chars.  
   Note, like in C, a string is just an 
   array of chars */
typedef struct PrimitiveArray {
  int type;
  int length;
  /* pointer to start of array 
     elements in heap */
  char *elements;
} PrimitiveArray;

#define LENGTH_INDEX 0

int primarray_field_offsets[2] = {
  offsetof(PrimitiveArray, length),
  offsetof(PrimitiveArray, elements)
};

TypeDescriptor PrimitiveArray_type = {
  "prim_array",
  PRIM_ARRAY,
  sizeof(PrimitiveArray),
  2, 
  primarray_field_offsets
};


typedef PrimitiveArray String;
/* set in main() to PrimitiveArray_type */
TypeDescriptor String_type;


typedef struct ObjArray {
  int type;
  int length;
  /* A pointer to an array of pointers to managed objs. */
  void *(*p)[];
} ObjArray;

int objarray_field_offsets[1] = {offsetof(ObjArray, p)};

TypeDescriptor ObjArray_type = {
  "obj_array",
  OBJ_ARRAY,
  sizeof(ObjArray),
  1, 
  objarray_field_offsets
};


typedef struct User {
  int type; 
  int id;
  String *name;
} User;

int user_field_offsets[1] = {offsetof(User, name)};

/* sample def of User object (id, name) */
TypeDescriptor User_type = {
  "user",
  USER,
  sizeof(User),          
  1,                            
  user_field_offsets
};


TypeDescriptor *type_table;
int type_table_length;

#define MAX_ROOTS 100

void **roots[MAX_ROOTS];
int rp; /* index of next free space in array for a root */

typedef unsigned char byte;

byte *heap1;
byte *heap2;

/* these point to whichever is the 
   currently used heap */
byte *start_of_heap;
byte *end_of_heap;
byte *next_free;

#define MAX_HEAP_SIZE 512 /* bytes */

void switch_to_heap(byte *next)
{
  start_of_heap = next;
  end_of_heap = start_of_heap + MAX_HEAP_SIZE;
  next_free = start_of_heap;
}

void print_type_table()
{
  int i;
  printf("Types in table\n");
  for(i = 0; i <= type_table_length; i++)
    printf("%s\n", type_table[i].name);
}

void gc_init(TypeDescriptor *types, int n)
{
  type_table = types;
  type_table_length = n;
  print_type_table();

  heap1 = malloc(MAX_HEAP_SIZE);
  heap2 = malloc(MAX_HEAP_SIZE);
  switch_to_heap(heap1);
}

int in_heap(void *p)
{
  return start_of_heap <= (byte *) p && (byte *) p <= end_of_heap;
}

int is_space_allocated()
{
  return  start_of_heap != NULL;
}

int heap_size()
{
  return end_of_heap - next_free;
}

int align(int size)
{
    /* word align allocation */
  if(size % sizeof(int) != 0)
    size += sizeof(int) - size % sizeof(int);
  
  return size;
}

void *alloc_space(int size)
{
  void *p;
  int aligned_size;
  
  aligned_size = align(size);
  printf("\naligned_size %d", size);
  if(next_free + aligned_size > end_of_heap)
    return NULL;

  p = next_free;
  next_free += aligned_size;
  return p;
}

void *alloc(int descriptor_index)
{
  TypeDescriptor t;

  if(descriptor_index > type_table_length 
     || descriptor_index < 0 ) 
    return NULL;

  t = type_table[descriptor_index];

  return alloc_space(t.size);
}


void *alloc_primarray(int size)
{
  PrimitiveArray *array;
  /* size for struct, primitive elems, and null char */
  array = alloc_space(sizeof(PrimitiveArray) + size + 1);
  /* set pointer to point into heap */
  array->elements = (char *) array + sizeof(PrimitiveArray);
  return array;
}

void *alloc_string(int size)
{
  return alloc_primarray(size);
}

void add_root(void *root)
{
  roots[rp++] = root;
}

void remove_root(void *root)
{
  int i;
  for(i = 0; i < rp; i++) {
    if(root == roots[i]) {
      /* overwrite with last root */
      roots[i] = roots[--rp];
      break;
    }
  }
}

void move_primarray(void **old, int length_offset)
{
  int length, total_length;
  void *new;
  /* find length of string to alloc correct size*/
  length = *(int *) ((byte *) *old + length_offset);
  new = alloc_primarray(length);
  /* handle failures */
  total_length = sizeof(PrimitiveArray) + length + 1;
  memcpy(new, *old, total_length); 
  *old = new;
}

void move(void **old);

void move_obj(void **old, int size, int num_fields, int offsets[])
{
  int i;
  void *new, *field;
  new = alloc_space(size);
  memcpy(new, *old, size);
  *old = new;

  for(i = 0; i < num_fields; i++){
    field = (void *) ((char *) new + offsets[i]); 
    if(!in_heap(field)){
      move(field);
    }
  }
}

void move(void **old) 
{
  int type_idx, length_offset;
  TypeDescriptor type;
  /* type descriptor index is always
     stored at first location */
  type_idx = **(int **)old; 
  /* printf("\n%d\n", type_table[2].id); */
  type = type_table[type_idx];
  /* printf("\ntype.id %d\ntype_idx %d\n", type.id, type_idx); */
  if(type.id == PrimitiveArray_type.id) {
    length_offset = type.field_offsets[LENGTH_INDEX];
    move_primarray(old, length_offset);
  } else {
    move_obj(old, type.size, type.num_fields, type.field_offsets);
  }
}

void move_roots()
{
  int i;

  for(i = 0; i < rp; i++){
    if(in_heap(*roots[i]))
       continue;
    else
      move(roots[i]);
  }
}

void gc()
{
  byte *other;
  other = start_of_heap == heap1 ? heap2 : heap1;
  switch_to_heap(other);
  move_roots();
}
