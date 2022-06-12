lexer grammar RpgleppLexer;

tokens { 
    BAD_COMMENT,
    BAD_INSTRUCTION,
    BAD_PREFIX,
    COMMENT,
    EOL,
    EMPTY,
    END_EXEC,
    END_SOURCE,
    EXEC_SQL,
    INSTRUCTION,
    LINE_NUMBER,
    SPACE,
    SQL_STATEMENT,
    STANDARD_PREFIX
}


FULLY_FREE_DIR  : '**'[Ff][Rr][Ee][Ee] -> pushMode(FullyFree) ;

END_SOURCE_DIR  : '**' ANY_F* -> mode(EndSource) ;

PFX_LINE_NUMBER     : LNUM_F -> type(LINE_NUMBER) ;

PFX_STANDARD_PREFIX : PREF_F -> type(STANDARD_PREFIX), pushMode(Fixed) ;

PFX_BAD_PREFIX : BAD_PREF_F -> type(BAD_PREFIX), pushMode(Fixed) ;

PFX_EOL         : PART_PREFIX_F? EOL_F -> type(EOL) ;

fragment PART_PREFIX_F : ~[\r\n*] ~[\r\n*]? ANY_F? ANY_F? ;

// Common fragments

fragment LNUM_F     : [0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9] ;
fragment PREF_F     : ANY_F ANY_F ANY_F ANY_F ANY_F ;
fragment BAD_PREF_F : BAD_F BAD_F BAD_F BAD_F BAD_F ;
fragment EOL_F      : '\r'? '\n' ;
fragment ANY_F      : ~[\r\n\u0082\u00A3\u00A7\u00C2] ;
fragment BAD_F      : ~[\r\n] ;
fragment SPACE_F    : [ \t] ;
fragment SPEC_F		: [CcDdFfHhIiOoPp] ;
fragment EXEC_SQL_F : [Ee][Xx][Ee][Cc] SPACE_F+ [Ss][Qq][Ll] ;
fragment END_EXEC_F : [Ee][Nn][Dd]'-'[Ee][Xx][Ee][Cc] ;


mode Fixed;

FX_DIR_PREF : ANY_F '/' -> channel(HIDDEN), popMode, pushMode(Directive) ;

FX_EXEC_SQL : [Cc] '/' EXEC_SQL_F -> type(EXEC_SQL), popMode, pushMode(SqlFixed) ;

FX_COMMENT : ( ANY_F '*' | SPACE_F* '//' ) ANY_F* -> type(COMMENT);

FX_BAD_COMMENT : ( BAD_F BAD_F? '*' | SPACE_F* '//' ) BAD_F* -> type(BAD_COMMENT);

FX_INSTRUCTION : SPEC_F ~[/*\r\n] ANY_F+ -> type(INSTRUCTION);

FX_BAD_INSTRUCTION : SPEC_F ~[/*\r\n] BAD_F+ -> type(BAD_INSTRUCTION);

FX_EMPTY : SPEC_F -> type(EMPTY);

FX_EOL : EOL_F -> type(EOL), popMode ;

FX_FREE_PREF : SPACE_F SPACE_F -> channel(HIDDEN), popMode, pushMode(Free) ;


mode Free;

FR_DIR_PREF : SPACE_F* '/' -> channel(HIDDEN), popMode, pushMode(Directive) ;

FR_COMMENT : SPACE_F* '//' ANY_F* -> type(COMMENT);

FR_BAD_COMMENT : SPACE_F* '//' BAD_F+ -> type(BAD_COMMENT);

FR_INSTRUCTION : SPACE_F* ~[/\r\n] ANY_F+ -> type(INSTRUCTION);

FR_BAD_INSTRUCTION : SPACE_F* ~[/\r\n] BAD_F+ -> type(BAD_INSTRUCTION);

FR_EMPTY : SPACE_F+ -> type(EMPTY) ;

FR_EOL : EOL_F -> type(EOL), popMode ;


mode FullyFree;

FF_DIR_PREF : '/' -> channel(HIDDEN), pushMode(Directive) ;

FF_COMMENT : '*' ANY_F* -> type(COMMENT);

FF_BAD_COMMENT : '*' BAD_F+ -> type(BAD_COMMENT);

FF_INSTRUCTION : ~[*/] ANY_F+ -> type(INSTRUCTION);

FF_BAD_INSTRUCTION : ~[*/] BAD_F+ -> type(BAD_INSTRUCTION);

FF_EOL : EOL_F -> type(EOL) ;


mode Directive;

COPY        : [Cc][Oo][Pp][Yy] ;
DEFINE      : [Dd][Ee][Ff][Ii][Nn][Ee] ;
DEFINED     : [Dd][Ee][Ff][Ii][Nn][Ee][Dd] ;
EJECT       : [Ee][Jj][Ee][Cc][Tt] ;
ELSE        : [Ee][Ll][Ss][Ee] ;
ELSEIF      : [Ee][Ll][Ss][Ee][Ii][Ff] ;
ENDIF       : [Ee][Nn][Dd][Ii][Ff] ;
EOF_DIR     : [Ee][Oo][Ff] ;
FREE        : [Ff][Rr][Ee][Ee] -> popMode ;
END_FREE    : [Ee][Nn][Dd]'-'[Ff][Rr][Ee][Ee] -> popMode ;
IF          : [Ii][Ff] ;
INCLUDE     : [Ii][Nn][Cc][Ll][Uu][Dd][Ee] ;
NOT         : [Nn][Oo][Tt] ;
UNDEFINE    : [Uu][Nn][Dd][Ee][Ff][Ii][Nn][Ee] ;

EXEC_SQL    : EXEC_SQL_F -> popMode, pushMode(SqlFree);

NAME        : NAME_START NAME_CONT* ;

LPAR        : '(' ;
RPAR        : ')' ;
SLASH       : '/' ;
COMMA       : ',' ;

DR_SPACE    : SPACE_F -> type(SPACE) ;
DR_EOL      : EOL_F -> type(EOL), popMode ;

fragment NAME_START : [A-Za-z$#@*] ;
fragment NAME_CONT  : [A-Za-z0-9_$#@] ;


mode SqlFixed;

SQX_END_EXEC : [Cc] '/' END_EXEC_F -> type(END_EXEC), popMode ;

SQX_STATEMENT   : [Cc ] '+' ANY_F* -> type(SQL_STATEMENT) ;

SQX_EOL         : PART_PREFIX_F? EOL_F -> type(EOL), pushMode(SqlPrefix) ;


mode SqlPrefix;

SQX_LINE_NUMBER     : LNUM_F -> type(LINE_NUMBER) ;

SQX_STANDARD_PREFIX : PREF_F -> popMode, type(STANDARD_PREFIX) ;

SQX_BAD_PREFIX : BAD_PREF_F -> popMode, type(BAD_PREFIX) ;


mode SqlFree;

SQE_STATEMENT : ~[\r\n;]+ -> type(SQL_STATEMENT) ;

SQE_END_EXE   : ';' -> type(END_EXEC), popMode ;


mode EndSource;

END_SOURCE : EOL_F | BAD_F+ EOL_F? ;
