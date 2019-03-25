import java_cup.runtime.*;

%%
/* ----------------- Options and Declarations Section----------------- */

/*
   The name of the class JFlex will create will be Scanner.
   Will write the code to the file Scanner.java.
*/
%class Scanner

/*
  The current line number can be accessed with the variable yyline
  and the current column number with the variable yycolumn.
*/
%line
%column

/*
   Will switch to a CUP compatibility mode to interface with a CUP
   generated parser.
*/
%cup
%unicode

/*
  Declarations

  Code between %{ and %}, both of which must be at the beginning of a
  line, will be copied letter to letter into the lexer class source.
  Here you declare member variables and functions that are used inside
  scanner actions.
*/

%{
    StringBuilder buffer = new StringBuilder();
    /**
        The following two methods create java_cup.runtime.Symbol objects
    **/
    private Symbol symbol(int type) {
       return new Symbol(type, yyline, yycolumn);
    }
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

/*
  Macro Declarations

  These declarations are regular expressions that will be used latter
  in the Lexical Rules Section.
*/

/* A line terminator is a \r (carriage return), \n (line feed), or
   \r\n. */
LineTerminator = \r|\n|\r\n

/* White space is a line terminator, space, tab, or line feed. */
WhiteSpace     = {LineTerminator} | [ \t\f]

Token          = [A-Za-z][A-Za-z0-9]*

/* A literal integer is is a number beginning with a number between
   one and nine followed by zero or more numbers between zero and nine
   or just a zero.  */

%state STRING
%state ESCAPE

%%
/* ------------------------Lexical Rules Section---------------------- */

<YYINITIAL> {
/* operators */
//  "+"              { return symbol(sym.PLUS); }                        //concatenation operator
  "("              { return symbol(sym.LPAREN); }                      //open parenthesis
  ")"              { return symbol(sym.RPAREN); }                      //close parenthesis
  "\""             { buffer.setLength(0); yybegin(STRING); }     //string
//  "{"              { return symbol(sym.LBRACKET); }                    //open bracket
//  "}"              { return symbol(sym.RBRACKET); }                    //close bracket
//  ","              { return symbol(sym.COMMA); }                       //comma
  "if"             { return symbol(sym.IF); }                          //if keyword
  "else"           { return symbol(sym.ELSE); }                        //else keyword
  "prefix"         { return symbol(sym.PREFIX); }                      //prefix keyword
  "suffix"         { return symbol(sym.SUFFIX); }                      //suffix keyword
  {WhiteSpace}     { /* just skip what was found, do nothing */ }
  {Token}          { return symbol(sym.TOKEN, new String(yytext())); }
}

<STRING> {
  "\""             { yybegin(YYINITIAL); return symbol(sym.STRING_LITERAL, buffer.toString()); }
  "\\"             { yybegin(ESCAPE); buffer.append("\\"); }
  [^'\"''\\']+     { buffer.append(yytext()); }
}

<ESCAPE> {
  "\""            { yybegin(STRING); buffer.append("\""); }
  "\\"            { yybegin(STRING); buffer.append("\\"); }
  "n"             { yybegin(STRING); buffer.append("n"); }
  "r"             { yybegin(STRING); buffer.append("r"); }
  "t"             { yybegin(STRING); buffer.append("t"); }
  "b"             { yybegin(STRING); buffer.append("b"); }
  "f"             { yybegin(STRING); buffer.append("f"); }
  "0"             { yybegin(STRING); buffer.append("0"); }
}

/* No token was found for the input so through an error.  Print out an
   Illegal character message with the illegal character that was found. */
[^]                    { throw new Error("Illegal character <"+yytext()+">"); }
