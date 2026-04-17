import java.util.*;

public class Grammar {
    private final Set<String> nonTerminals;
    private final Set<String> terminals;
    private final Map<String, List<List<String>>> productions;
    private final String startSymbol;

    public Grammar(Set<String> nonTerminals, Set<String> terminals,
                   Map<String, List<List<String>>> productions, String startSymbol) {
        this.nonTerminals = new LinkedHashSet<>(nonTerminals);
        this.terminals = new LinkedHashSet<>(terminals);
        this.productions = new LinkedHashMap<>(productions);
        this.startSymbol = startSymbol;
    }

    public Set<String> getNonTerminals() { return nonTerminals; }
    public Set<String> getTerminals() { return terminals; }
    public Map<String, List<List<String>>> getProductions() { return productions; }
    public String getStartSymbol() { return startSymbol; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("V_N = ").append(nonTerminals).append("\n");
        sb.append("V_T = ").append(terminals).append("\n");
        sb.append("S   = ").append(startSymbol).append("\n");
        sb.append("P:\n");
        for (Map.Entry<String, List<List<String>>> entry : productions.entrySet()) {
            for (List<String> rhs : entry.getValue()) {
                sb.append("  ").append(entry.getKey()).append(" -> ");
                sb.append(rhs.isEmpty() ? "ε" : String.join(" ", rhs));
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}