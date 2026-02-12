import java.util.*;

class Grammar {
    private Set<String> nonTerminals;
    private Set<String> terminals;
    private String startSymbol;
    private Map<String, List<String>> productions;
    private Random random;

    public Grammar() {
        nonTerminals = new HashSet<>(Arrays.asList("S", "D", "E", "J"));
        terminals = new HashSet<>(Arrays.asList("a", "b", "c", "d", "e"));
        startSymbol = "S";

        productions = new HashMap<>();
        productions.put("S", Arrays.asList("aD"));
        productions.put("D", Arrays.asList("dE", "bJ", "aE"));
        productions.put("J", Arrays.asList("cS"));
        productions.put("E", Arrays.asList("e", "aE"));

        random = new Random();
    }

    // Generate one random string from the grammar
    public String generateString() {
        String current = startSymbol;
        StringBuilder result = new StringBuilder();

        while (nonTerminals.contains(current)) {
            List<String> rules = productions.get(current);
            String rule = rules.get(random.nextInt(rules.size()));

            result.append(rule.charAt(0)); // terminal

            if (rule.length() == 2) {
                current = String.valueOf(rule.charAt(1));
            } else {
                break; // terminal-only production
            }
        }

        return result.toString();
    }

    // Convert grammar to finite automaton
    public FiniteAutomaton toFiniteAutomaton() {
        Set<String> states = new HashSet<>(nonTerminals);
        String finalState = "FINAL";
        states.add(finalState);

        Map<String, Map<String, Set<String>>> transitions = new HashMap<>();

        for (String state : productions.keySet()) {
            transitions.putIfAbsent(state, new HashMap<>());

            for (String rule : productions.get(state)) {
                String symbol = String.valueOf(rule.charAt(0));
                String nextState = (rule.length() == 2) ? String.valueOf(rule.charAt(1)) : finalState;

                transitions.get(state).putIfAbsent(symbol, new HashSet<>());
                transitions.get(state).get(symbol).add(nextState);
            }
        }

        return new FiniteAutomaton(states, terminals, transitions, startSymbol, new HashSet<>(Arrays.asList(finalState)));
    }
}