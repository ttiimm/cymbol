// Based on Parr's "Language Implementation Patterns"
// code/semantics/safety/Cymbol.g

grammar Cymbol;

@header {
package cymbol.compiler;

import cymbol.symtab.Scope;
import cymbol.symtab.Type;
import cymbol.symtab.Symbol;
import cymbol.compiler.CymbolProperties;
}

@lexer::header {
package cymbol.compiler;
}

compilationUnit
  locals[CymbolProperties props]
  @init{$compilationUnit.props = new CymbolProperties();}
	: (structDeclaration 
	| methodDeclaration 
	| varDeclaration)+
	;

structDeclaration
  locals[CymbolProperties props]
  @init{$structDeclaration.props = new CymbolProperties();}
  : 'struct' ID '{' structMember+ '}'
  ;
  
structMember
  locals[CymbolProperties props]
  @init{$structMember.props = new CymbolProperties();}
  : type ID ';'
  | type ID '[]' ';'
  | structDeclaration
  ;

methodDeclaration
  locals[CymbolProperties props]
  @init{$methodDeclaration.props = new CymbolProperties();}
  : type ID '(' formalParameters? ')' block
  ;

formalParameters
  : parameter (',' parameter)*
  ;
    
parameter
  locals[CymbolProperties props]
  @init{$parameter.props = new CymbolProperties();}
  : type ID 
  | type ID '[]'
  ;

type 
  returns [CymbolProperties props]
  @init{$type.props = new CymbolProperties();}
  : primitiveType 
  | ID            
  ;

primitiveType 
  returns [CymbolProperties props]
  @init{$primitiveType.props = new CymbolProperties();}
  : 'float'  
  | 'int'    
  | 'char'
  | 'boolean'
  | 'void' 
  ;

varDeclaration
  returns [CymbolProperties props]
  @init{$varDeclaration.props = new CymbolProperties();}
  : type ID ('=' expr)? ';'
  | type ID '[]' ('=' expr)? ';'
  ;

block 
  returns [CymbolProperties props]
  @init{$block.props = new CymbolProperties();}
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
  returns [CymbolProperties props]
  @init{$expr.props = new CymbolProperties();}
  : expr '(' ( expr (',' expr)* )? ')'                -> expression              
  | expr '[' expr ']'                                 -> expression
  | expr '.' expr                                     -> expression         
  | '-' expr                                          -> expression
  | '!' expr                                          -> expression
  | expr ('*' | '/') expr                             -> expression
  | expr ('+' | '-') expr                             -> expression
  | expr ('!=' | '==' | '<' | '>' | '<=' | '>=') expr -> expression
  | primary                                           -> expr_Primary 
  | '(' expr ')'                                      -> expression
  ;

primary
  returns [CymbolProperties props]
  @init{$primary.props = new CymbolProperties();}
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
