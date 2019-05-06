## Part 2 - A translator to Java for a language for string operations
In the second part of this homework you will implement a parser and translator for a language supporting string operations. The language supports the concatenation operator over strings, function definitions and calls, conditionals (if-else i.e, every "if" must be followed by an "else"), and the following logical expressions:

* is-prefix-of (string1 prefix string2): Whether string1 is a prefix of string2.
* is-suffix-of (string1 suffix string2): Whether string1 is a suffix of string2.
All values in the language are strings.

Your parser, based on a context-free grammar, will translate the input language into Java. You will use JavaCUP for the generation of the parser combined either with a hand-written lexer or a generated-one (e.g., using JFlex, which is encouraged).

You will infer the desired syntax of the input and output languages from the examples below. The output language is a subset of Java so it can be compiled using the "javac" command and executed using the "java" command or online Java compilers like [this](http://repl.it/languages/java), if you want to test your output.

There is no need to perform type checking for the argument types or a check for the number of function arguments. You can assume that the program input will always be semantically correct.

Note that each file of Java source code you produce must have the same name as the public Java class in it. For your own convenience you can name the public class "Main" and the generated files "Main.java". In order to compile a file named Main.java you need to execute the command: javac Main.java. In order to execute the produced Main.class file you need to execute: java Main.

To execute the program successfully, the "Main" class of your Java program must have a method with the following signature: public static void main(String[] args), which will be the main method of your program, containing all the translated statements of the input program. Moreover, for each function declaration of the input program, the translated Java program must contain an equivalent static method of the same name. Finaly, keep in mind that in the input language the function declations must precede all statements.

#### Example #1
Input:
```java
    name()  {
        "John"
    }
    
    surname() {
        "Doe"
    }
    
    fullname(first_name, sep, last_name) {
        first_name + sep + last_name
    }

    name()
    surname()
    fullname(name(), " ", surname())
```
Output (Java):
```java
public class Main {
    public static void main(String[] args) {
        System.out.println(name());
        System.out.println(surname());
        System.out.println(fullname(name(), " ", surname()));
    }

    public static String name() {
        return "John";
    }

    public static String surname() {
        return "Doe";
    }

    public static String fullname(String firstname, String sep, String last_name) {
        return first_name + sep + last_name;
    }
}
```
#### Example #2
Input:
```java
    name() {
        "John"
    }

    repeat(x) {
        x + x
    }

    cond_repeat(c, x) {
        if (c prefix "yes")
            if("yes" prefix c)
                repeat(x)
            else
                x
        else
            x
    }

    cond_repeat("yes", name())
    cond_repeat("no", "Jane")
```
#### Example #3
Input:
```java
    findLangType(langName) {
        if ("Java" prefix langName)
            if(langName prefix "Java")
                "Static"
            else
                if("script" suffix langName)
                    "Dynamic"
                else
                    "Unknown"
        else
            if ("script" suffix langName)
                "Probably Dynamic"
            else
                "Unknown"
    }

    findLangType("Java")
    findLangType("Javascript")
    findLangType("Typescript")
```
