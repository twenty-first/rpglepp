parser grammar RpgleppParser;

options { tokenVocab = RpgleppLexer; }

sourceFile : ( FULLY_FREE_DIR? EOL+ )? line ( eol line? )* endSource? eof;

line :	( prefix
        | prefix? statement
        | PART_PREFIX
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
            | space
            | eject
            | free
            | end_free
            )
            ( WHITESPACE+ COMMENT? )?
            ;

copy		: ( COPY | INCLUDE ) WHITESPACE member WHITESPACE* ;

member      : ( ( NAME SLASH )? NAME COMMA )? NAME ;

define : DEFINE WHITESPACE+ NAME ;

undefine : UNDEFINE WHITESPACE+ NAME ;

if_ : IF WHITESPACE+ condition ;

elseif : ELSEIF WHITESPACE+ condition ;

condition : ( NOT WHITESPACE+ )? DEFINED LPAR NAME RPAR ;
 
else_ : ELSE ;

endif : ENDIF ;

eofDir : EOF_DIR ;

space : SPACE ;

eject : EJECT ;

free : FREE ;

end_free : END_FREE ;

instruction : ( INSTRUCTION | BAD_INSTRUCTION );

comment     : ( COMMENT | BAD_COMMENT ) ;

empty       : EMPTY ;

sql   : ( EXEC_SQL
        | SQL_STATEMENT
        | END_EXEC
        ) 
        ;

endSource   : END_SOURCE+ ;

eol         : EOL ;
eof         : EOF ;
