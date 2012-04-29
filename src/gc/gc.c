#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "gc.h"

void dump_root(Object * obj, char *buf);
void unmark_root(Object *obj);

void copy(Object **old);
void copy_or_set(Object **copy_from, Object **copy_to);
Object **get_field_pointer(Object *obj, int i);


extern int _rp;
extern Object **_roots[100];

int max_heap_size; /* in bytes */

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
  end_of_heap = start_of_heap + max_heap_size;
  next_free = start_of_heap;
}

bool gc_init(int heap_size)
{
  _rp = 0;
  String_type = Array_type;
  max_heap_size = heap_size;
  heap1 = malloc(max_heap_size);
  heap2 = malloc(max_heap_size);
  switch_to_heap(heap1);

  return heap1 != NULL && heap2 != NULL;
}


void unmark_pointer_array(Array *array)
{
  int i;
  Object *ele;

  array->forward = NULL;

  for(i = 0; i < array->length; i++) {
    ele = ((Object **) array->elements)[i];
    unmark_root(ele);
  }
}

void unmark_obj(Object *obj)
{
  int i;
  Object *field;

  obj->forward = NULL;

  if(obj->type == &String_type)
    return;

  for(i = 0; i < obj->type->num_fields; i++) {
    field = *get_field_pointer(obj, i);
    unmark_root(field);
  }
}

void unmark_root(Object *obj)
{
  if(obj == NULL || obj->forward == NULL)
    return;

  if(obj->type == &Array_type && ((Array *) obj)->array_type == ARRAY_POINTER)
    unmark_pointer_array((Array *) obj);
  else
    unmark_obj(obj);
}

void unmark()
{
  int i;
  for(i = 0; i < _rp; i++)
    unmark_root(*_roots[i]);
}


void dump_string(String *string, int addr, long int size, char *buf)
{
  char *str;
  int len;
  char tmp[100];
  str = string->elements;
  /* count null terminator */
  len = string->length + 1;
  sprintf(tmp, "%04d:String[%lu+%d]=\"%s\"\n", addr, size, len, str);
  strcat(buf, tmp);
}

void dump_element(int index, Array *array, char *template, char *tmp, char *buf)
{
  int a_addr;
  Object *e;
  e = ((Object **) array->elements)[index];
  a_addr = ((byte *) &*(e) - start_of_heap);
  sprintf(tmp, template, a_addr >= 0 ? a_addr : -1);
  strcat(buf, tmp);
}

void dump_array(Array *array, int addr, long int size, char *buf)
{
  int i;
  char tmp[50];
  sprintf(tmp, "%04d:Array[%lu+%d]->[", addr, size, array->length);
  strcat(buf, tmp);

  /**
   * Strings are handled separately
   */
  switch(array->array_type)
  {
    Object *e;

    case ARRAY_POINTER:
      if(array->length >= 1) {
        dump_element(0, array, "%d", tmp, buf);
      }

      for(i = 1; i < array->length; i++) {
        dump_element(i, array, ", %d", tmp, buf);
      }
      strcat(buf, "]\n");

      for(i = 0; i < array->length; i++) {
        e = ((Object **) array->elements)[i];
        dump_root(e, buf);
      }
      break;
  }
}

void dump_field(int index, Object *obj, char *template, char *tmp, char *buf)
{
  int f_addr;
  f_addr = ((byte *) &**(Object **) get_field_pointer(obj, index)) - start_of_heap;
  sprintf(tmp, template, f_addr >= 0 ? f_addr : -1);
  strcat(buf, tmp);
}

void dump_obj(Object *obj, int addr, long int size, char *buf)
{
  int i;
  char *name, tmp[50];
  Object **field;

  name = obj->type->name;
  sprintf(tmp, "%04d:%s[%lu]->[", addr, name, size);
  strcat(buf, tmp);

  if(obj->type->num_fields >= 1) {
    dump_field(0, obj, "%d", tmp, buf);
  }

  for(i = 1; i < obj->type->num_fields; i++) {
    dump_field(i, obj, ", %d", tmp, buf);
  }

  strcat(buf, "]\n");

  for(i = 0; i < obj->type->num_fields; i++) {
    field = get_field_pointer(obj, i);
    dump_root(*field, buf);
  }
}

void dump_root(Object * obj, char *buf)
{
  int addr, size;

  if(obj == NULL || obj->forward)
    return;

  obj->forward = (byte *) obj;
  addr = (byte *)&*obj - start_of_heap;
  size = obj->type->size;
  if(&*obj->type == &String_type) {
    dump_string((String *) obj, addr, size, buf);
  } else if(&*obj->type == &Array_type) {
    dump_array((Array *) obj, addr, size, buf);
  } else {
    dump_obj(obj, addr, size, buf);
  }
}

void dump_root_list(char *buf)
{
  int i;
  for(i = 0; i < _rp; i++)
    dump_root(*_roots[i], buf);
}

