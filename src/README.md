# Laboratory Work 3 - Lexer / Scanner

### Course: Formal Languages & Finite Automata
### Author: Gabriela Bîtca FAF-242

---

## Theory

A lexer (also called a scanner or tokenizer) is the first step in processing any programming language. It reads raw text character by character and groups those characters into meaningful units called tokens. Each token has a type (like NUMBER or PLUS) and a value (the actual text that was read).

The difference between a lexeme and a token is simple. A lexeme is the raw chunk of text you get after splitting the input, for example `3.14` or `sin`. A token gives that chunk a category and meaning. So `3.14` becomes `Token{type=NUMBER, value='3.14'}` and `sin` becomes `Token{type=SIN, value='sin'}`.

Lexers are used in compilers and interpreters as the very first processing stage before any parsing or execution happens.

---

## Objectives

* Understand what lexical analysis is.
* Get familiar with how a lexer works internally.
* Implement a lexer that handles identifiers, keywords, integers, floats, operators, and comments.
* Support trigonometric functions `sin` and `cos` and the `def` keyword.

---

## Implementation Description

### TokenType.java

This is a simple enum that lists every possible token category the lexer can produce. Having all types in one place makes the code clean and easy to extend later. If you ever want to add a new keyword you just add it here first.

The types are grouped by purpose. `EOF` signals the end of input so the main loop knows when to stop. `IDENTIFIER` and `NUMBER` are the general value types. `DEF`, `SIN`, and `COS` are specific keywords that get their own types so the parser can treat them differently later. The rest are all operators and punctuation symbols.

I decided to give `sin` and `cos` their own token types instead of just leaving them as identifiers. This makes it much easier down the line if you want to build a parser or evaluator on top of this lexer, because you can check the type directly without comparing strings.

```java
public enum TokenType {
    EOF,
    IDENTIFIER,
    NUMBER,
    DEF,
    SIN,
    COS,
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    ASSIGN,
    LPAREN,
    RPAREN,
    COMMA
}
```

---

### Token.java

This class holds one token. It stores the type and the actual string value together. The `toString()` method is useful for printing results during testing.

One small thing I noticed early on is that without a proper `toString()` debugging is painful because you just see object references in the console. Adding it right away saved a lot of time.

The class is intentionally simple. It does not do any logic, it just carries data. The `getType()` method is needed so the main loop in `Main.java` can check whether the current token is `EOF` and stop reading. The value is stored as a plain `String` for everything, whether it is a number like `3.14` or an operator like `+`. Converting the number to an actual numeric type is something a later stage like a parser would do.

```java
public class Token {

    private TokenType type;
    private String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
```

---

### Lexer.java

This is the main class. It reads the input string one character at a time using `advance()`. The `getNextToken()` method checks the current character and decides what kind of token to build next.

The `advance()` method is very small but it is the core of everything. It moves the position forward by one and updates `currentChar`. When the position goes past the end of the string it sets `currentChar` to `\0` which acts as a signal that there is nothing left to read. Every other method in the lexer calls `advance()` to move through the input.

Whitespace is just skipped entirely. The `skipWhitespace()` method keeps calling `advance()` as long as the current character is a space, tab, or newline. This means indentation and blank lines between expressions are all handled automatically without any special cases.

The trickiest part was handling float numbers. At first the `number()` method just kept reading digits and dots without any check. This meant something like `3.14.5` would be accepted as one valid token which is wrong. The fix was adding a `hasDot` boolean that prevents a second dot from being consumed into the same number. Before the fix I tested with `10.5` and it worked fine, but then I tried `1.1.2` and it produced one NUMBER token with value `1.1.2` which would cause problems in any real use. The boolean check was a simple one-line fix once I understood what was happening.

```java
private Token number() {
    StringBuilder result = new StringBuilder();
    boolean hasDot = false;

    while (Character.isDigit(currentChar) || (currentChar == '.' && !hasDot)) {
        if (currentChar == '.') {
            hasDot = true;
        }
        result.append(currentChar);
        advance();
    }

    return new Token(TokenType.NUMBER, result.toString());
}
```

For identifiers and keywords the lexer reads the whole word first and then checks what it is. This is simpler than trying to detect keywords letter by letter while reading. It also means adding new keywords later is very easy. I also added support for underscores in identifiers so names like `my_var` or `result_2` work correctly. Without that, the underscore would hit the `default` case in `getNextToken()` and throw an exception.

The keyword check uses a switch statement. At first I had a chain of `if` statements checking `word.equals("def")` and so on, which worked but looked messy. The switch version is cleaner and easier to read at a glance.

