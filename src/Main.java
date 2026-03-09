public class Main {

    public static void main(String[] args) {

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

        fa.addTransition("q0",'a',"q1");

        fa.addTransition("q1",'b',"q2");
        fa.addTransition("q1",'b',"q1");

        fa.addTransition("q2",'b',"q0");
        fa.addTransition("q2",'a',"q3");

        fa.addTransition("q3",'a',"q4");

        fa.addTransition("q4",'a',"q0");

        System.out.println("Deterministic: " + fa.isDeterministic());

        System.out.println("\nNDFA -> DFA conversion:");

        FiniteAutomaton dfa = fa.convertToDFA();

        for (String state : dfa.transitions.keySet()) {

            for (char symbol : dfa.transitions.get(state).keySet()) {

                for (String next : dfa.transitions.get(state).get(symbol)) {

                    System.out.println(state + " -> " + symbol + " " + next);
                }
            }
        }

        Grammar grammar = fa.toRegularGrammar();

        grammar.printGrammar();
        grammar.classifyGrammar();
    }
}