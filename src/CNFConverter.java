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

        Grammar s2 = eliminateRenamingRules(s1);
        System.out.println("STEP 2: Eliminate renaming (unit) rules\n" + s2);

        Grammar s3 = eliminateInaccessibleSymbols(s2);
        System.out.println("STEP 3: Eliminate inaccessible symbols\n" + s3);

        Grammar s4 = eliminateNonProductiveSymbols(s3);
        System.out.println("STEP 4: Eliminate non-productive symbols\n" + s4);

        Grammar s5 = toProperCNF(s4);
        System.out.println("STEP 5: Chomsky Normal Form\n" + s5);

        return s5;
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

    // STEP 2: Eliminate renaming rules A -> B
    public Grammar eliminateRenamingRules(Grammar g) {
        Map<String, List<List<String>>> newProds = new LinkedHashMap<>();
        for (String nt : g.getNonTerminals()) {
            Set<List<String>> expanded = new LinkedHashSet<>();
            for (String reachable : unitClosure(nt, g))
                for (List<String> rhs : g.getProductions().getOrDefault(reachable, Collections.emptyList()))
                    if (!(rhs.size() == 1 && g.getNonTerminals().contains(rhs.get(0))))
                        expanded.add(rhs);
            if (!expanded.isEmpty()) newProds.put(nt, new ArrayList<>(expanded));
        }
        return new Grammar(new LinkedHashSet<>(g.getNonTerminals()),
                new LinkedHashSet<>(g.getTerminals()), newProds, g.getStartSymbol());
    }

    private Set<String> unitClosure(String start, Grammar g) {
        Set<String> visited = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            String cur = queue.poll();
            if (!visited.add(cur)) continue;
            for (List<String> rhs : g.getProductions().getOrDefault(cur, Collections.emptyList()))
                if (rhs.size() == 1 && g.getNonTerminals().contains(rhs.get(0)))
                    queue.add(rhs.get(0));
        }
        return visited;
    }

    // STEP 3: Eliminate inaccessible symbols
    public Grammar eliminateInaccessibleSymbols(Grammar g) {
        Set<String> accessible = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(g.getStartSymbol());
        while (!queue.isEmpty()) {
            String cur = queue.poll();
            if (!accessible.add(cur)) continue;
            for (List<String> rhs : g.getProductions().getOrDefault(cur, Collections.emptyList()))
                for (String sym : rhs)
                    if (g.getNonTerminals().contains(sym)) queue.add(sym);
        }
        Set<String> newNTs = new LinkedHashSet<>(g.getNonTerminals());
        newNTs.retainAll(accessible);
        Map<String, List<List<String>>> newProds = new LinkedHashMap<>();
        for (String nt : newNTs)
            if (g.getProductions().containsKey(nt)) newProds.put(nt, g.getProductions().get(nt));
        return new Grammar(newNTs, new LinkedHashSet<>(g.getTerminals()), newProds, g.getStartSymbol());
    }

    // STEP 4: Eliminate non-productive symbols
    public Grammar eliminateNonProductiveSymbols(Grammar g) {
        Set<String> productive = new HashSet<>(g.getTerminals());
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<String, List<List<String>>> e : g.getProductions().entrySet())
                if (!productive.contains(e.getKey()))
                    for (List<String> rhs : e.getValue())
                        if (rhs.isEmpty() || productive.containsAll(rhs))
                            if (productive.add(e.getKey())) { changed = true; break; }
        }
        Set<String> newNTs = new LinkedHashSet<>(g.getNonTerminals());
        newNTs.retainAll(productive);
        Map<String, List<List<String>>> newProds = new LinkedHashMap<>();
        for (String nt : newNTs) {
            List<List<String>> filtered = new ArrayList<>();
            for (List<String> rhs : g.getProductions().getOrDefault(nt, Collections.emptyList()))
                if (rhs.stream().allMatch(s -> g.getTerminals().contains(s) || productive.contains(s)))
                    filtered.add(rhs);
            if (!filtered.isEmpty()) newProds.put(nt, filtered);
        }
        return new Grammar(newNTs, new LinkedHashSet<>(g.getTerminals()), newProds, g.getStartSymbol());
    }

    // STEP 5: Obtain proper CNF (START + TERM + BIN)
    public Grammar toProperCNF(Grammar g) {
        Set<String> nts = new LinkedHashSet<>(g.getNonTerminals());
        Set<String> ts  = new LinkedHashSet<>(g.getTerminals());
        Map<String, List<List<String>>> prods = deepCopy(g.getProductions());
        String start = g.getStartSymbol();
        String newStart = start;

        // START: add S0 only if start appears on any RHS
        boolean startOnRhs = prods.values().stream()
                .flatMap(Collection::stream).anyMatch(r -> r.contains(start));
        if (startOnRhs) {
            newStart = freshSymbol("S0", nts);
            nts.add(newStart);
            // inline S rules directly into S0 (avoid leaving S0->S unit rule)
            List<List<String>> s0rules = new ArrayList<>(
                    prods.getOrDefault(start, Collections.emptyList()));
            prods.put(newStart, s0rules);
        }

        // TERM: wrap lone terminals in mixed rules
        Map<String, String> termMap = new LinkedHashMap<>();
        Map<String, List<List<String>>> termProds = new LinkedHashMap<>(prods);
        for (String lhs : new ArrayList<>(prods.keySet())) {
            List<List<String>> newRules = new ArrayList<>();
            for (List<String> rhs : prods.get(lhs)) {
                if (rhs.size() <= 1) { newRules.add(rhs); continue; }
                List<String> newRhs = new ArrayList<>();
                for (String sym : rhs) {
                    if (ts.contains(sym)) {
                        String wrapper = termMap.computeIfAbsent(sym, t -> {
                            String name = freshSymbol("T_" + t.toUpperCase(), nts);
                            nts.add(name);
                            return name;
                        });
                        newRhs.add(wrapper);
                    } else { newRhs.add(sym); }
                }
                newRules.add(newRhs);
            }
            termProds.put(lhs, newRules);
        }
        for (Map.Entry<String, String> e : termMap.entrySet())
            termProds.put(e.getValue(), Collections.singletonList(Collections.singletonList(e.getKey())));
        prods = termProds;

        // BIN: binarize rules with > 2 symbols on RHS
        Map<String, List<List<String>>> binProds = new LinkedHashMap<>();
        for (String lhs : prods.keySet())
            for (List<String> rhs : prods.get(lhs))
                if (rhs.size() <= 2)
                    binProds.computeIfAbsent(lhs, k -> new ArrayList<>()).add(new ArrayList<>(rhs));
                else
                    binarize(lhs, new ArrayList<>(rhs), nts, binProds);

        return new Grammar(nts, ts, binProds, newStart);
    }

    private void binarize(String lhs, List<String> symbols,
                          Set<String> nts, Map<String, List<List<String>>> prods) {
        if (symbols.size() <= 2) {
            prods.computeIfAbsent(lhs, k -> new ArrayList<>()).add(new ArrayList<>(symbols));
            return;
        }
        String fresh = freshSymbol(lhs + "1", nts);
        nts.add(fresh);
        prods.computeIfAbsent(lhs, k -> new ArrayList<>())
                .add(Arrays.asList(symbols.get(0), fresh));
        binarize(fresh, symbols.subList(1, symbols.size()), nts, prods);
    }

    private String freshSymbol(String base, Set<String> existing) {
        String candidate = base;
        while (existing.contains(candidate)) candidate = base + (++freshCounter);
        return candidate;
    }

    private Map<String, List<List<String>>> deepCopy(Map<String, List<List<String>>> orig) {
        Map<String, List<List<String>>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<List<String>>> e : orig.entrySet()) {
            List<List<String>> rhsCopy = new ArrayList<>();
            for (List<String> rhs : e.getValue()) rhsCopy.add(new ArrayList<>(rhs));
            copy.put(e.getKey(), rhsCopy);
        }
        return copy;
    }
}