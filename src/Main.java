// Main.java
import java.util.*;

public class Main {

    public static void main(String[] args) {

        Set<String> states = Set.of("q0", "q1", "q2", "q3", "q4");
        Set<String> alphabet = Set.of("a", "b");
        Set<String> finalStates = Set.of("q3");
        String startState = "q0";

        //state -> symbol -> set of next states
        Map<String, Map<String, Set<String>>> transitions = new HashMap<>();

        addTransition(transitions, "q0", "a", "q1");

        addTransition(transitions, "q1", "b", "q2");
        addTransition(transitions, "q1", "b", "q1"); // NDFA case

        addTransition(transitions, "q2", "b", "q0");
        addTransition(transitions, "q2", "a", "q3");

        addTransition(transitions, "q3", "a", "q4");
        addTransition(transitions, "q4", "a", "q0");

        // Create FA
        FiniteAutomaton fa =
                new FiniteAutomaton(states, alphabet, transitions, startState, finalStates);

        // b) Check determinism
        System.out.println("Is deterministic: " + fa.isDeterministic());

        // c) Convert NDFA to DFA
        FiniteAutomaton dfa = fa.convertToDFA();
        System.out.println("Converted to DFA.");
        System.out.println("Is deterministic: " + dfa.isDeterministic());

        // a) Convert FA to Regular Grammar (use original NDFA)
        Grammar grammar = fa.toRegularGrammar();
        grammar.printProductions();

        // Example string test
        System.out.println("Test word 'aba': " + dfa.stringBelongsToLanguage("aba"));
    }

    private static void addTransition(
            Map<String, Map<String, Set<String>>> transitions,
            String from, String symbol, String to) {

        transitions.putIfAbsent(from, new HashMap<>());
        transitions.get(from).putIfAbsent(symbol, new HashSet<>());
        transitions.get(from).get(symbol).add(to);
    }
}