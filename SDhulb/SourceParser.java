package SDhulb;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class SourceParser {
    public static ArrayList<Token<?>> parse(String source) {// parse source file into token array
        ArrayList<Token<?>> lst = new ArrayList<>();
        try (FileInputStream fIn = new FileInputStream(new File(source))) {
            int cchar = 0;// current character
            while (cchar != -1) {// read file contents
                if (cchar == '/') {// lots of stuff starts with slashes
                    int tchar = fIn.read();// test character
                    if (tchar == -1) {// check for EOF
                        SDhulb.wasErr = true;
                        SDhulb.errMsg = "Unexpected EOF after '/'";
                        return lst;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append('/');
                    if (tchar == '*') {// comment
                        sb.append('*');
                        boolean starflag = false;// whether the last character was a '*'
                        while (true) {// go through comment
                            cchar = fIn.read();
                            sb.append((char) cchar);
                            if (cchar == '*') {
                                starflag = true;
                                continue;
                            }
                            if (starflag && cchar == '/') {
                                break;
                            }
                            starflag = false;
                        }
                        String str = sb.toString();
                        if (str.matches("^/\\*[\\s*]*@Dhulb.*$")) {
                            lst.add(new Token<String>(TokenType.Comment, str));
                        }
                    }
                }
                cchar = fIn.read();
            }
        } catch (Exception E) {// internal exception, get stack trace
            SDhulb.wasErr = true;
            StringBuilder sb = new StringBuilder();
            StackTraceElement[] ste = E.getStackTrace();
            for (StackTraceElement st : ste) {
                sb.append(st.toString());
            }
            SDhulb.errMsg = sb.toString();
        }
        return lst;
    }
}
