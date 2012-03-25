#include <stdio.h>
#include <stdlib.h>

/* same as Java */
#define MAX_FIELDS 65535

struct TypeDescriptor {

  char *name;

  /* same as position in table */
  int id; 

  /* size in bytes */
  int size;

  int num_fields;

  /* offset from ptr to object of only fields that are managed ptrs
     e.g., don't want to gc ptrs to functions, say */
  int field_offsets[MAX_FIELDS]; 

};

#define STRING 0
#define USER 1

/* string def */
struct TypeDescriptor string_type = {
  "string",
  /* first type index */
  STRING,
  /* size */
  0,
  /* no fields */ 
  0,
  {0}
};

/* sample def of User object (id, name) */
struct TypeDescriptor User_type = {
  "user",
  USER,
  /* hmm...size here */
  4+4,
  1,
  /* offset of 2nd field */
  {4}
};

struct TypeDescriptor *type_table;
int type_table_length;

void **roots; 

typedef unsigned char byte;

byte *space1;
byte *space2;
byte *current_space;

void gc_init(struct TypeDescriptor types[], int length)
{
  type_table = types;
  type_table_length = length;
  space1 = malloc(8);
  space2 = NULL;
  current_space = space1;
  /*  printf("gc_init: current_space(%p)\n", current_space);*/ 
}

int space_allocated()
{
  return current_space != NULL;
}

void *alloc(int descriptor_index)
{
  struct TypeDescriptor t;
  void *p;
  
  if(descriptor_index > type_table_length 
     || descriptor_index < 0 ) 
    return NULL;

  t = type_table[descriptor_index];
  p = current_space;
  current_space += t.size;
  /* printf("alloc: current_space(%p)\n", current_space);*/
  return p;
}


void *alloc_string(int size)
{
  /* use type_table[STRING] */
}

void add_root(void **root)
{
}

void rm_root(void **root)
{
}

void gc()
{
}
