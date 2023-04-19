package SDhulb;

public class SDhulb {
    static String vStr = "0.0.0.1"; /*Rightmost: incremented every commit. Second from right: incremented upon feature implementation. Second from left: incremented upon implementation of a set of related features. Leftmost: incremented every breaking change. NO NUMBERS INCREMENTED FROM COMMITS CONSISTING OF COMMENTS EXCLUSIVELY */
    public static boolean wasErr = false;
    public static String errMsg = null;
    public static void main(String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("--help")) {
            System.out.println("sDhulb --[help|version]\nsDhulb --test [cat] [spec]\nsDhulb [source] [destination]");
            return;
        }
        if (args[0].equalsIgnoreCase("--version")) {
            System.out.println("sDhulb version: " + vStr);
            return;
        }
        if (args[0].equalsIgnoreCase("--test")) {
            if (args.length < 3) {
                Fmt.printErr("not enough args for --test");
                return;
            }
            if (args[1].equalsIgnoreCase("msg")) {
                Fmt.printInfo("testing msg");
                switch (args[2]) {
                    case "info":
                        Fmt.printInfo("test");
                        break;
                    case "ok":
                        Fmt.printOk("test");
                        break;
                    case "warn":
                        Fmt.printWarn("test");
                        break;
                    case "err":
                        Fmt.printErr("test");
                        break;
                    default:
                        Fmt.printErr("unrecognized message type");
                        break;
                }
                return;
            }
            return;
        }
        if (args.length < 2) {
            Fmt.printErr("must provide both source and destination");
            return;
        }
        Fmt.printOk("SOURCE: " + args[0] + " DESTINATION: " + args[1]);
    }
}
