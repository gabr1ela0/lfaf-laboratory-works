# Laboratory Work 4 – Regular Expressions

### Course: Formal Languages & Finite Automata
### Author: Gabriela Bîtca
### Group: FAF-242


## Theory

Regular expressions are patterns used to describe sets of strings.
They are built from literal characters and operators like alternation (`|`),
repetition (`*`, `+`), and grouping (`()`). A regex engine reads these patterns
and can either match them against existing text, or, as in this lab, generate
valid strings that conform to the pattern. Regular expressions are used everywhere:
input validation, search engines, compilers, and text processing tools all rely on them.

## Objectives

1. Write and cover what regular expressions are and what they are used for.
2. Write a program that dynamically generates valid strings from given regular expressions.
3. Limit unbounded repetitions (`*`, `+`) to a maximum of 5 to avoid extremely long outputs.
4. **Bonus:** Write a function that shows the step-by-step sequence of how the regex is processed.

## Variant 4 – Regular Expressions
```
(S|T)(U|V)W*Y+24
L(M|N)D³P*Q(2|3)
R*S(T|U|V)W(X|Y|Z)²
```

## Implementation description

### RegexNode (AST)

The program represents a parsed regex as a tree of `RegexNode` objects.
There are four node types: `LITERAL`, `CONCAT`, `ALTERNATION`, and `REPEAT`.
This separation makes it easy to handle each construct independently during generation.
```java
enum NodeType { LITERAL, CONCAT, ALTERNATION, REPEAT }

static class RegexNode {
    NodeType        type;
    String          literal;
    List<RegexNode> children = new ArrayList<>();
    int             minRep, maxRep;
    // ...
}
```

### Parser

The parser is a recursive-descent parser that reads the regex character by character
and builds the AST. One difficulty was handling Unicode superscript characters (`²`, `³`)
since they are not standard ASCII — they had to be detected separately using their
Unicode code points (`\u00B2`, `\u00B3`). Another challenge was making sure operator
precedence was respected: alternation (`|`) has lower priority than concatenation,
which in turn is lower than quantifiers (`*`, `+`, superscripts).
```java
// Level 1 – alternation
RegexNode parseExpr() {
    List<RegexNode> alts = new ArrayList<>();
    alts.add(parseConcat());
    while (pos < src.length && src[pos] == '|') {
        pos++;
        alts.add(parseConcat());
    }
    if (alts.size() == 1) return alts.get(0);
    return RegexNode.alt(alts);
}

// Level 2 – concatenation
RegexNode parseConcat() {
    List<RegexNode> seq = new ArrayList<>();
    while (pos < src.length && src[pos] != ')' && src[pos] != '|') {
        seq.add(parseQuantified());
    }
    if (seq.size() == 1) return seq.get(0);
    return RegexNode.concat(seq);
}

// Level 3 – quantifiers
RegexNode parseQuantified() {
    RegexNode atom = parseAtom();
    if (pos < src.length) {
        char q = src[pos];
        if (q == '*') { pos++; return RegexNode.repeat(atom, 0, MAX_REPEAT); }
        if (q == '+') { pos++; return RegexNode.repeat(atom, 1, MAX_REPEAT); }
        int exact = superscriptValue(q);
        if (exact > 0) { pos++; return RegexNode.repeat(atom, exact, exact); }
    }
    return atom;
}
```

### Generator

The generator walks the AST recursively and builds a string by making random choices
at each `ALTERNATION` node and picking a random repetition count inside the allowed
range for each `REPEAT` node. A difficulty here was ensuring that `REPEAT` nodes with
`minRep == maxRep` (like `D³`) always produce exactly that count without accidentally
calling `nextInt(0)`, which throws an exception. This was solved by checking equality
before calling the random function.
```java
static String generate(RegexNode node) {
    return switch (node.type) {
        case LITERAL     -> node.literal;
        case CONCAT      -> {
            StringBuilder sb = new StringBuilder();
            for (RegexNode child : node.children) sb.append(generate(child));
            yield sb.toString();
        }
        case ALTERNATION ->
            generate(node.children.get(RNG.nextInt(node.children.size())));
        case REPEAT      -> {
            int times = (node.minRep == node.maxRep)
                    ? node.minRep
                    : node.minRep + RNG.nextInt(node.maxRep - node.minRep + 1);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < times; i++) sb.append(generate(node.children.get(0)));
            yield sb.toString();
        }
    };
}
```

### Bonus – Processing sequence logger

Every parsing decision is recorded in a `log` list that is printed after parsing.
This was straightforward to add since the parser already visited every node in order,
adding a `log()` call at each step was enough. The main difficulty was making the
indentation readable so that nested groups are clearly visible in the output.
```java
void log(String msg) { log.add(msg); }

// example calls inside the parser:
log("parseExpr: looking for alternatives separated by '|'");
log("  found '|' at pos " + (pos - 1) + ", parsing next alternative");
log("  => built ALTERNATION with " + alts.size() + " branches");
log("    superscript '" + q + "' on " + atom + " -> repeat exactly " + exact);
```

## Conclusions / Results

The program successfully generates valid strings for all three Variant 4 regular expressions.
Running it produces output like:
```
Regex 1 : (S|T)(U|V)W*Y+24
    TVWWYYYYY24
    SVWWWYYYYY24
    SVWWY24
    TUWWWYYY24
    SVWWWWY24
    SVWYYYYY24
    TVWWYYYY24
    TVY24
    SUWWWWWYYYY24
    TVWWWWYYY24

Regex 2 : L(M|N)D³P*Q(2|3)
    LNDDDPPQ3
    LNDDDPPPPPQ2
    LMDDDPPPPQ2
    LNDDDPQ2
    LNDDDPPPPPQ3
    LNDDDPPQ2
    LMDDDPPQ3
    LNDDDPPPQ2
    LMDDDPQ2
    LMDDDPPPPPQ3

Regex 3 : R*S(T|U|V)W(X|Y|Z)²
   STWXZ
    SUWZY
    RSUWZZ
    RRRRSVWXZ
    RRRRSTWYZ
    RRSVWXY
    RRRSUWZX
    RRSUWXY
    RRRRSUWXX
    RRRRSUWYX
```

The key ideas I took from this lab are: regex operators map cleanly onto recursive tree structures,
recursive-descent parsing is a natural fit for this kind of grammar, and separating
parsing from generation makes both parts easier to reason about and test.

