import java.util.*;

class Grammar {

    private Set<String> nonTerminals;
    private Set<String> terminals;
    private String startSymbol;
    private Map<String, List<String>> productions;

    public Grammar(Set<String> nonTerminals,
                   Set<String> terminals,
                   String startSymbol,
                   Map<String, List<String>> productions) {

        this.nonTerminals = nonTerminals;
        this.terminals = terminals;
        this.startSymbol = startSymbol;
        this.productions = productions;
    }

    // Classify grammar according to Chomsky hierarchy
    public String classifyGrammar() {

        boolean isRegular = true;

        for (String left : productions.keySet()) {

            // Left side must be single non-terminal
            if (!nonTerminals.contains(left) || left.length() != 1) {
                isRegular = false;
                break;
            }

            for (String right : productions.get(left)) {

                // Regular grammar: a OR aB
                if (!(right.length() == 1 ||
                        (right.length() == 2 &&
                                nonTerminals.contains(
                                        String.valueOf(right.charAt(1)))))) {

                    isRegular = false;
                    break;
                }
            }
        }

        if (isRegular) return "Type 3 (Regular Grammar)";
        return "Cannot determine precisely (but not regular)";
    }

    public void printProductions() {

        System.out.println("\nRegular Grammar Productions:");

        for (String left : productions.keySet()) {
            System.out.println(left + " -> " +
                    String.join(" | ", productions.get(left)));
        }

        System.out.println("Classification: " + classifyGrammar());
    }
}