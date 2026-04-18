public abstract class ASTNode {

    // Returns a readable label for this node (used when printing the tree)
    public abstract String label();

    // Prints the tree with indentation to show hierarchy
    public void print(String indent, boolean isLast) {
        String branch = isLast ? "└── " : "├── ";
        System.out.println(indent + branch + label());
        String childIndent = indent + (isLast ? "    " : "│   ");
        java.util.List<ASTNode> children = children();
        for (int i = 0; i < children.size(); i++) {
            children.get(i).print(childIndent, i == children.size() - 1);
        }
    }

    public java.util.List<ASTNode> children() {
        return java.util.Collections.emptyList();
    }

    // Concrete node types

    /** Root of the program: a list of top-level statements */
    public static class Program extends ASTNode {
        public final java.util.List<ASTNode> statements;
        public Program(java.util.List<ASTNode> statements) { this.statements = statements; }

        @Override public String label() { return "Program"; }
        @Override public java.util.List<ASTNode> children() { return statements; }
    }

    /** def add(x, y) */
    public static class FunctionDef extends ASTNode {
        public final String name;
        public final java.util.List<String> params;
        public FunctionDef(String name, java.util.List<String> params) {
            this.name = name; this.params = params;
        }

        @Override public String label() {
            return "FunctionDef: " + name + "(" + String.join(", ", params) + ")";
        }
    }

    /** result = expr */
    public static class Assignment extends ASTNode {
        public final String variable;
        public final ASTNode value;
        public Assignment(String variable, ASTNode value) {
            this.variable = variable; this.value = value;
        }

        @Override public String label() { return "Assign: " + variable; }
        @Override public java.util.List<ASTNode> children() {
            return java.util.Collections.singletonList(value);
        }
    }

    /** sin(expr) or cos(expr) or name(args...) */
    public static class FunctionCall extends ASTNode {
        public final String name;
        public final java.util.List<ASTNode> args;
        public FunctionCall(String name, java.util.List<ASTNode> args) {
            this.name = name; this.args = args;
        }

        @Override public String label() { return "Call: " + name; }
        @Override public java.util.List<ASTNode> children() { return args; }
    }

    /** left OP right */
    public static class BinaryOp extends ASTNode {
        public final String op;
        public final ASTNode left, right;
        public BinaryOp(String op, ASTNode left, ASTNode right) {
            this.op = op; this.left = left; this.right = right;
        }

        @Override public String label() { return "BinaryOp: " + op; }
        @Override public java.util.List<ASTNode> children() {
            return java.util.Arrays.asList(left, right);
        }
    }

    /** A number literal */
    public static class NumberLiteral extends ASTNode {
        public final String value;
        public NumberLiteral(String value) { this.value = value; }

        @Override public String label() { return "Number: " + value; }
    }

    /** A variable reference */
    public static class Identifier extends ASTNode {
        public final String name;
        public Identifier(String name) { this.name = name; }

        @Override public String label() { return "Identifier: " + name; }
    }
}