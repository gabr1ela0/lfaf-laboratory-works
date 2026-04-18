import java.util.ArrayList;
import java.util.List;

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

        System.out.println("Input:");
        System.out.println(input);

        Lexer lexer = new Lexer(input);
        List<Token> tokens = new ArrayList<>();
        Token token;

        System.out.println("Tokens:");
        do {
            token = lexer.getNextToken();
            System.out.println(token);
            tokens.add(token);
        } while (token.getType() != TokenType.EOF);

        Parser parser = new Parser(tokens);
        ASTNode.Program program = parser.parse();

        System.out.println("\nAST:");
        program.print("", true);
    }
}