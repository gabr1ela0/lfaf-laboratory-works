public class Main {

    public static void main(String[] args) {
        String input = """
                def add(x, y)
                x + y * 2
                sin(3.14)
                cos(0)
                result = 10.5 - 1.1 / 2
                # this is a comment
                """;

        System.out.println("Input");
        System.out.println(input);
        System.out.println("Tokens");

        Lexer lexer = new Lexer(input);
        Token token;

        do {
            token = lexer.getNextToken();
            System.out.println(token);
        } while (token.getType() != TokenType.EOF);
    }
}