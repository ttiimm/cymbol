#include "types-usr.h"

typedef struct Object {
  TypeDescriptor *type;
  /* address of obj after copy */
  byte *forward;
} Object;


/**
 * Root management
 */
int _rp;
Object **_roots[100];

#define GC_SAVE_RP			int __rp = _rp;
#define ADD_ROOT(p)	        _roots[_rp++] = (Object **)(&(p));
#define GC_RESTORE_RP       _rp = __rp;


/**
 * Helpers to inspect the heap, mostly for testing.
 */
bool in_heap(Object *p);
byte *heap_address();
int heap_free();
int heap_allocated();
void heap_dump(char *buf);


/**
 * Allocation
 */
Object *alloc(TypeDescriptor *type);
Array  *alloc_array(int len, int type);
String *alloc_String(int len);
int align(int size);
int sizeof_array(int len, int type);


/**
 * Garbage Collection
 */
bool gc_init(int heap_size);
void gc();