```java
private Token identifier() {
    StringBuilder result = new StringBuilder();

    while (Character.isLetterOrDigit(currentChar) || currentChar == '_') {
        result.append(currentChar);
        advance();
    }

    String word = result.toString();

    switch (word) {
        case "def": return new Token(TokenType.DEF, word);
        case "sin": return new Token(TokenType.SIN, word);
        case "cos": return new Token(TokenType.COS, word);
        default:    return new Token(TokenType.IDENTIFIER, word);
    }
}
```

Comments start with `#` and go until the end of the line. The `skipComment()` method just advances past everything until it hits a newline or end of input. The comment never becomes a token so it simply disappears from the output.

I chose `#` as the comment character because it is the same style used in Python. It felt natural for the kind of simple scripting language this lexer is built for. The method does not need to do anything with the text inside the comment, it just throws it away, which keeps the logic minimal.

```java
private void skipComment() {
    if (currentChar == '#') {
        while (currentChar != '\n' && currentChar != '\0') {
            advance();
        }
    }
}
```

---

### Main.java

This is the entry point. It creates a `Lexer` with a sample input string and prints every token until it hits `EOF`. The input covers all the features: function definitions, arithmetic, trigonometric calls, floats, and a comment line.

The loop uses a `do-while` so the first token is always fetched before the condition is checked. This makes sure `EOF` itself also gets printed, which is useful to confirm the lexer reached the end cleanly. Without that you would have to guess whether the loop ended because of `EOF` or some other reason.

The test input was designed to hit every token type at least once. The line `result = 10.5 - 1.1 / 2` tests floats, arithmetic operators, an identifier, and assignment all together. The `sin(3.14)` and `cos(0)` lines test keyword recognition and parentheses. The `def add(x, y)` line tests the `def` keyword, identifiers, and comma. The comment at the end confirms that lines starting with `#` are silently ignored.

```java
public class Main {

    public static void main(String[] args) {
        String input = """
                def add(x, y)
                x + y * 2
                sin(3.14)
                cos(0)
                result = 10.5 - 1.1 / 2
                # this is a comment
                """;

        System.out.println("Input");
        System.out.println(input);
        System.out.println("Tokens");

        Lexer lexer = new Lexer(input);
        Token token;

        do {
            token = lexer.getNextToken();
            System.out.println(token);
        } while (token.getType() != TokenType.EOF);
    }
}
```

---

## Conclusions / Screenshots / Results

```
Input
def add(x, y)
x + y * 2
sin(3.14)
cos(0)
result = 10.5 - 1.1 / 2
# this is a comment

Tokens
Token{type=DEF, value='def'}
Token{type=IDENTIFIER, value='add'}
Token{type=LPAREN, value='('}
Token{type=IDENTIFIER, value='x'}
Token{type=COMMA, value=','}
Token{type=IDENTIFIER, value='y'}
Token{type=RPAREN, value=')'}
Token{type=IDENTIFIER, value='x'}
Token{type=PLUS, value='+'}
Token{type=IDENTIFIER, value='y'}
Token{type=MULTIPLY, value='*'}
Token{type=NUMBER, value='2'}
Token{type=SIN, value='sin'}
Token{type=LPAREN, value='('}
Token{type=NUMBER, value='3.14'}
Token{type=RPAREN, value=')'}
Token{type=COS, value='cos'}
Token{type=LPAREN, value='('}
Token{type=NUMBER, value='0'}
Token{type=RPAREN, value=')'}
Token{type=IDENTIFIER, value='result'}
Token{type=ASSIGN, value='='}
Token{type=NUMBER, value='10.5'}
Token{type=MINUS, value='-'}
Token{type=NUMBER, value='1.1'}
Token{type=DIVIDE, value='/'}
Token{type=NUMBER, value='2'}
Token{type=EOF, value='EOF'}
```

Every token is correctly identified. The comment line is skipped and does not show up in the output at all. Floats like `3.14` and `10.5` are tokenized as single NUMBER tokens. Keywords `def`, `sin`, and `cos` are correctly separated from regular identifiers.

Building the lexer was straightforward once the structure was clear. The hardest part was the float parsing bug where multiple dots could end up inside one number token. Tracking whether a dot had already been seen fixed it cleanly. Another thing that needed attention was making sure keywords were only checked after reading the full word. The lexer now correctly handles all required cases including integers, floats, trig functions, arithmetic operators, assignment, and single-line comments.

---

## References

1. [LLVM Tutorial - My First Language Frontend](https://llvm.org/docs/tutorial/MyFirstLanguageFrontend/LangImpl01.html)
2. [Lexical Analysis - Wikipedia](https://en.wikipedia.org/wiki/Lexical_analysis)