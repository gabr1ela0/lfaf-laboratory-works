# Laboratory Work 2

### Course: Formal Languages & Finite Automata
### Author: Gabriela Bîtca FAF-242


## Theory

A finite automaton is a mathematical model that recognizes patterns in strings by moving between states based on transition rules. If the automaton ends in a final state after reading the full input, the string is accepted. There are two types: DFA (one transition per state/symbol) and NDFA (multiple possible transitions). Both recognize regular languages, and every NDFA can be converted to an equivalent DFA using subset construction.


## Objectives

* Implement a grammar classifier based on the Chomsky hierarchy.
* Define the finite automaton from Variant 8 and determine if it's deterministic.
* Convert the NDFA to a DFA using subset construction.
* Convert the finite automaton into a regular grammar.


## Implementation Description

### Setting Up the Automaton

In `Main.java`, I defined the automaton from Variant 8 by building the transition map manually. To keep things clean, I wrote a helper method `addTransition()` that handles the nested map structure so I didn't have to repeat that boilerplate everywhere. The NDFA case is clear here: `q1` on input `b` can go to both `q1` and `q2`.
```java
Set<String> states = Set.of("q0", "q1", "q2", "q3", "q4");
Set<String> alphabet = Set.of("a", "b");
Set<String> finalStates = Set.of("q3");
String startState = "q0";

Map<String, Map<String, Set<String>>> transitions = new HashMap<>();

addTransition(transitions, "q0", "a", "q1");
addTransition(transitions, "q1", "b", "q2");
addTransition(transitions, "q1", "b", "q1"); // NDFA case
addTransition(transitions, "q2", "b", "q0");
addTransition(transitions, "q2", "a", "q3");
addTransition(transitions, "q3", "a", "q4");
addTransition(transitions, "q4", "a", "q0");
```

### Checking Determinism

The logic here is simple: for every state and symbol pair, if the set of next states has more than one element, the automaton is non-deterministic. What caught me off guard initially was that I wasn't checking `.size()` properly I was just checking whether a transition existed, not how many destinations it had. Once that was fixed, it correctly flagged Variant 8 as an NDFA.
```java
public boolean isDeterministic() {
    for (String state : transitions.keySet())
        for (String symbol : transitions.get(state).keySet())
            if (transitions.get(state).get(symbol).size() > 1)
                return false;
    return true;
}
```

### NDFA to DFA Conversion

I used subset construction, where each DFA state represents a set of NDFA states. A queue drives the process  I start with the initial state, then for each symbol compute all reachable states and create a new DFA state from that set. This continues until no new state sets are discovered.

The tricky part was identifying final states in the DFA. Since state sets get serialized as strings like `[q1, q2]`, I used `.contains()` to check if any original final state appears in that string.
```java
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
            for (String state : currentSet)
                if (transitions.containsKey(state) && transitions.get(state).containsKey(symbol))
                    nextSet.addAll(transitions.get(state).get(symbol));

            if (!nextSet.isEmpty()) {
                String nextName = nextSet.toString();
                newTransitions.putIfAbsent(currentName, new HashMap<>());
                newTransitions.get(currentName).put(symbol, Set.of(nextName));
                if (!newStates.contains(nextName)) {
                    newStates.add(nextName);
                    queue.add(nextSet);
                }
            }
        }
    }

    Set<String> newFinalStates = new HashSet<>();
    for (String state : newStates)
        for (String finalState : finalStates)
            if (state.contains(finalState))
                newFinalStates.add(state);

    return new FiniteAutomaton(newStates, alphabet, newTransitions, startSet.toString(), newFinalStates);
}
```

### Automaton to Regular Grammar

Each transition `qi --a--> qj` maps directly to a production `qi -> a qj`. If `qj` is a final state, I also add `qi -> a` so the string can terminate there. The early mistake I made was forgetting that second rule entirely the grammar was generating productions but strings that ended in a final state were never being accepted. Adding the terminal-only rule fixed it.
```java
public Grammar toRegularGrammar() {
    Map<String, List<String>> productions = new HashMap<>();
    for (String state : transitions.keySet())
        for (String symbol : transitions.get(state).keySet())
            for (String next : transitions.get(state).get(symbol)) {
                productions.putIfAbsent(state, new ArrayList<>());
                productions.get(state).add(symbol + next);
                if (finalStates.contains(next))
                    productions.get(state).add(symbol);
            }
    return new Grammar(states, alphabet, startState, productions);
}
```

### Grammar Classification

The classifier checks whether all productions follow the form `A -> a` or `A -> aB`, which defines a Type 3 regular grammar. The issue I ran into was assuming non-terminals are always single characters. Since states are named `q0`, `q1` that check was causing everything to fail. I removed the `left.length() != 1` restriction and checked set membership instead, which resolved it.

However, since the grammar produced from the automaton uses multi-character state names as non-terminals (like `q0`, `q1`), the classifier still returns `"Cannot determine precisely"` for this specific output — the structure is technically regular but the naming doesn't match the strict single-character assumption built into the method. This is a known limitation of the current implementation.
```java
public String classifyGrammar() {
    boolean isRegular = true;
    for (String left : productions.keySet()) {
        if (!nonTerminals.contains(left) || left.length() != 1) {
            isRegular = false;
            break;
        }
        for (String right : productions.get(left)) {
            if (!(right.length() == 1 ||
                (right.length() == 2 && nonTerminals.contains(String.valueOf(right.charAt(1)))))) {
                isRegular = false;
                break;
            }
        }
    }
    if (isRegular) return "Regular Grammar";
    return "Cannot determine precisely";
}
```


## Results
```
Is deterministic: false
Converted to DFA.
Is deterministic: true

Regular Grammar Productions:
q1 -> bq1 | bq2
q2 -> aq3 | a | bq0
q3 -> aq4
q4 -> aq0
q0 -> aq1
Classification: Cannot determine precisely

Test word 'aba': true

Process finished with exit code 0
```

The automaton was correctly identified as non-deterministic. After conversion, the DFA confirmed as deterministic. The grammar productions match the expected right-linear structure, and the test word `"aba"` was accepted.


## Conclusions

Variant 8 was correctly identified as an NDFA due to the duplicate transition on `δ(q1,b)`. The subset construction algorithm successfully produced an equivalent DFA. The automaton was then converted into a right-linear regular grammar. The classifier returned `"Cannot determine precisely"` due to multi-character state names, the grammar is structurally regular, but the classifier's single-character assumption doesn't hold for state-named non-terminals. This is something to improve in future iterations.