void heap_dump(char *buf)
{
  int heap_num, size, total;
  char heap_info[24], *template;

  heap_num = start_of_heap == heap1 ? 1: 2;
  size = heap_allocated();
  total = end_of_heap - start_of_heap;
  template = "heap%d[%d,%d]\n";

  sprintf(heap_info, template, heap_num, size, total);
  strcat(buf, heap_info);

  dump_root_list(buf);
  unmark();
}


byte *heap_address()
{
  return next_free;
}

bool in_heap(Object *p)
{
  return (Object *) start_of_heap <= p && p <= (Object *) end_of_heap;
}

int heap_allocated()
{
  return next_free - start_of_heap;
}

int heap_free()
{
  return end_of_heap - next_free;
}



int sizeof_array(int len, int type)
{
  int prim_size;

  switch(type)
  {
      case ARRAY_CHAR:
        /* room for null char at end */
        prim_size = sizeof(char) * len + 1;
        break;
      case ARRAY_INT:
        prim_size = sizeof(int) * len;
        break;
      case ARRAY_FLOAT:
        prim_size = sizeof(float) * len;
        break;
      case ARRAY_POINTER:
        prim_size = sizeof(void *) * len;
        break;
  }

  return sizeof(Array) + prim_size;
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

  if(next_free + aligned_size > end_of_heap) {
    gc();

    if(next_free + aligned_size > end_of_heap)
      return NULL;
    else
      return alloc_space(size);
  }

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
Array *alloc_array(int len, int type)
{
  Array *array;
  array = alloc_space(sizeof_array(len, type));

  if(array == NULL)
    return NULL;

  array->type = &Array_type;
  array->array_type = type;
  array->forward = NULL;
  array->length = len;
  /* set elements to point into heap right after
   * meta data */
  array->elements = (char *) array + sizeof(Array);
  return array;
}

/* len 
       number of chars in the string */
String *alloc_String(int len)
{
  String *s;
  s = alloc_array(len, ARRAY_CHAR);

  if(s == NULL)
    return NULL;

  s->type = &String_type;
  return s;
}

void copy_elements(Array **old, Array *new)
{
  int i, len;
  Object *from;
  Object *to;

  switch((*old)->array_type)
  {
    case ARRAY_CHAR:
      len = ((*old)->length + 1) * sizeof(char);
      memcpy(new->elements, (*old)->elements, len);
      break;
    case ARRAY_INT:
      len = ((*old)->length) * sizeof(int);
      memcpy(new->elements, (*old)->elements, len);
      break;
    case ARRAY_FLOAT:
      len = ((*old)->length) * sizeof(float);
      memcpy(new->elements, (*old)->elements, len);
      break;
    case ARRAY_POINTER:
      for(i = 0; i < (*old)->length; i++) {
        from = ((Object **) (*old)->elements)[i];
        copy_or_set(&from, &to);
        ((Object **) new->elements)[i] = from;
      }
      break;
  }
}

void copy_array(Array **old)
{
  Array *old_array, *new;
  void *new_elements;

  old_array = *old;
  new = alloc_array(old_array->length, old_array->array_type);
  new_elements = new->elements;
  (*old)->forward = (byte *) new;
  memcpy(new, *old, sizeof(Array));
  new->forward = NULL;
  new->elements = new_elements;
  copy_elements(old, new);
  *old = new;
}

Object **get_field_pointer(Object *obj, int i)
{
  return (Object **) ((byte *) obj + obj->type->field_offsets[i]);
}

void copy_fields(Object **old, Object *new, TypeDescriptor *type)
{
  Object **old_f, **new_f;
  int i;
  for(i = 0; i < type->num_fields; i++){
    old_f = get_field_pointer(*old, i);
    new_f = get_field_pointer(new, i);
    copy_or_set(old_f, new_f);
  }
}

void copy_obj(Object **old, TypeDescriptor *type)
{
  Object *new;

  new = alloc(type);
  (*old)->forward = (byte *) new;
  memcpy(new, *old, type->size);
  copy_fields(old, new, type);
  new->forward = NULL;
  *old = new;
}

void copy(Object **old) 
{
  TypeDescriptor *type;
  type = (*old)->type;
  if(&*type == &Array_type || &*type == &String_type) {
    copy_array((Array **) old);
  } else {
    copy_obj(old, type);
  }
}

void copy_or_set(Object **copy_from, Object **copy_to)
{
  if(*copy_from != NULL && (*copy_from)->forward != NULL) {
    *copy_to = (Object *) (*copy_from)->forward;
  } else if(*copy_from != NULL) {
    copy(copy_from);
    *copy_to = *copy_from;
  }
}

void copy_roots()
{
  int i;

  for(i = 0; i < _rp; i++)
    copy_or_set(_roots[i], _roots[i]);
}

void gc()
{
  byte *other;
  other = start_of_heap == heap1 ? heap2 : heap1;
  switch_to_heap(other);
  copy_roots();
}
