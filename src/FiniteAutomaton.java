// FiniteAutomaton.java
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

    public boolean isDeterministic() {
        for (String state : transitions.keySet()) {
            for (String symbol : transitions.get(state).keySet()) {
                if (transitions.get(state).get(symbol).size() > 1) return false;
            }
        }
        return true;
    }

    public boolean stringBelongsToLanguage(String input) {
        Set<String> currentStates = new HashSet<>();
        currentStates.add(startState);

        for (char ch : input.toCharArray()) {
            Set<String> nextStates = new HashSet<>();
            for (String state : currentStates) {
                if (transitions.containsKey(state) &&
                        transitions.get(state).containsKey(String.valueOf(ch))) {
                    nextStates.addAll(transitions.get(state).get(String.valueOf(ch)));
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

    // NDFA -> DFA subset construction
    public FiniteAutomaton convertToDFA() {

        Set<String> newStates = new LinkedHashSet<>();
        Map<String, Map<String, Set<String>>> newTransitions = new LinkedHashMap<>();
        Queue<Set<String>> queue = new LinkedList<>();
        Map<Set<String>, String> nameMap = new HashMap<>();

        Set<String> startSet = new HashSet<>();
        startSet.add(startState);

        String startName = setToName(startSet);
        queue.add(startSet);
        newStates.add(startName);
        nameMap.put(startSet, startName);

        Set<String> newFinalStates = new HashSet<>();
        if (containsFinal(startSet)) newFinalStates.add(startName);

        while (!queue.isEmpty()) {

            Set<String> currentSet = queue.poll();
            String currentName = nameMap.get(currentSet);

            for (String symbol : alphabet) {

                Set<String> nextSet = new HashSet<>();
                for (String state : currentSet) {
                    if (transitions.containsKey(state) &&
                            transitions.get(state).containsKey(symbol)) {
                        nextSet.addAll(transitions.get(state).get(symbol));
                    }
                }

                if (nextSet.isEmpty()) continue;

                String nextName = setToName(nextSet);
                nameMap.putIfAbsent(nextSet, nextName);

                newTransitions.putIfAbsent(currentName, new HashMap<>());
                newTransitions.get(currentName).put(symbol, Set.of(nextName));

                if (!newStates.contains(nextName)) {
                    newStates.add(nextName);
                    queue.add(nextSet);

                    if (containsFinal(nextSet)) newFinalStates.add(nextName);
                }
            }
        }

        return new FiniteAutomaton(newStates, alphabet, newTransitions, startName, newFinalStates);
    }

    private boolean containsFinal(Set<String> set) {
        for (String s : set) if (finalStates.contains(s)) return true;
        return false;
    }

    private String setToName(Set<String> set) {
        List<String> list = new ArrayList<>(set);
        Collections.sort(list); // optional alphabetical sort
        return String.join("", list);
    }

    public Grammar toRegularGrammar() {

        Map<String, List<String>> productions = new HashMap<>();

        for (String state : transitions.keySet()) {

            for (String symbol : transitions.get(state).keySet()) {

                for (String next : transitions.get(state).get(symbol)) {

                    productions.putIfAbsent(state, new ArrayList<>());

                    productions.get(state).add(symbol + next);

                    if (finalStates.contains(next)) {
                        productions.get(state).add(symbol);
                    }
                }
            }
        }

        return new Grammar(states, alphabet, startState, productions);
    }
}