package SDhulb;

import java.util.ArrayList;
import java.util.Arrays;

public class SDhulb { /*Built not to handle: non UTF-8 source files  */
    static String vStr = "0.0.0.5"; /*Rightmost: incremented every commit. Second from right: incremented upon feature implementation. Second from left: incremented upon implementation of a set of related features. Leftmost: incremented every breaking change. NO NUMBERS INCREMENTED FROM COMMITS CONSISTING OF COMMENTS EXCLUSIVELY */
    public static ArrayList<String> typlst = new ArrayList<>();
    public static boolean wasErr = false;
    public static String errMsg = null;
    public static char[] typchars = new char[]{' ', '$', '(', ')', '*', ',', '<', '>'};
    public static boolean isValidTypeChar(char test) {
        int min = 0;
        int max = typchars.length;
        while (true) {
            if (max == min) {
                break;
            }
            int mid = (max-min)/2+min;
            if (typchars[mid] == test) {
                return true;
            }
            if (typchars[mid] > test) {
                max = mid;
            } else {
                min = mid;
            }
        }
        return false;
    }
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
        if (args[1].equalsIgnoreCase("-")) {
            ArrayList<Token<?>> res = SourceParser.parse(args[0]);
            if (wasErr) {
                Fmt.printErr(errMsg);
                return;
            }
            System.out.println(Arrays.toString(res.toArray()));
        }
    }
}
