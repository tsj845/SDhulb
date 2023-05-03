package SDhulb;

public class Token<T> {
    public TokenType type;
    public T data;
    public Token(TokenType type, T data) {
        this.type = type;
        this.data = data;
    }
    public String toString() {
        String v;
        if (data.getClass() == String.class) {
            v = data.toString().replace("\n", "\\n");
        } else {
            v = data.toString();
        }
        return "Token<"+type+">("+v+")";
    }
}
