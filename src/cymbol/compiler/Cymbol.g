// From Parr's "Language Implementation Patterns"
// code/semantics/safety/Cymbol.g

grammar Cymbol;

@header {
package cymbol.compiler;

import cymbol.symtab.Scope;
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
  : 'struct' name=ID '{' structMember+ '}'
  ;
  
structMember
  : type name=ID ';'
  | type name=ID '[]' ';'
  | structDeclaration
  ;

methodDeclaration
  :   ret=type name=ID '(' formalParameters? ')' block
  ;

formalParameters
  :   parameter (',' parameter)*
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
  : type name=ID ('=' expression)? ';'
  | type name=ID '[]' ('=' expression)? ';'
  ;

block 
  locals [Scope scope]  
  : '{' statement* '}'
  ;

statement
  : block
  | structDeclaration
  | varDeclaration
  | 'if' '(' expression ')' statement ('else' statement)?
  | 'return' expression? ';'
  | lhs '=' expression ';'
  | postfixExpression ';' 
  ;

lhs 
  : postfixExpression
  ;
  
expression
  :   expr
  ;

expr
  : equalityExpression
  ;
  
equalityExpression
  : relationalExpression (('!=' | '==') relationalExpression)*
  ;

relationalExpression
  : additiveExpression
    ( ( ( '<'
        | '>'
        | '<='
        | '>='
        )
        additiveExpression
      )*
    )
  ;

additiveExpression
  : multiplicativeExpression (('+' | '-') multiplicativeExpression)*
  ;

multiplicativeExpression
  : unaryExpression (('*' | '/') unaryExpression)*
  ;

unaryExpression
  : '-' unaryExpression
  | '!' unaryExpression
  | postfixExpression
  ;

postfixExpression
  :   primary
    (
      ( '(' expressionList? ')'
      | '[' expr ']'
      | '.' ID
      )
    )*
  ;

expressionList
  :   expr (',' expr)*
  ;

primary
	: ID
	| INT
	| FLOAT
	| CHAR
	| 'true'
	| 'false'
	| '(' expression ')'
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