// From Parr's "Language Implementation Patterns"
// code/semantics/safety/Cymbol.g

grammar Cymbol;

@header {
package cymbol.compiler;

import cymbol.symtab.Scope;
import cymbol.symtab.Type;
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
  : t=type name=ID ';'
  | t=type name=ID '[]' ';'
  | structDeclaration
  ;

methodDeclaration
  : ret=type name=ID '(' formalParameters? ')' block
  ;

formalParameters
  : parameter (',' parameter)*
  ;
    
parameter
  : t=type name=ID 
  | t=type name=ID '[]'
  ;

type 
  locals [Type type, String t]
  : primitiveType { $type.t = $primitiveType.t; } 
  | ID            { $type.t = $ID.getText(); }
  ;

primitiveType 
  returns [String t]
  : 'float'  {$primitiveType.t = "float"; }
  | 'int'    {$primitiveType.t = "int"; }
  | 'char'   {$primitiveType.t = "char"; }
  | 'boolean'{$primitiveType.t = "boolean"; }
  | 'void'   {$primitiveType.t = "void"; }
  ;

varDeclaration
  : t=type name=ID ('=' e=expr)? ';'
  | t=type name=ID '[]' ('=' e=expr)? ';'
  ;

block 
  locals [Scope scope]  
  : '{' statement* '}'
  ;

statement
  : block
  | structDeclaration
  | varDeclaration
  | 'if' '(' expr ')' statement ('else' statement)?
  | 'return' expr? ';'
  | expr '=' expr ';'
  | expr ';' 
  ;

expr
  : expr '(' expressionList? ')'
  | expr '[' expr ']'
  | expr '.' expr
  | '-' expr
  | '!' expr
  | expr ('*' | '/') expr
  | expr ('+' | '-') expr
  | expr ('!=' | '==' | '<' | '>' | '<=' | '>=') expr
  | primary
  | '(' expr ')'
  ;

expressionList
  : expr (',' expr)*
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