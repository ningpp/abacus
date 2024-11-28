
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
    | invocationExpression
    ;

parenthesisExpression
    : '(' expression ')'
    ;

scientific
    : SCIENTIFIC_NUMBER
    ;

variable
    : VARIABLE
    ;


expression
    : invocationExpression
    | conditionalExpression
    ;

invocationExpression: methodName '(' argumentList? ')';

methodName
    : VARIABLE
    ;

argumentList
    : expression (',' expression)*
    ;


conditionalExpression
    : conditionalOrExpression
    | conditionalCondition '?' conditionalThen ':' conditionalElse
    ;

conditionalCondition:     conditionalOrExpression;
conditionalThen:          expression;
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
