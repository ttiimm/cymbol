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
  : 'struct' name=ID '{' structMember+ '}'
  ;
  
structMember
  locals[Scope scope]
  : t=type name=ID ';'
  | t=type name=ID '[]' ';'
  | structDeclaration
  ;

methodDeclaration
  locals[Symbol method]
  : ret=type name=ID '(' formalParameters? ')' block
  ;

formalParameters
  : parameter (',' parameter)*
  ;
    
parameter
  locals[Scope scope]
  : t=type name=ID 
  | t=type name=ID '[]'
  ;

type 
  locals [Type type, String t, Scope scope, Symbol method]
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
  locals [Scope scope]
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
  locals [List<Type> types]
  : e1=expr '(' ( expr (',' expr)* )? ')'
  | e1=expr '[' e2=expr ']'
  | e1=expr '.' member=expr
  | '-' e1=expr
  | '!' e1=expr
  | e1=expr ('*' | '/') e2=expr
  | e1=expr ('+' | '-') e2=expr
  | e1=expr ('!=' | '==' | '<' | '>' | '<=' | '>=') e2=expr
  | p=primary
  | '(' e1=expr ')'
  ;

primary
  returns [Scope scope, Type type]
	: id=ID
	| i=INT
	| f=FLOAT
	| c=CHAR
	| bool='true'
	| bool='false'
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