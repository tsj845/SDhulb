package SDhulb;

class Fmt {
    static String normal = "\u001b[0m";
    static String red = "\u001b[38;5;9m";
    static String green = "\u001b[38;5;10m";
    static String yellow = "\u001b[38;5;11m";
    static void printErr(String msg) {
        System.out.println(red + "ERR: " + normal + msg);
    }
    static void printOk(String msg) {
        System.out.println(green + "OK: " + normal + msg);
    }
    static void printWarn(String msg) {
        System.out.println(yellow + "WARN: " + normal + msg);
    }
    static void printInfo(String msg) {
        System.out.println("INFO: " + msg);
    }
}
