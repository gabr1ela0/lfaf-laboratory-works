import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent parser.
 * Consumes a flat list of tokens produced by the Lexer and builds an AST.
 *
 * Grammar (informal):
 *   program     -> statement*
 *   statement   -> funcDef | assignment | expr
 *   funcDef     -> DEF IDENTIFIER LPAREN paramList RPAREN
 *   paramList   -> (IDENTIFIER (COMMA IDENTIFIER)*)?
 *   assignment  -> IDENTIFIER ASSIGN expr
 *   expr        -> term ((PLUS | MINUS) term)*
 *   term        -> factor ((MULTIPLY | DIVIDE) factor)*
 *   factor      -> NUMBER
 *               | IDENTIFIER
 *               | funcCall
 *               | LPAREN expr RPAREN
 *   funcCall    -> (SIN | COS | IDENTIFIER) LPAREN argList RPAREN
 *   argList     -> (expr (COMMA expr)*)?
 */
public class Parser {

    private final List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    // Helpers
    private Token peek() { return tokens.get(pos); }

    private Token consume() { return tokens.get(pos++); }

    private Token expect(TokenType type) {
        Token t = consume();
        if (t.getType() != type) {
            throw new RuntimeException(
                    "Expected " + type + " but got " + t.getType() + " ('" + t.getValue() + "')");
        }
        return t;
    }

    private boolean check(TokenType type) { return peek().getType() == type; }

    private boolean isAtEnd() { return check(TokenType.EOF); }

    // Entry point
    public ASTNode.Program parse() {
        List<ASTNode> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(parseStatement());
        }
        return new ASTNode.Program(statements);
    }

    // Statement
    private ASTNode parseStatement() {
        // def keyword -> function definition
        if (check(TokenType.DEF)) {
            return parseFunctionDef();
        }

        // IDENTIFIER followed by '=' -> assignment
        // We peek two tokens ahead to decide without consuming
        if (check(TokenType.IDENTIFIER) && lookahead(1) == TokenType.ASSIGN) {
            return parseAssignment();
        }

        // Everything else is a standalone expression (function call, arithmetic)
        return parseExpr();
    }

    private TokenType lookahead(int offset) {
        int idx = pos + offset;
        if (idx >= tokens.size()) return TokenType.EOF;
        return tokens.get(idx).getType();
    }
    
    // Function definition
    private ASTNode.FunctionDef parseFunctionDef() {
        expect(TokenType.DEF);
        String name = expect(TokenType.IDENTIFIER).getValue();
        expect(TokenType.LPAREN);

        List<String> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            params.add(expect(TokenType.IDENTIFIER).getValue());
            while (check(TokenType.COMMA)) {
                consume(); // eat ','
                params.add(expect(TokenType.IDENTIFIER).getValue());
            }
        }

        expect(TokenType.RPAREN);
        return new ASTNode.FunctionDef(name, params);
    }

    //Assignment 
    private ASTNode.Assignment parseAssignment() {
        String varName = expect(TokenType.IDENTIFIER).getValue();
        expect(TokenType.ASSIGN);
        ASTNode value = parseExpr();
        return new ASTNode.Assignment(varName, value);
    }

    //  Expression (handles + and -) 

    private ASTNode parseExpr() {
        ASTNode left = parseTerm();

        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            String op = consume().getValue();
            ASTNode right = parseTerm();
            left = new ASTNode.BinaryOp(op, left, right);
        }

        return left;
    }

    //  Term (handles * and /) 

    private ASTNode parseTerm() {
        ASTNode left = parseFactor();

        while (check(TokenType.MULTIPLY) || check(TokenType.DIVIDE)) {
            String op = consume().getValue();
            ASTNode right = parseFactor();
            left = new ASTNode.BinaryOp(op, left, right);
        }

        return left;
    }

    //  Factor (atoms + parenthesised expressions) 

    private ASTNode parseFactor() {
        Token t = peek();

        // Number literal
        if (t.getType() == TokenType.NUMBER) {
            consume();
            return new ASTNode.NumberLiteral(t.getValue());
        }

        // Parenthesised expression
        if (t.getType() == TokenType.LPAREN) {
            consume(); // '('
            ASTNode inner = parseExpr();
            expect(TokenType.RPAREN);
            return inner;
        }

        // sin / cos / identifier - could be a call or a plain variable
        if (t.getType() == TokenType.SIN
                || t.getType() == TokenType.COS
                || t.getType() == TokenType.IDENTIFIER) {

            // If followed by '(' it is a function call
            if (lookahead(1) == TokenType.LPAREN) {
                return parseFunctionCall();
            }

            // Otherwise just an identifier reference
            consume();
            return new ASTNode.Identifier(t.getValue());
        }

        throw new RuntimeException(
                "Unexpected token in factor: " + t.getType() + " ('" + t.getValue() + "')");
    }

    //  Function call
    private ASTNode.FunctionCall parseFunctionCall() {
        String name = consume().getValue(); // function name
        expect(TokenType.LPAREN);

        List<ASTNode> args = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            args.add(parseExpr());
            while (check(TokenType.COMMA)) {
                consume(); // eat ','
                args.add(parseExpr());
            }
        }

        expect(TokenType.RPAREN);
        return new ASTNode.FunctionCall(name, args);
    }
}