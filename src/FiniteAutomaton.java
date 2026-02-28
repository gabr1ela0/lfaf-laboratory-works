import java.util.*;

class FiniteAutomaton {

    private Set<String> states;
    private Set<String> alphabet;
    private Map<String, Map<String, Set<String>>> transitions;
    private String startState;
    private Set<String> finalStates;

    public FiniteAutomaton(Set<String> states,
                           Set<String> alphabet,
                           Map<String, Map<String, Set<String>>> transitions,
                           String startState,
                           Set<String> finalStates) {

        this.states = states;
        this.alphabet = alphabet;
        this.transitions = transitions;
        this.startState = startState;
        this.finalStates = finalStates;
    }

    // Check if automaton is deterministic
    public boolean isDeterministic() {

        for (String state : transitions.keySet()) {
            for (String symbol : transitions.get(state).keySet()) {

                // More than one transition = NDFA
                if (transitions.get(state).get(symbol).size() > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    // Check if string belongs to language
    public boolean stringBelongsToLanguage(String input) {

        Set<String> currentStates = new HashSet<>();
        currentStates.add(startState);

        for (char ch : input.toCharArray()) {

            Set<String> nextStates = new HashSet<>();

            for (String state : currentStates) {

                if (transitions.containsKey(state) &&
                        transitions.get(state).containsKey(String.valueOf(ch))) {

                    nextStates.addAll(
                            transitions.get(state).get(String.valueOf(ch)));
                }
            }

            currentStates = nextStates;

            if (currentStates.isEmpty()) return false;
        }

        for (String state : currentStates) {
            if (finalStates.contains(state)) return true;
        }

        return false;
    }

    // Convert NDFA to DFA (subset construction)
    public FiniteAutomaton convertToDFA() {

        Set<String> newStates = new HashSet<>();
        Map<String, Map<String, Set<String>>> newTransitions = new HashMap<>();
        Queue<Set<String>> queue = new LinkedList<>();

        Set<String> startSet = new HashSet<>();
        startSet.add(startState);

        queue.add(startSet);
        newStates.add(startSet.toString());

        while (!queue.isEmpty()) {

            Set<String> currentSet = queue.poll();
            String currentName = currentSet.toString();

            for (String symbol : alphabet) {

                Set<String> nextSet = new HashSet<>();

                for (String state : currentSet) {

                    if (transitions.containsKey(state) &&
                            transitions.get(state).containsKey(symbol)) {

                        nextSet.addAll(transitions.get(state).get(symbol));
                    }
                }

                if (!nextSet.isEmpty()) {

                    String nextName = nextSet.toString();

                    newTransitions.putIfAbsent(currentName, new HashMap<>());
                    newTransitions.get(currentName)
                            .put(symbol, Set.of(nextName));

                    if (!newStates.contains(nextName)) {
                        newStates.add(nextName);
                        queue.add(nextSet);
                    }
                }
            }
        }

        // Determine new final states
        Set<String> newFinalStates = new HashSet<>();

        for (String state : newStates) {
            for (String finalState : finalStates) {
                if (state.contains(finalState)) {
                    newFinalStates.add(state);
                }
            }
        }

        return new FiniteAutomaton(
                newStates,
                alphabet,
                newTransitions,
                startSet.toString(),
                newFinalStates);
    }

    // Convert FA to right-linear grammar
    public Grammar toRegularGrammar() {

        Map<String, List<String>> productions = new HashMap<>();

        for (String state : transitions.keySet()) {

            for (String symbol : transitions.get(state).keySet()) {

                for (String next : transitions.get(state).get(symbol)) {

                    productions.putIfAbsent(state, new ArrayList<>());

                    productions.get(state)
                            .add(symbol + next);

                    // If next is final -> add terminal-only rule
                    if (finalStates.contains(next)) {
                        productions.get(state).add(symbol);
                    }
                }
            }
        }

        return new Grammar(states, alphabet, startState, productions);
    }
}