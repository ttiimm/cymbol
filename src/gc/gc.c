#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>

typedef unsigned char byte;

struct TypeDescriptor {
  char *name;
  int id;          /* same as position in table */
  int size;        /* size in bytes of struct */
  int num_fields;
  /* offset from ptr to object of only fields that are managed ptrs
     e.g., don't want to gc ptrs to functions, say */
  int *field_offsets; 
};

#define STRING     0
#define PRIM_ARRAY 1
#define OBJ_ARRAY  2
#define USER       3

struct String {
  int type;
  int length;
  /* pointer to start of chars 
     in heap */
  char *str;
};

struct TypeDescriptor String_type = {
  "string",
  STRING,                /* first type index */ 
  sizeof(struct String), /* size of string obj, not string */
  0,                     /* fields */
  NULL
};    

struct PrimitiveArray {
  int type;
  int length;
  /* pointer to start of array 
     elements in heap */
  byte *elements;
};

struct TypeDescriptor PrimitiveArray_type = {
  "prim_array",
  PRIM_ARRAY,
  sizeof(struct PrimitiveArray),
  0, 
  NULL
};

struct ObjArray {
  int type;
  int length;
  /* A pointer to an array of pointers to managed objs. */
  void *(*p)[];
};

/* must define array outside of struct b/c
   no int[] literal? */
int objarray_field_offsets[1] = {offsetof(struct ObjArray, p)};

struct TypeDescriptor ObjArray_type = {
  "obj_array",
  OBJ_ARRAY,
  sizeof(struct ObjArray),
  1, 
  objarray_field_offsets
};

struct User {
  int type; 
  int id;
  struct String *name;
};

/* must define array outside of struct b/c
   no int[] literal? */
int user_field_offsets[1] = {offsetof(struct User, name)};

/* sample def of User object (id, name) */
struct TypeDescriptor User_type = {
  "user",
  USER,
  sizeof(struct User),          
  1,                            
  user_field_offsets
};

struct TypeDescriptor *type_table;
int type_table_length;

#define MAX_ROOTS 100

void **roots[MAX_ROOTS];
int rp; /* index of next free space in array for a root */

const byte *heap1;
const byte *heap2;

/* these point to whichever is the 
   currently used heap */
byte *start_of_heap;
byte *end_of_heap;
byte *next_free;

#define MAX_HEAP_SIZE 512 /* bytes */

void switch_to_heap(byte *current)
{
  start_of_heap = heap1;
  end_of_heap = start_of_heap + MAX_HEAP_SIZE;
  next_free = start_of_heap;
}

void gc_init(struct TypeDescriptor *types, int n)
{
  type_table = types;
  type_table_length = n;
  heap1 = malloc(MAX_HEAP_SIZE);
  heap2 = malloc(MAX_HEAP_SIZE);
  switch_to_heap(&heap1);
}

int in_current_heap(void *p)
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

void *alloc_space(int size)
{
  void *p;

  /* word align allocation */
  if(size % sizeof(int) != 0)
    size += sizeof(int) - size % sizeof(int);

  if(next_free + size > end_of_heap)
    return NULL;

  p = next_free;
  next_free += size;
  return p;
}

void *alloc(int descriptor_index)
{
  struct TypeDescriptor t;
  
  if(descriptor_index > type_table_length 
     || descriptor_index < 0 ) 
    return NULL;

  t = type_table[descriptor_index];

  return alloc_space(t.size);
}


void *alloc_string(int size)
{
  struct String *string;
  /* size for struct String, the String itself, 
     and null char */
  string = alloc_space(sizeof(struct String) + size + 1);
  /* set str pointer to point into heap */
  string->str = (char *) string + sizeof(struct String);
  return string;
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

void move_string(void **old, int length_offset)
{
  int length, total_length;
  void *new;
  /* find length of string to alloc correct size*/
  length = *(int *) ((byte *) *old + length_offset);
  new = alloc_string(length);
  /* handle failures */
  total_length = sizeof(struct String) + length + 1;
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
    if(!on_heap(field)){
      printf("here");
      move(field);
    }}
}

void move(void **old) 
{
  int type_idx, length_offset;
  struct TypeDescriptor type;
  /* type descriptor index is always
     stored at first location */
  type_idx = **(int **)old; 
  /* printf("\n%d\n", type_table[2].id); */
  type = type_table[type_idx];
  /* printf("\ntype.id %d\ntype_idx %d\n", type.id, type_idx); */
  if(type.id == String_type.id) {
    length_offset = type.field_offsets[STR_LENGTH_INDEX];
    move_string(old, length_offset);
  } else {
    move_obj(old, type.size, type.num_fields, type.field_offsets);
  }
}

void move_roots()
{
  int i;

  for(i = 0; i < rp; i++){
    if(on_heap(*roots[i]))
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
