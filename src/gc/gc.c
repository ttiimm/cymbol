#include <stdio.h>

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
  const int field_offsets[MAX_FIELDS]; 

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

void **roots; 

void *space1;
void *space2;
void *current_space;

void gc_init(struct TypeDescriptor types[])
{
  type_table = types;
  /* alloc both spaces */
}

void *alloc(int descriptor_index)
{
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
