#include "types.h"

#define MAX_HEAP_SIZE 512 /* bytes */

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
int heap_size();
void heap_dump(char *buf);


/**
 * Allocation
 */
int align(int size);
Object *alloc(TypeDescriptor *type);
PrimitiveArray *alloc_primarray(int len);
String *alloc_string(int len);


/**
 * Garbage Collection
 */
bool gc_init();
void gc();
