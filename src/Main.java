import java.util.*;

public class Main {
    public static void main(String[] args) {
        Grammar g = buildVariant8();
        new CNFConverter().toCNF(g);
    }

    static Grammar buildVariant8() {
        Set<String> vn = new LinkedHashSet<>(Arrays.asList("S","A","B","C"));
        Set<String> vt = new LinkedHashSet<>(Arrays.asList("a","d"));
        Map<String, List<List<String>>> p = new LinkedHashMap<>();

        p.put("S", Arrays.asList(
                Arrays.asList("d","B"),
                Arrays.asList("A")
        ));
        p.put("A", Arrays.asList(
                Arrays.asList("d"),
                Arrays.asList("d","S"),
                Arrays.asList("a","A","d","A","B")
        ));
        p.put("B", Arrays.asList(
                Arrays.asList("a"),
                Arrays.asList("a","S"),
                Arrays.asList("A"),
                Collections.emptyList()   // ε
        ));
        p.put("C", Arrays.asList(
                Arrays.asList("A","a")
        ));

        return new Grammar(vn, vt, p, "S");
    }
}