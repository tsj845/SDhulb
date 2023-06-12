package SDhulb;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class SourceParser {
    private static boolean coreused = false;
    private static boolean usecore = true;
    private static HashSet<String> imported = new HashSet<>();
    private static HashMap<String, String> defined = new HashMap<>();
    public static String stdlibpath = ".";
    private static ScriptEngine javaScriptEngine = new ScriptEngineManager().getEngineByName("js");
    public static Path resolveStdlib(String name) {
        return Path.of(stdlibpath, name);
    }
    private static boolean parseIf (String[] line) throws Exception {
        line[0] = "";
        for (int i = 1; i < line.length; i ++) {
            if (defined.containsKey(line[i])) {
                line[i] = defined.get(line[i]);
            }
        }
        return (Boolean) javaScriptEngine.eval(String.join("", line));
    }
    private static ArrayList<Token<?>> postprocess(ArrayList<Token<?>> lst) {
        int i = 0;
        while (i < lst.size()) {
            Token<?> tok = lst.get(i);
            if (tok.type == TokenType.Word) {
                int ind = Arrays.binarySearch(SDhulb.keywords, tok.data);
                if (ind >= 0) {
                    tok.type = TokenType.Keyword;
                    switch (ind) {
                        case 4:// class
                        case 14:// interface
                        case 24:// typealias
                        case 25:// typedef
                        case 26:// typefullalias
                            Token<?> nt = lst.get(i+1);
                            SDhulb.typlst.add((String)nt.data);
                            nt.type = TokenType.Type;
                            break;
                        case 8:// false
                            lst.set(i, new Token<Byte>(TokenType.Literal, (byte)0));
                            break;
                        case 16:// null
                            lst.set(i, new Token<Byte>(TokenType.RawAddr, (byte)0));
                            break;
                        case 19:// struct
                        case 20:// structure
                            lst.set(i, new Token<String>(TokenType.Word, "class"));
                            i --;
                            break;
                        case 23:// true
                            lst.set(i, new Token<Byte>(TokenType.Literal, (byte)1));
                            break;
                        default:
                            break;
                    }
                } else if (SDhulb.typlst.contains(tok.data)) {
                    tok.type = TokenType.Type;
                } else if (i > 0 && lst.get(i-1).type == TokenType.Type) {
                    tok.type = TokenType.Name;
                }
            }
            if (tok.type == TokenType.Type) {
                String f = (String)tok.data;
                while (i > 0) {
                    Token<?> lt = lst.get(i-1);
                    if ((lt.type == TokenType.Symbol || lt.type == TokenType.Operator) && lt.data instanceof Character) {
                        if ((char)lt.data == '*') {
                            f = "*" + f;
                            lst.remove(i-1);
                            i --;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                lst.set(i, new Token<String>(TokenType.Type, f));
            }
            i ++;
        }
        return lst;
    }
    public static ArrayList<Token<?>> parse(String source) {
        return parse(source, new ArrayList<>());
    }
    private static ArrayList<Token<?>> parse(String source, ArrayList<Token<?>> lst) {
        String abs = Path.of(source).toAbsolutePath().toString();
        if (imported.contains(abs)) {// don't include stuff more than once
            return lst;
        }
        imported.add(abs);
        ArrayList<Token<?>> ret = parse_inner(source, lst);
        return ret;
    }
    private static ArrayList<Token<?>> parse_inner(String source, ArrayList<Token<?>> lst) {// parse source file into token array
        try (FileInputStream fIn = new FileInputStream(new File(source))) {
            int cchar = fIn.read();// current character
            boolean aftnewl = true;
            while (cchar != -1) {// read file contents
                if (cchar == '\n' || (aftnewl && Character.isWhitespace(cchar))) {// check for validity of certain statements
                    aftnewl = true;
                    cchar = fIn.read();
                    continue;
                }
                if (aftnewl && cchar == '#') {// preprocessor directive
                    String[] flin;
                    {
                        StringBuilder sb = new StringBuilder();
                        cchar = fIn.read();
                        while (cchar != -1) {
                            if (cchar == '\n') {
                                break;
                            }
                            sb.appendCodePoint(cchar);
                            cchar = fIn.read();
                        }
                        String[] manip = sb.toString().split("\"");
                        for (int i = 1; i < manip.length; i += 2) {
                            manip[i] = manip[i].replace(' ', '\u0007');
                        }
                        flin = String.join("", manip).split("[\\s]");
                        for (int i = 0; i < flin.length; i ++) {
                            flin[i] = flin[i].replace('\u0007', ' ');
                        }
                    }
                    if (flin[0].equalsIgnoreCase("nocore")) {
                        usecore = false;
                        continue;
                    } else if (usecore && !coreused) {
                        lst = parse(resolveStdlib("stdcore.sdlb").toString(), lst);
                        if (SDhulb.wasErr) {
                            return lst;
                        }
                        coreused = true;
                    }
                    if (flin[0].equalsIgnoreCase("import")) {
                        if (flin.length < 2) {
                            SDhulb.wasErr = true;
                            SDhulb.errMsg = "incomplete import statement";
                            return lst;
                        }
                        String pat;
                        if (flin[1].charAt(0) == '<') {
                            pat = resolveStdlib(flin[1].substring(1, flin[1].length()-1)).toString();
                        } else {
                            pat = Path.of(source, flin[1]).toString();
                        }
                        if (!pat.matches("^.*\\.[a-zA-Z0-9]+$")) {
                            pat = pat + "." + SDhulb.fileExt;
                        }
                        lst = parse(pat, lst);
                        if (SDhulb.wasErr) {
                            return lst;
                        }
                    } else if (flin[0].equalsIgnoreCase("info")) {
                        Fmt.printInfo(flin[1]);
                    } else if (flin[0].equalsIgnoreCase("warn")) {
                        Fmt.printWarn(flin[1]);
                    } else if (flin[0].equalsIgnoreCase("error")) {
                        SDhulb.errMsg = "\nPP DIRECTIVE ERR:\n" + flin[1];
                        SDhulb.wasErr = true;
                        return lst;
                    } else if (flin[0].equalsIgnoreCase("define")) {
                        defined.put(flin[1], flin[2]);
                    } else if (flin[0].equalsIgnoreCase("if")) {
                        //TODO: implement ifs
                    }
                    continue;
                }
                aftnewl = false;
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
                        } else {
                            lst.add(new Token<String>(TokenType.Word, tok));
                        }
                        continue;
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
            SDhulb.errMsg = sb.toString()+"SOURCE: "+source;
        }
        return postprocess(lst);
    }
}
