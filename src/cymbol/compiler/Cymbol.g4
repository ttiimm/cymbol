// Based on Parr's "Language Implementation Patterns"
// code/semantics/safety/Cymbol.g

grammar Cymbol;

@header {
package cymbol.compiler;
}


compilationUnit
	: (structDeclaration 
	| methodDeclaration 
	| varDeclaration)+
	;

structDeclaration
  : 'struct' ID '{' structMember+ '}'
  ;
  
structMember
  : type ID ';'
  | type ID array='[]' ';'
  | structDeclaration
  ;

methodDeclaration
  : type ID '(' formalParameters? ')' block
  ;

formalParameters
  : parameter (',' parameter)*
  ;
    
parameter
  : type ID 
  | type ID array='[]'
  ;

type 
  : primitiveType 
  | ID            
  ;

primitiveType 
  : 'float'  
  | 'int'    
  | 'char'
  | 'bool'
  | 'void'
  | 'String'
  ;

varDeclaration
  : type ID ('=' expr)? ';'
  | type ID array='[]' ('=' expr)? ';'
  ;

block 
  : '{' statement* '}'
  ;

statement
  : block                                            # stat_Block
  | structDeclaration                                # stat_StructDecl
  | varDeclaration                                   # stat_VarDecl
  | 'if' '(' expr ')' statement ('else' statement)?  # stat_Conditional
  | 'return' expr? ';'                               # stat_Return
  | expr '=' expr ';'                                # stat_Assign
  | expr ';'                                         # stat
  ;

expr
  : expr '(' ( expr (',' expr)* )? ')'                  # expr_Call              
  | expr '[' expr ']'                                   # expr_Array
  | expr o='.' expr                                     # expr_Member         
  | '-' expr                                            # expr_Unary
  | '!' expr                                            # expr_Unary
  | expr o=('*' | '/') expr                             # expr_Binary
  | expr o=('+' | '-') expr                             # expr_Binary
  | expr o=('!=' | '==' | '<' | '>' | '<=' | '>=') expr # expr_Binary
  | 'new' expr '(' (expr (',' expr)* )? ')'             # expr_New
  | primary                                             # expr_Primary 
  | '(' expr ')'                                        # expr_Group
  ;

primary
	: ID      # prim_Id
	| INT     # prim_Int
	| FLOAT   # prim
	| CHAR    # prim
	| 'true'  # prim
	| 'false' # prim
	| 'null'  # prim                
	| STRING  # prim_String
	;

// LEXER RULES

ID: LETTER (LETTER | '0'..'9' | '_')* ;

fragment
LETTER: ('a'..'z' | 'A'..'Z') ;

CHAR: '\'' . '\'' ;

INT: '0'..'9'+ ;
    
FLOAT
  : INT '.' INT*
  | '.' INT+
  ;
  
STRING: '"' ~( '"' | '\r' | '\n' )* '"'; 

WS  
  : (' '|'\r'|'\t'|'\n') -> skip
  ;

SL_COMMENT
  : '//' ~('\r'|'\n')* '\r'? '\n' -> skip
  ;
