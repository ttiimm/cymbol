#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>

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


typedef struct Object {
  TypeDescriptor *type;
  /* address of obj after copy */
  byte *forward;
} Object;


Object **roots[100];
int rp = 0; /* index of next free space in array for a root */

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

void gc_init()
{
  String_type = PrimitiveArray_type;
  heap1 = malloc(MAX_HEAP_SIZE);
  heap2 = malloc(MAX_HEAP_SIZE);
  switch_to_heap(heap1);
}

int in_heap(Object *p)
{
  return (Object *) start_of_heap <= p && p <= (Object *) end_of_heap;
}

int is_space_allocated()
{
  return start_of_heap != NULL;
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

  if(next_free + aligned_size > end_of_heap)
    return NULL;

  p = next_free;
  next_free += aligned_size;
  return p;
}

Object *alloc(TypeDescriptor *type)
{
  Object *p;
  p = alloc_space(type->size);

  if(p == NULL)
    return NULL;

  memset(p, 0, type->size);
  p->type = type;
  p->forward = NULL;
  return p;
}


void *alloc_primarray(int size)
{
  PrimitiveArray *array;
  /* size for struct, primitive elems, and null char */
  array = alloc_space(sizeof(PrimitiveArray) + size + 1);

  if(array == NULL)
    return NULL;

  array->type = &PrimitiveArray_type;
  array->forward = NULL;
  /* set pointer to point into heap */
  array->elements = (char *) array + sizeof(PrimitiveArray);
  return array;
}

void *alloc_string(int size)
{
  String *s;
  s = alloc_primarray(size);
  s->type = &String_type;
  return s;
}

void add_root(Object **root)
{
  roots[rp++] = root;
}

void remove_root(Object **root)
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

void copy_primarray(PrimitiveArray **old)
{
  int total_length;
  void *new;
  new = alloc_primarray((*old)->length);
  /* handle failures */
  total_length = sizeof(PrimitiveArray) + (*old)->length + 1;
  memcpy(new, *old, total_length); 
  *old = new;
}

void copy(Object **old);

void copy_obj(Object **old, TypeDescriptor *type)
{
  int i;
  void *new, *field;
  new = alloc_space(type->size);
  memcpy(new, *old, type->size);
  *old = new;

  for(i = 0; i < type->num_fields; i++){
    field = (Object *) ((char *) new + type->field_offsets[i]); 
    if(!in_heap(field)){
      copy(field);
    }
  }
}

void copy(Object **old) 
{
  TypeDescriptor *type;
  type = (*old)->type;
  if(&*type == &PrimitiveArray_type
     || &*type == &String_type) {
    copy_primarray((PrimitiveArray **) old);
  } else {
    copy_obj(old, type);
  }
}

void copy_roots()
{
  int i;

  for(i = 0; i < rp; i++){
    if(in_heap(*roots[i]))
       continue;
    else
      copy(roots[i]);
  }
}

void gc()
{
  byte *other;
  other = start_of_heap == heap1 ? heap2 : heap1;
  switch_to_heap(other);
  copy_roots();
}
