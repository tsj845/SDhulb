package SDhulb;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class SourceParser {
    public static ArrayList<Token<?>> parse(String source) {// parse source file into token array
        ArrayList<Token<?>> lst = new ArrayList<>();
        try (FileInputStream fIn = new FileInputStream(new File(source))) {
            int cchar = fIn.read();// current character
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
                    cchar = fIn.read();
                    continue;
                }
                if (!Character.isWhitespace(cchar)) {// whitespace is ignored unless it is a mandatory part of an instruction
                    if (!Character.isLetterOrDigit(cchar)) {// symbol
                        lst.add(new Token<Character>(TokenType.Symbol, (char) cchar));
                    } else {
                        String tok;//token contents
                        int l = 0;
                        {// free the string builder once done
                            StringBuilder sb = new StringBuilder();
                            while (cchar != -1) {// get the token
                                if (Character.isWhitespace(cchar) || (!Character.isLetterOrDigit(cchar) && cchar != '.')) {
                                    break;
                                }
                                l++;
                                sb.append((char) cchar);
                                cchar = fIn.read();
                            }
                            tok = sb.toString();
                        }
                        int start = tok.codePointAt(0);
                        int end = tok.codePointAt(l-1);
                        if (Character.isDigit(start)) {// number
                            if (l == 1) {// s32
                                lst.add(new Token<Integer>(TokenType.Literal, Integer.parseInt(tok)));
                            } else {
                                String suffix = null;
                                if (end == '8') {
                                    if (Character.isLetter(tok.codePointAt(l-2))) {
                                        suffix = tok.substring(l-2);
                                        tok = tok.substring(0, l-2);
                                    }
                                } else if (Character.isLetter(tok.codePointAt(l-3))) {
                                    suffix = tok.substring(l-3);
                                    tok = tok.substring(0, l-3);
                                }
                                boolean floatflag = tok.contains(".");
                                if (suffix == null) {// s32 or f32
                                    if (floatflag) {
                                        lst.add(new Token<Float>(TokenType.Literal, Float.parseFloat(tok)));
                                    } else {
                                        lst.add(new Token<Integer>(TokenType.Literal, Integer.parseInt(tok)));
                                    }
                                } else {
                                    suffix.intern();
                                    switch (suffix) {
                                        case ("u32"):
                                        case ("s32"):
                                            lst.add(new Token<Integer>(TokenType.Literal, Integer.parseInt(tok)));
                                            break;
                                        case ("u8"):
                                        case ("s8"):
                                            lst.add(new Token<Byte>(TokenType.Literal, Byte.parseByte(tok)));
                                            break;
                                        case ("u16"):
                                        case ("s16"):
                                            lst.add(new Token<Short>(TokenType.Literal, Short.parseShort(tok)));
                                            break;
                                        case ("u64"):
                                        case ("s64"):
                                            lst.add(new Token<Long>(TokenType.Literal, Long.parseLong(tok)));
                                            break;
                                        case ("f32"):
                                            lst.add(new Token<Float>(TokenType.Literal, Float.parseFloat(tok)));
                                            break;
                                        case ("f64"):
                                            lst.add(new Token<Double>(TokenType.Literal, Double.parseDouble(tok)));
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
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
                sb.append(st.toString()+"\n");
            }
            SDhulb.errMsg = sb.toString();
        }
        return lst;
    }
}
