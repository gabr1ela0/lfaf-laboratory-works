import java.util.*;
import java.util.regex.*;

public class Grammar {

    private Map<String, List<String>> productions = new HashMap<>();

    public void addProduction(String left, String right) {
        productions.putIfAbsent(left, new ArrayList<>());
        productions.get(left).add(right);
    }

    public void printGrammar() {
        System.out.println("\nRegular Grammar:");
        for (String nonTerminal : productions.keySet()) {
            System.out.print(nonTerminal + " -> ");
            List<String> rules = productions.get(nonTerminal);
            for (int i = 0; i < rules.size(); i++) {
                System.out.print(rules.get(i));
                if (i < rules.size() - 1)
                    System.out.print(" , ");
            }
            System.out.println();
        }
    }

    public void classifyGrammar() {
        boolean isType3 = true;
        boolean isType2 = true;
        boolean isType1 = true;

        // Right-linear: one terminal optionally followed by a non-terminal (any length)
        // e.g. "a", "aq1", "bq0"
        Pattern rightLinear = Pattern.compile("^[a-z]([a-zA-Z][a-zA-Z0-9]*)?$");

        // Left-linear: one terminal optionally preceded by a non-terminal (any length)
        // e.g. "a", "q1a", "q0b"
        Pattern leftLinear = Pattern.compile("^([a-zA-Z][a-zA-Z0-9]*)?[a-z]$");

        for (Map.Entry<String, List<String>> entry : productions.entrySet()) {
            String lhs = entry.getKey();
            List<String> rhs = entry.getValue();

            // Type 2 check: LHS must be a single non-terminal (any length is fine)
            // but it must contain ONLY non-terminal symbols (no terminals on LHS)
            if (!lhs.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
                isType2 = false;
                isType3 = false;
            }

            for (String production : rhs) {

                // Type 1: RHS length must be >= LHS length (except epsilon)
                if (!production.equals("ε") && production.length() < lhs.length()) {
                    isType1 = false;
                }

                // Type 3: must be right-linear OR left-linear
                if (!production.equals("ε") &&
                        !rightLinear.matcher(production).matches() &&
                        !leftLinear.matcher(production).matches()) {
                    isType3 = false;
                }
            }
        }

        if (isType3 && isType2) {
            System.out.println("\nGrammar Type: Type 3 (Regular Grammar)");
        } else if (isType2) {
            System.out.println("\nGrammar Type: Type 2 (Context-Free Grammar)");
        } else if (isType1) {
            System.out.println("\nGrammar Type: Type 1 (Context-Sensitive Grammar)");
        } else {
            System.out.println("\nGrammar Type: Type 0 (Unrestricted Grammar)");
        }
    }
}