// Based on Parr's "Language Implementation Patterns"
// code/semantics/safety/Cymbol.g

grammar Cymbol;

@header {
package cymbol.compiler;

import cymbol.symtab.Scope;
import cymbol.symtab.Type;
import cymbol.symtab.Symbol;
}

@lexer::header {
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
  | type ID '[]' ';'
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
  | type ID '[]'
  ;

type 
  : primitiveType 
  | ID            
  ;

primitiveType 
  : 'float'  
  | 'int'    
  | 'char'
  | 'boolean'
  | 'void' 
  ;

varDeclaration
  : type ID ('=' expr)? ';'
  | type ID '[]' ('=' expr)? ';'
  ;

block 
  : '{' statement* '}'
  ;

statement
  : block                                            -> stat_Block
  | structDeclaration                                -> stat_StructDecl
  | varDeclaration                                   -> stat_VarDecl
  | 'if' '(' expr ')' statement ('else' statement)?  -> stat_Conditional
  | 'return' expr? ';'                               -> stat_Return
  | expr '=' expr ';'                                -> stat_Assign
  | expr ';'                                         -> stat
  ;

expr
  : expr '(' ( expr (',' expr)* )? ')'                  -> expr_Call              
  | expr '[' expr ']'                                   -> expr_Array
  | expr o='.' expr                                     -> expr_Member         
  | '-' expr                                            -> expr_Unary
  | '!' expr                                            -> expr_Unary
  | expr o=('*' | '/') expr                             -> expr_Binary
  | expr o=('+' | '-') expr                             -> expr_Binary
  | expr o=('!=' | '==' | '<' | '>' | '<=' | '>=') expr -> expr_Binary
  | primary                                             -> expr_Primary 
  | '(' expr ')'                                        -> expr_Group
  ;

primary
	: ID      
	| INT     
	| FLOAT   
	| CHAR    
	| 'true'
	| 'false'
	;

// LEXER RULES

ID: LETTER (LETTER | '0'..'9')* ;

fragment
LETTER: ('a'..'z' | 'A'..'Z') ;

CHAR: '\'' . '\'' ;

INT: '0'..'9'+ ;
    
FLOAT
  : INT '.' INT*
  | '.' INT+
  ;

WS  
  : (' '|'\r'|'\t'|'\n') {$channel=HIDDEN;}
  ;

SL_COMMENT
  : '//' ~('\r'|'\n')* '\r'? '\n' {$channel=HIDDEN;}
  ;
