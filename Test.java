public class Test {
    public static void main(String[] args) {
        if (args[0].matches("^/\\*[\\s*]*@Dhulb.*$")) {
            System.out.println("match");
        } else {
            System.out.println("no match");
        }
    }
}
