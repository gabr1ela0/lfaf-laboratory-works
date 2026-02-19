import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class FiniteAutomaton {
    private Set<String> states;
    private Set<String> alphabet;
    private Map<String, Map<String, Set<String>>> transitions;
    private String startState;
    private Set<String> finalStates;

    public FiniteAutomaton(Set<String> states, Set<String> alphabet,
                           Map<String, Map<String, Set<String>>> transitions,
                           String startState, Set<String> finalStates) {
        this.states = states;
        this.alphabet = alphabet;
        this.transitions = transitions;
        this.startState = startState;
        this.finalStates = finalStates;
    }

    public boolean stringBelongsToLanguage(String input) {
        Set<String> currentStates = new HashSet<>();
        currentStates.add(startState);

        for (char ch : input.toCharArray()) {
            Set<String> nextStates = new HashSet<>();

            for (String state : currentStates) {
                if (transitions.containsKey(state) && transitions.get(state).containsKey(String.valueOf(ch))) {
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
}
