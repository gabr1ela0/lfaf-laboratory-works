import java.util.*;

public class FiniteAutomaton {

    public Set<String> states = new HashSet<>();
    public Set<Character> alphabet = new HashSet<>();
    public String startState;
    public Set<String> finalStates = new HashSet<>();

    public Map<String, Map<Character, Set<String>>> transitions = new HashMap<>();

    public void addTransition(String from, char symbol, String to) {

        transitions.putIfAbsent(from, new HashMap<>());
        transitions.get(from).putIfAbsent(symbol, new HashSet<>());
        transitions.get(from).get(symbol).add(to);
    }

    public boolean isDeterministic() {

        for (String state : transitions.keySet()) {
            for (char symbol : transitions.get(state).keySet()) {

                if (transitions.get(state).get(symbol).size() > 1)
                    return false;
            }
        }

        return true;
    }

    public FiniteAutomaton convertToDFA() {

        FiniteAutomaton dfa = new FiniteAutomaton();
        dfa.alphabet = this.alphabet;

        Map<Set<String>, String> nameMap = new HashMap<>();
        Queue<Set<String>> queue = new LinkedList<>();

        Set<String> startSet = new HashSet<>();
        startSet.add(startState);

        nameMap.put(startSet, setToString(startSet));
        dfa.startState = setToString(startSet);
        dfa.states.add(setToString(startSet));

        queue.add(startSet);

        if (containsFinal(startSet))
            dfa.finalStates.add(setToString(startSet));

        while (!queue.isEmpty()) {

            Set<String> current = queue.poll();
            String currentName = nameMap.get(current);

            for (char symbol : alphabet) {

                Set<String> nextSet = new HashSet<>();

                for (String state : current) {

                    if (transitions.containsKey(state) &&
                            transitions.get(state).containsKey(symbol)) {

                        nextSet.addAll(transitions.get(state).get(symbol));
                    }
                }

                if (nextSet.isEmpty())
                    continue;

                nameMap.putIfAbsent(nextSet, setToString(nextSet));
                String nextName = nameMap.get(nextSet);

                if (!dfa.states.contains(nextName)) {

                    dfa.states.add(nextName);
                    queue.add(nextSet);

                    if (containsFinal(nextSet))
                        dfa.finalStates.add(nextName);
                }

                dfa.addTransition(currentName, symbol, nextName);
            }
        }

        return dfa;
    }

    private boolean containsFinal(Set<String> set) {

        for (String s : set)
            if (finalStates.contains(s))
                return true;

        return false;
    }

    public Grammar toRegularGrammar() {

        Grammar grammar = new Grammar();

        for (String state : transitions.keySet()) {

            for (char symbol : transitions.get(state).keySet()) {

                for (String next : transitions.get(state).get(symbol)) {

                    grammar.addProduction(state, symbol + next);

                    if (finalStates.contains(next))
                        grammar.addProduction(state, String.valueOf(symbol));
                }
            }
        }

        return grammar;
    }

    private String setToString(Set<String> set) {

        List<String> list = new ArrayList<>(set);
        Collections.sort(list);

        return "{" + String.join(",", list) + "}";
    }
}