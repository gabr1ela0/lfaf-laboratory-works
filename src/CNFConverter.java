import java.util.*;

/**
 * Converts a CFG to Chomsky Normal Form in 5 steps:
 *   1. Eliminate epsilon-productions (DEL)
 *   2. Eliminate renaming/unit rules (UNIT)
 *   3. Eliminate inaccessible symbols
 *   4. Eliminate non-productive symbols
 *   5. CNF proper: START + TERM + BIN
 */
public class CNFConverter {
    private int freshCounter = 0;

    public Grammar toCNF(Grammar g) {
        System.out.println("ORIGINAL GRAMMAR\n" + g);

        Grammar s1 = eliminateEpsilonProductions(g);
        System.out.println("STEP 1: Eliminate ε-productions\n" + s1);



    }

    // STEP 1: Eliminate ε-productions
    public Grammar eliminateEpsilonProductions(Grammar g) {
        Set<String> nullable = findNullable(g);
        Map<String, List<List<String>>> newProds = new LinkedHashMap<>();

        for (Map.Entry<String, List<List<String>>> entry : g.getProductions().entrySet()) {
            String lhs = entry.getKey();
            Set<List<String>> expanded = new LinkedHashSet<>();
            for (List<String> rhs : entry.getValue())
                expanded.addAll(expandNullable(rhs, nullable));
            expanded.remove(Collections.emptyList());
            if (!expanded.isEmpty()) newProds.put(lhs, new ArrayList<>(expanded));
        }
        if (nullable.contains(g.getStartSymbol()))
            newProds.computeIfAbsent(g.getStartSymbol(), k -> new ArrayList<>())
                    .add(Collections.emptyList());

        return new Grammar(g.getNonTerminals(), g.getTerminals(), newProds, g.getStartSymbol());
    }

    private Set<String> findNullable(Grammar g) {
        Set<String> nullable = new HashSet<>();
        for (Map.Entry<String, List<List<String>>> e : g.getProductions().entrySet())
            for (List<String> rhs : e.getValue())
                if (rhs.isEmpty()) nullable.add(e.getKey());
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<String, List<List<String>>> e : g.getProductions().entrySet())
                if (!nullable.contains(e.getKey()))
                    for (List<String> rhs : e.getValue())
                        if (!rhs.isEmpty() && nullable.containsAll(rhs))
                            if (nullable.add(e.getKey())) changed = true;
        }
        return nullable;
    }

    private Set<List<String>> expandNullable(List<String> rhs, Set<String> nullable) {
        Set<List<String>> result = new LinkedHashSet<>();
        result.add(new ArrayList<>(rhs));
        for (int i = 0; i < rhs.size(); i++) {
            if (nullable.contains(rhs.get(i))) {
                Set<List<String>> next = new LinkedHashSet<>();
                for (List<String> existing : result) {
                    next.add(existing);
                    if (i < existing.size()) {
                        List<String> omitted = new ArrayList<>(existing);
                        omitted.remove(i);
                        next.add(omitted);
                    }
                }
                result = next;
            }
        }
        return result;
    }


}