# Laboratory Work 6 - Parser & Building an Abstract Syntax Tree

### Course: Formal Languages & Finite Automata
### Author: Gabriela Bîtca
### Group: FAF-242


## Theory

Parsing is the process of analyzing a sequence of tokens to determine its grammatical
structure according to a given formal grammar. Where the lexer breaks raw text into
tokens, the parser figures out how those tokens relate to each other. The result is
usually some kind of tree - either a parse tree (which closely mirrors the grammar
rules) or an **Abstract Syntax Tree (AST)**, which keeps only the semantically
meaningful structure and throws away the noise like parentheses or commas.

An AST is organized in layers of abstraction. Each node represents a construct in the
language: a function definition, an assignment, a binary operation, a call, a literal.
The tree shape makes it easy for later stages - interpreters, compilers, static
analyzers - to walk the code in a structured way without having to re-read the raw text.


## Objectives

1. Get familiar with parsing and how it can be programmed.
2. Get familiar with the concept of AST.
3. Extend the Lab 3 lexer work by:
    1. Ensuring `TokenType` exists and categorizes all tokens (already done in Lab 3).
    2. Implementing the necessary data structures for an AST.
    3. Implementing a simple parser that extracts syntactic information from the input.


## Implementation Description

### ASTNode

`ASTNode` is an abstract base class. Every node type in the tree extends it and
overrides `label()` (what to print) and optionally `children()` (the subtrees below).
There is also a `print()` method on the base class that walks the tree recursively and
draws it with box-drawing characters so the hierarchy is visible in the console.

The concrete node types are defined as static inner classes:

```java
public abstract class ASTNode {
    public abstract String label();
    public java.util.List<ASTNode> children() { return Collections.emptyList(); }
    public void print(String indent, boolean isLast) { ... }

    public static class Program      extends ASTNode { ... } // root: list of statements
    public static class FunctionDef  extends ASTNode { ... } // def add(x, y)
    public static class Assignment   extends ASTNode { ... } // result = expr
    public static class FunctionCall extends ASTNode { ... } // sin(3.14)
    public static class BinaryOp     extends ASTNode { ... } // left OP right
    public static class NumberLiteral extends ASTNode { ... } // 3.14
    public static class Identifier   extends ASTNode { ... } // x, y, result
}
```

Keeping all node types in one file made it easier to see the full shape of the tree at
a glance. The main difficulty was deciding what counted as a "statement" versus an
"expression" - `def` is clearly a statement, but a bare `sin(3.14)` on its own line
is an expression used as a statement, so the parser needed to handle both cases.

### Parser

The `Parser` takes the flat list of tokens from the `Lexer` and builds the AST using
**recursive descent**. Each grammar rule maps directly to a method. The informal
grammar is:

```
program    -> statement*
statement  -> funcDef | assignment | expr
funcDef    -> DEF IDENTIFIER LPAREN paramList RPAREN
assignment -> IDENTIFIER ASSIGN expr
expr       -> term ((PLUS | MINUS) term)*
term       -> factor ((MULTIPLY | DIVIDE) factor)*
factor     -> NUMBER | IDENTIFIER | funcCall | LPAREN expr RPAREN
funcCall   -> (SIN | COS | IDENTIFIER) LPAREN argList RPAREN
```

Operator precedence is encoded by the nesting: `expr` handles `+` and `-`, `term`
handles `*` and `/`, and `factor` handles the atoms. This means `*` and `/` naturally
bind tighter than `+` and `-` without any extra logic.

```java
private ASTNode parseExpr() {
    ASTNode left = parseTerm();
    while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
        String op = consume().getValue();
        ASTNode right = parseTerm();
        left = new ASTNode.BinaryOp(op, left, right);
    }
    return left;
}

private ASTNode parseTerm() {
    ASTNode left = parseFactor();
    while (check(TokenType.MULTIPLY) || check(TokenType.DIVIDE)) {
        String op = consume().getValue();
        ASTNode right = parseFactor();
        left = new ASTNode.BinaryOp(op, left, right);
    }
    return left;
}
```

The trickiest part was distinguishing between an assignment (`result = ...`) and a
plain identifier expression at the `parseStatement` level, without consuming any
tokens. The fix was a two-token lookahead: if the current token is an `IDENTIFIER`
and the next one is `ASSIGN`, it is an assignment; otherwise it falls through to
`parseExpr`.

```java
private ASTNode parseStatement() {
    if (check(TokenType.DEF)) return parseFunctionDef();
    if (check(TokenType.IDENTIFIER) && lookahead(1) == TokenType.ASSIGN)
        return parseAssignment();
    return parseExpr();
}
```

Similarly, inside `parseFactor`, a name followed by `(` is a function call, while the
same name alone is just a variable reference. One lookahead was enough to split those
two paths cleanly.

### Main

`Main` first runs the `Lexer` and collects all tokens into a list, then passes that
list to the `Parser`. The resulting `Program` node is printed with the tree-drawing
helper, showing the full hierarchy of every statement.

```java
Lexer lexer = new Lexer(input);
List<Token> tokens = new ArrayList<>();
Token token;
do {
    token = lexer.getNextToken();
    tokens.add(token);
} while (token.getType() != TokenType.EOF);

Parser parser = new Parser(tokens);
ASTNode.Program program = parser.parse();
```


## Conclusions / Results

The parser correctly handles all five constructs in the input: function definitions,
binary expressions with proper precedence, function calls, and assignments. The output
is:

```
Input:
def add(x, y)
x + y * 2
sin(3.14)
cos(0)
result = 10.5 - 1.1 / 2
# this is a comment

Tokens:
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

AST:
└── Program
    ├── FunctionDef: add(x, y)
    ├── BinaryOp: +
    │   ├── Identifier: x
    │   └── BinaryOp: *
    │       ├── Identifier: y
    │       └── Number: 2
    ├── Call: sin
    │   └── Number: 3.14
    ├── Call: cos
    │   └── Number: 0
    └── Assign: result
        └── BinaryOp: -
            ├── Number: 10.5
            └── BinaryOp: /
                ├── Number: 1.1
                └── Number: 2
```

The tree shows that `x + y * 2` correctly parses `*` with higher precedence than `+`,
and that `10.5 - 1.1 / 2` correctly groups `/` before `-`. The comment line is
silently skipped by the lexer and never reaches the parser. The main takeaway from
this lab is that recursive descent is a very natural fit for expression parsing - once
you get the precedence levels right as nested method calls, most of the grammar just
falls into place.