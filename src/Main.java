import java.util.*;

/**
 * Variant 4
 *
 * Regexes:
 *   1. (S|T)(U|V)W*Y+24
 *   2. L(M|N)D³P*Q(2|3)      (D³ = exactly 3 D's)
 *   3. R*S(T|U|V)W(X|Y|Z)²   (² = exactly 2 repetitions of one of X/Y/Z)
 *
 * Supported operators:
 *   |   – alternation
 *   *   – zero to MAX_REPEAT times (randomly chosen)
 *   +   – one  to MAX_REPEAT times (randomly chosen)
 *   ²   – exactly 2 times (Unicode superscript \u00B2)
 *   ³   – exactly 3 times (Unicode superscript \u00B3)
 *   ()  – grouping
 *
 *   Parser builds a tree of RegexNode objects
 *   Generator walks the tree randomly to produce valid strings
 *   Steps log records every parsing decision (bonus )
 */
public class Main {

    // Maximum repetitions for * and + operators
    static final int MAX_REPEAT = 5;
    static final Random RNG = new Random();

    //  AST Node

    enum NodeType { LITERAL, CONCAT, ALTERNATION, REPEAT }

    static class RegexNode {
        NodeType        type;
        String          literal;
        List<RegexNode> children = new ArrayList<>();
        int             minRep, maxRep;

        static RegexNode literal(String s) {
            RegexNode n = new RegexNode();
            n.type = NodeType.LITERAL; n.literal = s; return n;
        }
        static RegexNode concat(List<RegexNode> ch) {
            RegexNode n = new RegexNode();
            n.type = NodeType.CONCAT; n.children = new ArrayList<>(ch); return n;
        }
        static RegexNode alt(List<RegexNode> ch) {
            RegexNode n = new RegexNode();
            n.type = NodeType.ALTERNATION; n.children = new ArrayList<>(ch); return n;
        }
        static RegexNode repeat(RegexNode child, int min, int max) {
            RegexNode n = new RegexNode();
            n.type = NodeType.REPEAT;
            n.children.add(child);
            n.minRep = min; n.maxRep = max; return n;
        }

        @Override public String toString() {
            return switch (type) {
                case LITERAL      -> "Literal(\"" + literal + "\")";
                case CONCAT       -> "Concat(" + children + ")";
                case ALTERNATION  -> "Alt(" + children + ")";
                case REPEAT       -> "Repeat(" + children.get(0) + ", " + minRep + ".." + maxRep + ")";
            };
        }
    }

    //  Recursive-Descent Parser
    static class Parser {
        final char[]      src;
        int               pos;
        final List<String> log;   // processing sequence (bonus)

        Parser(String regex, List<String> log) {
            this.src = regex.toCharArray();
            this.pos = 0;
            this.log = log;
        }

        // Level 1: alternation  ( a | b | c )
        RegexNode parseExpr() {
            log("parseExpr: looking for alternatives separated by '|'");
            List<RegexNode> alts = new ArrayList<>();
            alts.add(parseConcat());
            while (pos < src.length && src[pos] == '|') {
                pos++;   // consume '|'
                log("  found '|' at pos " + (pos - 1) + ", parsing next alternative");
                alts.add(parseConcat());
            }
            if (alts.size() == 1) return alts.get(0);
            RegexNode node = RegexNode.alt(alts);
            log("  => built ALTERNATION with " + alts.size() + " branches");
            return node;
        }

        // Level 2: concatenation  ( ab )
        RegexNode parseConcat() {
            log("  parseConcat: collecting atoms until ')' or '|'");
            List<RegexNode> seq = new ArrayList<>();
            while (pos < src.length && src[pos] != ')' && src[pos] != '|') {
                seq.add(parseQuantified());
            }
            if (seq.isEmpty()) return RegexNode.literal("");
            if (seq.size() == 1) return seq.get(0);
            return RegexNode.concat(seq);
        }

        // Level 3: atom + optional quantifier (* + ² ³)
        RegexNode parseQuantified() {
            RegexNode atom = parseAtom();
            if (pos < src.length) {
                char q = src[pos];
                if (q == '*') {
                    pos++;
                    log("    quantifier '*' on " + atom + " -> repeat 0.." + MAX_REPEAT);
                    return RegexNode.repeat(atom, 0, MAX_REPEAT);
                }
                if (q == '+') {
                    pos++;
                    log("    quantifier '+' on " + atom + " -> repeat 1.." + MAX_REPEAT);
                    return RegexNode.repeat(atom, 1, MAX_REPEAT);
                }
                int exact = superscriptValue(q);
                if (exact > 0) {
                    pos++;
                    log("    superscript '" + q + "' on " + atom + " -> repeat exactly " + exact);
                    return RegexNode.repeat(atom, exact, exact);
                }
            }
            return atom;
        }

        // Level 4: a single atom (literal char or parenthesised group)
        RegexNode parseAtom() {
            if (pos >= src.length) return RegexNode.literal("");
            char c = src[pos];
            if (c == '(') {
                pos++;  // consume '('
                log("    '(' at pos " + (pos - 1) + " -> entering group");
                RegexNode inner = parseExpr();
                if (pos < src.length && src[pos] == ')') {
                    pos++;  // consume ')'
                    log("    ')' -> closing group");
                }
                return inner;
            }
            // plain literal character
            pos++;
            log("    literal '" + c + "' at pos " + (pos - 1));
            return RegexNode.literal(String.valueOf(c));
        }

        // helpers
        boolean isSuperscript(char c) {
            return c == '\u00B9' || c == '\u00B2' || c == '\u00B3';
        }
        int superscriptValue(char c) {
            return switch (c) {
                case '\u00B9' -> 1;
                case '\u00B2' -> 2;
                case '\u00B3' -> 3;
                default       -> 0;
            };
        }
        void log(String msg) { log.add(msg); }
    }


    //  Generator  – random walk through the AST
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


    //  Convenience: generate N unique strings from one regex
    static List<String> generateSamples(String regex, int count) {
        List<String> log = new ArrayList<>();
        RegexNode root = new Parser(regex, log).parseExpr();
        Set<String> seen = new LinkedHashSet<>();
        int tries = 0;
        while (seen.size() < count && tries < count * 50) {
            seen.add(generate(root));
            tries++;
        }
        return new ArrayList<>(seen);
    }

    //  Entry point
    public static void main(String[] args) {
        // Variant 4 – three regular expressions
        // ² = \u00B2, ³ = \u00B3
        String[] regexes = {
                "(S|T)(U|V)W*Y+24",
                "L(M|N)D\u00B3P*Q(2|3)",
                "R*S(T|U|V)W(X|Y|Z)\u00B2"
        };


        System.out.println("Variant 4 --- Regular Expression Generator");
        System.out.println();

        for (int i = 0; i < regexes.length; i++) {
            String regex = regexes[i];
            System.out.printf("Regex %d : %s%n%n", i + 1, regex);

            // BONUS show processing sequence
            List<String> steps = new ArrayList<>();
            RegexNode root = new Parser(regex, steps).parseExpr();

            System.out.println("Processing sequence:");
            for (String step : steps) System.out.println("    " + step);

            //Generate 10 sample strings
            System.out.println();
            System.out.println("Sample generated strings:");
            Set<String> seen = new LinkedHashSet<>();
            int tries = 0;
            while (seen.size() < 10 && tries < 500) {
                seen.add(generate(root));
                tries++;
            }
            for (String s : seen) System.out.println("    " + s);
            System.out.println();
        }
    }
}