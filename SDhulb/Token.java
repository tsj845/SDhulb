package SDhulb;

public class Token<T> {
    public TokenType type;
    public T data;
    public Token(TokenType type, T data) {
        this.type = type;
        this.data = data;
    }
}
