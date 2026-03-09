# Laboratory Work 2

### Course: Formal Languages & Finite Automata
### Author: Gabriela Bîtca FAF-242

---

## Theory

A finite automaton is a simple machine that reads a string character by character and decides whether to accept or reject it based on rules called transitions. It moves between states as it reads input, and if it ends up in a "final" state, the string is accepted.

There are two kinds: a **DFA** (Deterministic Finite Automaton), where each state has exactly one possible next state for each input symbol, and an **NDFA** (Non-Deterministic Finite Automaton), where a state can branch into multiple next states at once. Both recognize the same class of languages (regular languages), and any NDFA can be converted into an equivalent DFA using an algorithm called **subset construction**.

---

## Objectives

- Implement a grammar classifier based on the Chomsky hierarchy.
- Define the finite automaton from Variant 8 and determine if it's deterministic.
- Convert the NDFA to a DFA using subset construction.
- Convert the finite automaton into a regular grammar.

---

## Variant 8

```
Q = {q0, q1, q2, q3, q4}
∑ = {a, b}
F = {q3}
δ(q0, a) = q1
δ(q1, b) = q2
δ(q1, b) = q1     ← same state, same symbol, two destinations: this is what makes it an NDFA
δ(q2, b) = q0
δ(q2, a) = q3
δ(q3, a) = q4
δ(q4, a) = q0
```

---

## Implementation

### Setting Up the Automaton (`Main.java`)

I built the automaton manually by adding states, alphabet symbols, and transitions one by one. The `addTransition()` helper method keeps it clean. The non-determinism is right here: `q1` on input `b` can go to both `q1` and `q2`.

```java
FiniteAutomaton fa = new FiniteAutomaton();

fa.states.add("q0");
fa.states.add("q1");
fa.states.add("q2");
fa.states.add("q3");
fa.states.add("q4");

fa.alphabet.add('a');
fa.alphabet.add('b');

fa.startState = "q0";
fa.finalStates.add("q3");

fa.addTransition("q0", 'a', "q1");
fa.addTransition("q1", 'b', "q2");
fa.addTransition("q1", 'b', "q1");  // non-deterministic transition
fa.addTransition("q2", 'b', "q0");
fa.addTransition("q2", 'a', "q3");
fa.addTransition("q3", 'a', "q4");
fa.addTransition("q4", 'a', "q0");
```

---

### Checking Determinism (`FiniteAutomaton.java`)

The idea is simple: if any state has more than one destination for the same input symbol, it's non-deterministic. The check just loops through transitions and looks at the size of each destination set.

```java
public boolean isDeterministic() {
    for (String state : transitions.keySet()) {
        for (char symbol : transitions.get(state).keySet()) {
            if (transitions.get(state).get(symbol).size() > 1)
                return false;
        }
    }
    return true;
}
```

This correctly returns `false` for Variant 8 because `q1` on `b` has two destinations.

---

### NDFA to DFA Conversion (`FiniteAutomaton.java`)

This uses **subset construction**: each DFA state represents a *set* of NDFA states that could be active at the same time. I start with just `{q0}`, then for each symbol, I collect all states reachable from the current set and treat that as a new DFA state. This repeats until no new sets are found.

A DFA state is marked as final if it contains any of the original NDFA final states.

```java
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

            if (nextSet.isEmpty()) continue;

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
```

---

### Automaton to Regular Grammar (`FiniteAutomaton.java`)

Every transition `qi --a--> qj` becomes a production `qi -> aqj`. If `qj` is a final state, I also add `qi -> a` — this lets the grammar "stop" there and accept the string.

```java
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
```

---

### Grammar Classification (`Grammar.java`)

This method figures out which Chomsky type the grammar is. The key fix from the original version: state names like `q0`, `q1` are multi-character, so the old single-character check `^[A-Z]$` was failing and everything was falling through to Type 0.

The updated version uses patterns that accept any alphanumeric non-terminal name:

- **Type 3 (Regular):** every production looks like `A -> aB` or `A -> a` (one terminal, optionally followed by one non-terminal)
- **Type 2 (Context-Free):** LHS is a single non-terminal (any length)
- **Type 1 (Context-Sensitive):** RHS is at least as long as LHS
- **Type 0:** everything else

```java
public void classifyGrammar() {
    boolean isType3 = true;
    boolean isType2 = true;
    boolean isType1 = true;

    // Right-linear: terminal optionally followed by a non-terminal, e.g. "a", "aq1", "bq0"
    Pattern rightLinear = Pattern.compile("^[a-z]([a-zA-Z][a-zA-Z0-9]*)?$");

    // Left-linear: terminal optionally preceded by a non-terminal, e.g. "a", "q1a"
    Pattern leftLinear = Pattern.compile("^([a-zA-Z][a-zA-Z0-9]*)?[a-z]$");

    for (Map.Entry<String, List<String>> entry : productions.entrySet()) {
        String lhs = entry.getKey();
        List<String> rhs = entry.getValue();

        // LHS must be a single non-terminal (letters/digits, starting with a letter)
        if (!lhs.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
            isType2 = false;
            isType3 = false;
        }

        for (String production : rhs) {

            // Type 1: RHS must be at least as long as LHS
            if (!production.equals("ε") && production.length() < lhs.length()) {
                isType1 = false;
            }

            // Type 3: must be right-linear or left-linear
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
```

---

## Output

```
Deterministic: false

NDFA -> DFA conversion:
{q1,q3} -> a {q4}
{q1,q3} -> b {q1,q2}
{q1,q2} -> a {q3}
{q1,q2} -> b {q0,q1,q2}
{q0,q1,q2} -> a {q1,q3}
{q0,q1,q2} -> b {q0,q1,q2}
{q4} -> a {q0}
{q3} -> a {q4}
{q0} -> a {q1}
{q1} -> b {q1,q2}

Regular Grammar:
q0 -> aq1
q1 -> bq1 , bq2
q2 -> aq3 , a , bq0
q3 -> aq4
q4 -> aq0

Grammar Type: Type 3 (Regular Grammar)
```

---

## Conclusions

The automaton from Variant 8 is correctly identified as an NDFA because `q1` transitions to both `q1` and `q2` on input `b`. The subset construction algorithm converts it into an equivalent DFA where each state is a set of original NDFA states. The grammar produced from the automaton is right-linear and correctly classified as Type 3 after fixing the classifier to handle multi-character state names like `q0`, `q1` instead of assuming single uppercase letters.