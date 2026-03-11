public class Lexer {

    private String input;
    private int position;
    private char currentChar;

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.currentChar = input.length() > 0 ? input.charAt(0) : '\0';
    }

    // Moves to the next character in the input
    private void advance() {
        position++;
        if (position >= input.length()) {
            currentChar = '\0';
        } else {
            currentChar = input.charAt(position);
        }
    }

    // Skips any whitespace characters
    private void skipWhitespace() {
        while (Character.isWhitespace(currentChar)) {
            advance();
        }
    }

    // Skips single-line comments starting with '#'
    private void skipComment() {
        if (currentChar == '#') {
            while (currentChar != '\n' && currentChar != '\0') {
                advance();
            }
        }
    }

    // Reads an integer or float number token
    // FIX: ensures only one '.' is allowed in a number
    private Token number() {
        StringBuilder result = new StringBuilder();
        boolean hasDot = false;

        while (Character.isDigit(currentChar) || (currentChar == '.' && !hasDot)) {
            if (currentChar == '.') {
                hasDot = true;
            }
            result.append(currentChar);
            advance();
        }

        return new Token(TokenType.NUMBER, result.toString());
    }

    // Reads an identifier or keyword token (def, sin, cos)
    private Token identifier() {
        StringBuilder result = new StringBuilder();

        while (Character.isLetterOrDigit(currentChar) || currentChar == '_') {
            result.append(currentChar);
            advance();
        }

        String word = result.toString();

        switch (word) {
            case "def": return new Token(TokenType.DEF, word);
            case "sin": return new Token(TokenType.SIN, word);
            case "cos": return new Token(TokenType.COS, word);
            default:    return new Token(TokenType.IDENTIFIER, word);
        }
    }

    // Returns the next token from the input stream
    public Token getNextToken() {

        while (currentChar != '\0') {

            // Skip whitespace
            if (Character.isWhitespace(currentChar)) {
                skipWhitespace();
                continue;
            }

            // Skip comments
            if (currentChar == '#') {
                skipComment();
                continue;
            }

            // Identifier or keyword
            if (Character.isLetter(currentChar) || currentChar == '_') {
                return identifier();
            }

            // Integer or float number
            if (Character.isDigit(currentChar)) {
                return number();
            }

            // Operators and punctuation
            switch (currentChar) {
                case '+':
                    advance();
                    return new Token(TokenType.PLUS, "+");
                case '-':
                    advance();
                    return new Token(TokenType.MINUS, "-");
                case '*':
                    advance();
                    return new Token(TokenType.MULTIPLY, "*");
                case '/':
                    advance();
                    return new Token(TokenType.DIVIDE, "/");
                case '=':
                    advance();
                    return new Token(TokenType.ASSIGN, "=");
                case '(':
                    advance();
                    return new Token(TokenType.LPAREN, "(");
                case ')':
                    advance();
                    return new Token(TokenType.RPAREN, ")");
                case ',':
                    advance();
                    return new Token(TokenType.COMMA, ",");
                default:
                    throw new RuntimeException("Unknown character: '" + currentChar + "' at position " + position);
            }
        }

        return new Token(TokenType.EOF, "EOF");
    }
}