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
                        if (str.matches("(?s)/\\*[\\s*]*@Dhulb.*")) {
                            lst.add(new Token<String>(TokenType.Comment, str));
                        }
                    } else if (tchar == '&' || tchar == '%') {
                        boolean eflag = false;// whether the last character was the one used to open the assembly
                        while (true) {// go through assembly
                            cchar = fIn.read();
                            sb.append((char) cchar);
                            if (cchar == tchar) {
                                eflag = true;
                                continue;
                            }
                            if (eflag && cchar == '/') {
                                break;
                            }
                            eflag = false;
                        }
                        TokenType ttype = null;
                        if (tchar == '&') {
                            ttype = TokenType.TextAsm;
                        } else {
                            ttype = TokenType.DataAsm;
                        }
                        lst.add(new Token<String>(ttype, sb.substring(1, sb.length()-2)));
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
