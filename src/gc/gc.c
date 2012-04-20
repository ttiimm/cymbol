#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "gc.h"

void copy(Object **old);
void check_forward_or_copy(Object **chkobj, Object **setobj);
void *get_field_pointer(Object *obj, int i);

extern int _rp;
extern Object **_roots[100];

byte *heap1;
byte *heap2;

/* these point to whichever is the 
   currently used heap */
byte *start_of_heap;
byte *end_of_heap;
byte *next_free;

void switch_to_heap(byte *next)
{
  start_of_heap = next;
  end_of_heap = start_of_heap + MAX_HEAP_SIZE;
  next_free = start_of_heap;
}

bool gc_init()
{
  _rp = 0;
  String_type = PrimitiveArray_type;
  heap1 = malloc(MAX_HEAP_SIZE);
  heap2 = malloc(MAX_HEAP_SIZE);
  switch_to_heap(heap1);

  return heap1 != NULL && heap2 != NULL;
}


void dump_string(String *string, int addr, long int size, char *tmp)
{
  char *str;
  int len;
  str = string->elements;
  /* count null terminator */
  len = string->length + 1;
  sprintf(tmp, "%04d:String[%lu+%d]=\"%s\"\n", addr, size, len, str);
}

void dump_obj(Object *obj, int addr, long int size, char *buf)
{
  int i, f_addr;
  char *name, tmp[50];
  name = obj->type->name;
  sprintf(tmp, "%04d:%s[%lu]->[", addr, name, size);
  strcpy(buf, tmp);

  if(obj->type->num_fields >= 1) {
    f_addr = ((byte *) &**(Object **) get_field_pointer(obj, 0)) - start_of_heap;
    sprintf(tmp, "%d", f_addr);
    strcat(buf, tmp);
  }

  for(i = 1; i < obj->type->num_fields; i++) {
    f_addr = (byte *) get_field_pointer(obj, i) - start_of_heap;
    sprintf(tmp, ", %d", f_addr);
    strcat(buf, tmp);
  }

  strcat(buf, "]\n");
}

void dump_roots(char *buf)
{
  Object * o;
  int i, addr, size;
  for(i = 0; i < _rp; i++) {
    char tmp[50];
    o = *_roots[i];
    addr = (byte *)&*o - start_of_heap;
    size = o->type->size;
    if(&*o->type == &String_type) {
      dump_string((String *) o, addr, size, tmp);
    } else if(&*o->type == &PrimitiveArray_type) {

    } else {
      dump_obj(o, addr, size, tmp);
    }

    strcat(buf, tmp);
  }

}

void heap_dump(char *buf)
{
  int heap_num, size, total;
  char tmp[24], *template;

  heap_num = start_of_heap == heap1 ? 1: 2;
  size = next_free - start_of_heap;
  total = end_of_heap - start_of_heap;
  template = "heap%d[%d,%d]\n";

  sprintf(tmp, template, heap_num, size, total);
  strcat(buf, tmp);

  dump_roots(buf);
}

byte *heap_address()
{
  return next_free;
}

bool in_heap(Object *p)
{
  return (Object *) start_of_heap <= p && p <= (Object *) end_of_heap;
}

int heap_size()
{
  return end_of_heap - next_free;
}



int sizeof_String(int len)
{
  /* size for struct, primitive elems, and null char */
  return sizeof(PrimitiveArray) + len + 1;
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

/* len
       length of the array to alloc */
PrimitiveArray *alloc_primarray(int len)
{
  PrimitiveArray *array;
  array = alloc_space(sizeof_String(len));

  if(array == NULL)
    return NULL;

  array->type = &PrimitiveArray_type;
  array->forward = NULL;
  array->length = len;
  /* set pointer to point into heap */
  array->elements = (char *) array + sizeof(PrimitiveArray);
  return array;
}

/* len 
       number of chars in the string */
String *alloc_string(int len)
{
  String *s;
  s = alloc_primarray(len);

  if(s == NULL)
    return NULL;

  s->type = &String_type;
  return s;
}

void copy_primarray(PrimitiveArray **old)
{
  int total_length;
  void *new;
  new = alloc_primarray((*old)->length);
  /* handle failures */
  total_length = sizeof_String((*old)->length);
  memcpy(new, *old, total_length); 
  (*old)->forward = new;
  *old = new;
}

void *get_field_pointer(Object *obj, int i)
{
  return (char *) obj + obj->type->field_offsets[i];
}


void copy_obj(Object **old, TypeDescriptor *type)
{
  int i;
  void *new;
  Object **old_f, *new_f;

  new = alloc_space(type->size);
  memcpy(new, *old, type->size);
  (*old)->forward = new;
  *old = new;

  for(i = 0; i < type->num_fields; i++){
    old_f = (Object **) get_field_pointer(*old, i);
    new_f = (Object *) get_field_pointer(new, i);
    check_forward_or_copy(old_f, &new_f);    
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

void check_forward_or_copy(Object **chkobj, Object **setobj)
{
  if((*chkobj)->forward != NULL)
    *setobj = (Object *) (*chkobj)->forward;
  else
    copy(chkobj);
}

void copy_roots()
{
  int i;

  for(i = 0; i < _rp; i++)
    check_forward_or_copy(_roots[i], _roots[i]);
}

void gc()
{
  byte *other;
  other = start_of_heap == heap1 ? heap2 : heap1;
  switch_to_heap(other);
  copy_roots();
}
