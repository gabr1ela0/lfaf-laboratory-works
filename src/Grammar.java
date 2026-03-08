// Grammar.java
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

    public String classifyGrammar() {
        boolean isRegular = true;

        for (String left : productions.keySet()) {

            if (!nonTerminals.contains(left) || left.length() < 1) {
                isRegular = false;
                break;
            }

            for (String right : productions.get(left)) {
                if (!(right.length() == 1 ||
                        (right.length() > 1 &&
                                nonTerminals.contains(
                                        String.valueOf(right.charAt(1)))))) {
                    isRegular = false;
                    break;
                }
            }
        }

        if (isRegular) return "Regular Grammar";
        return "Cannot determine precisely";
    }

    public void printProductions() {
        System.out.println("\nRegular Grammar Productions:");

        List<String> sortedLefts = new ArrayList<>(productions.keySet());
        Collections.sort(sortedLefts);

        for (String left : sortedLefts) {
            List<String> rhs = new ArrayList<>(productions.get(left));
            Collections.sort(rhs);

            System.out.println(left + " -> [" + String.join(", ", rhs) + "]");
        }

        System.out.println("Classification: " + classifyGrammar());
    }
}