public class Main {
    public static void main(String[] args) {
        Grammar grammar = new Grammar();

        System.out.println("Generated strings");
        for (int i = 0; i < 5; i++) {
            System.out.println((i + 1) + ". " + grammar.generateString());
        }

        FiniteAutomaton fa = grammar.toFiniteAutomaton();

        System.out.println("\nCheck if a string belongs to the language");
        String[] testWords = {"ase", "ade", "aaaaaaae", "miau", "aae", "aaaca", "wealth", "fame", "power"};

        for (String w : testWords) {
            System.out.println(w + " -> " + fa.stringBelongsToLanguage(w));
        }
    }
}
