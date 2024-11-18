
parser grammar AbacusParser;
options { tokenVocab=AbacusLexer; }


additiveExpression
    : multiplicativeExpression (('+' | '-')  multiplicativeExpression)*
    ;

multiplicativeExpression
    : unaryExpression (('*' | '/')  unaryExpression)*
    ;

unaryExpression
    : primaryExpression
    | arithmeticExpression
    ;

arithmeticExpression
    :  arithmeticOperator unaryExpression
    ;

arithmeticOperator: PLUS | MINUS | TIMES | DIV;

primaryExpression
    : scientific
    | variable
    | parenthesisExpression
    ;

parenthesisExpression
    : OPEN_PARENS additiveExpression CLOSE_PARENS
    ;

scientific
    : SCIENTIFIC_NUMBER
    ;

variable
    : VARIABLE
    ;

