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

byte *space1;
byte *space2;
byte *current_space;
byte *end_of_heap;

#define MAX_HEAP_SIZE 512 /* bytes */

void gc_init(struct TypeDescriptor *types, int n)
{
  type_table = types;
  type_table_length = n;
  space1 = malloc(MAX_HEAP_SIZE);
  end_of_heap = space1 + MAX_HEAP_SIZE;
  space2 = NULL;
  current_space = space1;
}

int is_space_allocated()
{
  return current_space != NULL;
}

int heap_size()
{
  return end_of_heap - current_space;
}

void *alloc_space(int size)
{
  void *p;

  if(current_space + size > end_of_heap)
    return NULL;

  p = current_space;
  current_space += size;
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

void gc_other(void **old, struct TypeDescriptor desc)
{
  printf("%s\n", desc.name);
}

void gc()
{
  int i, type_idx, length_offset;
  struct TypeDescriptor type;
  void **old;
  space2 = malloc(MAX_HEAP_SIZE);
  end_of_heap = space2 + MAX_HEAP_SIZE;
  current_space = space2;
  for(i = 0; i < rp; i++){
    old = roots[i];  
    /* type descriptor index is always
       stored at first location */
    type_idx = **(int **)old; 
    /* printf("**%s\n", type_table[1].name); */
    type = type_table[type_idx];
    /* printf("**%s\n", type_table[1].name); */
    
    if(type.id == string_type.id) {
      length_offset = type.field_offsets[0];
      gc_string(old, length_offset);
    } else {
      gc_other(old, type);
    }
  }
  free(space1);
}
