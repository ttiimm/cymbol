#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>

#define MAX_FIELDS 65535 

struct TypeDescriptor {
  char *name;
  int id;          /* same as position in table */
  int size;        /* size in bytes of struct */
  int num_fields;
  /* offset from ptr to object of only fields that are managed ptrs
     e.g., don't want to gc ptrs to functions, say */
  int field_offsets[MAX_FIELDS]; 
};

#define STRING 0
#define USER 1


#define LENGTH_INDEX 0

struct String {
  int type;
  int length;
  char *str;
};

/* string def */
struct TypeDescriptor string_type = {
  "string",
  STRING,                /* first type index */ 
  sizeof(struct String), /* size of field */
  2,                     /* fields */
  {offsetof(struct String, length),
   offsetof(struct String, str)}
};

struct User {
  int type; 
  int id;
  char *user;
};

/* sample def of User object (id, name) */
struct TypeDescriptor User_type = {
  "user",
  USER,
  sizeof(struct User),          /* hmm...size here */
  1,                            /* name field? */
  {offsetof(struct User, user)} /* offset of 2nd field */
};

struct TypeDescriptor *type_table;
int type_table_length;

#define MAX_ROOTS 100

void **roots[MAX_ROOTS];
int rp; /* index of next free space in array for a root */

typedef unsigned char byte;

byte *start_of_heap;
byte *end_of_heap;
byte *next_free;

#define MAX_HEAP_SIZE 512 /* bytes */

void alloc_heap()
{
  start_of_heap = malloc(MAX_HEAP_SIZE);
  end_of_heap = start_of_heap + MAX_HEAP_SIZE;
  next_free = start_of_heap;
}

void gc_init(struct TypeDescriptor *types, int n)
{
  type_table = types;
  type_table_length = n;
  alloc_heap();
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
  /* size for struct String, the String itself, 
     and null char */
  return alloc_space(sizeof(struct String) + size + 1);
}

void add_root(void *root)
{
  roots[rp++] = root;
}

void remove_root(void *root)
{
  int i;
  for(i = 0; i < rp; i++) 
    if(root == roots[i]) 
      /* overwrite with last root */
      roots[i] = roots[--rp];
}

void gc_string(void **old, int length_offset)
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

void gc_obj(void **old, int size)
{
  void *new;
  new = alloc_space(size);
  memcpy(new, *old, size);
  *old = new;
}

void gc()
{
  int i, type_idx, length_offset;
  struct TypeDescriptor type;
  void **old;
  byte *heap_to_free;
  heap_to_free = start_of_heap;
  alloc_heap();

  for(i = 0; i < rp; i++){
    old = roots[i];  
    /* type descriptor index is always
       stored at first location */
    type_idx = **(int **)old; 
    type = type_table[type_idx];
    if(type.id == string_type.id) {
      length_offset = type.field_offsets[LENGTH_INDEX];
      gc_string(old, length_offset);
    } else {
      gc_obj(old, type.size);
    }
  }

  free(heap_to_free);
}
