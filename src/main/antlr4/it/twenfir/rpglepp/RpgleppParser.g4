parser grammar RpgleppParser;

options { tokenVocab = RpgleppLexer; }

sourceFile : ( FULLY_FREE_DIR? EOL+ )? line ( eol line? )* endSource? eof;

line :	( prefix
        | prefix? statement
        )
        ;

statement : ( directive
            | instruction
            | comment
            | empty
            | sql
            ) 
            ;

prefix		:   ( LINE_NUMBER END_SOURCE_DIR?
                | LINE_NUMBER? ( END_SOURCE_DIR | STANDARD_PREFIX | BAD_PREFIX )
                ) 
                ;

directive : ( copy
            | define
            | undefine
            | if_
            | elseif
            | else_
            | endif
            | eofDir
            | eject
            | free
            | end_free
            )
            ( SPACE+ COMMENT? )?
            ;

copy		: ( COPY | INCLUDE ) SPACE member SPACE* ;

member      : ( ( NAME SLASH )? NAME COMMA )? NAME ;

define : DEFINE SPACE+ NAME ;

undefine : UNDEFINE SPACE+ NAME ;

if_ : IF SPACE+ condition ;

elseif : ELSEIF SPACE+ condition ;

condition : ( NOT SPACE+ )? DEFINED LPAR NAME RPAR ;
 
else_ : ELSE ;

endif : ENDIF ;

eofDir : EOF_DIR ;

eject : EJECT ;

free : FREE ;

end_free : END_FREE ;

instruction : ( INSTRUCTION | BAD_INSTRUCTION );

comment     : ( COMMENT | BAD_COMMENT ) ;

empty       : EMPTY ;

sql         : EXEC_SQL eol? prefix? SQL_STATEMENT ( eol prefix? SQL_STATEMENT )* eol? prefix? END_EXEC ;

endSource   : END_SOURCE+ ;

eol         : EOL ;
eof         : EOF ;
