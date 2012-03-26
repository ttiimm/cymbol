#include <stdio.h>
#include <stdlib.h>

#define MAX_FIELDS 65535 

struct TypeDescriptor {
  char *name;
  int id; /* same as position in table */
  int size; /* size in bytes */
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
  STRING,  /* first type index */ 
  0,       /* size */
  0,       /* no fields */ 
  {0}
};

/* sample def of User object (id, name) */
struct TypeDescriptor User_type = {
  "user",
  USER,
  4+4,  /* hmm...size here */
  1,    /* name field? */
  {4}   /* offset of 2nd field */
};

struct TypeDescriptor *type_table;
int type_table_length;

#define MAX_ROOTS 100

void *roots[MAX_ROOTS];
int rp; /* index of last root added in roots */

typedef unsigned char byte;

byte *space1;
byte *space2;
byte *current_space;
byte *end_of_heap;

#define MAX_HEAP_SIZE 32 /* bytes */

void gc_init(struct TypeDescriptor types[], int n)
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
  return alloc_space(size);
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

void gc()
{
}
