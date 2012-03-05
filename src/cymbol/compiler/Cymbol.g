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
  : block                                            -> stat
  | structDeclaration                                -> stat
  | varDeclaration                                   -> stat_VarDecl
  | 'if' '(' expr ')' statement ('else' statement)?  -> stat
  | 'return' expr? ';'                               -> stat
  | expr '=' expr ';'                                -> stat
  | expr ';'                                         -> stat
  ;

expr
  : expr '(' ( expr (',' expr)* )? ')'                -> expression              
  | expr '[' expr ']'                                 -> expression
  | expr '.' expr                                     -> expression         
  | '-' expr                                          -> expr_Unary
  | '!' expr                                          -> expression
  | expr ('*' | '/') expr                             -> expr_Binary
  | expr ('+' | '-') expr                             -> expr_Binary
  | expr ('!=' | '==' | '<' | '>' | '<=' | '>=') expr -> expr_Binary
  | primary                                           -> expr_Primary 
  | '(' expr ')'                                      -> expression
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
