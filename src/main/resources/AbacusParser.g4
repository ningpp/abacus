
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
    : OPEN_PARENS conditionalExpression CLOSE_PARENS
    ;

scientific
    : SCIENTIFIC_NUMBER
    ;

variable
    : VARIABLE
    ;


conditionalExpression
    : conditionalOrExpression
    | conditionalCondition '?' conditionalThen ':' conditionalElse
    ;

conditionalCondition:     conditionalOrExpression;
conditionalThen:          conditionalExpression;
conditionalElse:          conditionalExpression;


conditionalOrExpression
    : conditionalAndExpression
    | conditionalOrExpression '||' conditionalAndExpression
    ;

conditionalAndExpression
    : equalityExpression
    | conditionalAndExpression '&&' equalityExpression
    ;

equalityExpression
    : relationalExpression
    | equalityExpression '==' relationalExpression
    | equalityExpression '!=' relationalExpression
    ;

relationalExpression
    : additiveExpression
    | relationalExpression '<' additiveExpression
    | relationalExpression '>' additiveExpression
    | relationalExpression '<=' additiveExpression
    | relationalExpression '>=' additiveExpression
    ;
