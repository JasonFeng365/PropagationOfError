import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.TreeMap;

public class Propagation {
    private String equation;
    private String wordEquation;
    private Term function;
    private static final int DELAY = 10;
    private static final TreeMap<Character, Character> shiftKeys;
    private Robot robot;

    static {
        shiftKeys = new TreeMap<>();
        shiftKeys.put('(', '9');
        shiftKeys.put(')', '0');
        shiftKeys.put('*', '8');
        shiftKeys.put('^', '6');
        shiftKeys.put('+', '=');
        shiftKeys.put('_', '-');
        shiftKeys.put('%', '5');
        shiftKeys.put('{', '[');
        shiftKeys.put('}', ']');
    }
    public Propagation(String function, String equation) throws AWTException {
        this.equation = equation;
        this.function = new Term(function);
        robot = new Robot();

        wordEquation = calculate();
    }

    public String getWordEquation(){
        return wordEquation;
    }

    private String calculate(){
        int index = equation.indexOf('/');
        Term[] numVars;
        Term[] denVars;
        if (index!=-1) {
            String numerator = equation.substring(0, index);
            String denominator = equation.substring(index + 1);

            if (numerator.equals("1")){
                numVars = new Term[]{};
            } else {
                String[] strings = numerator.split("\\*");
                numVars = new Term[strings.length];
                for (int i = 0; i < strings.length; i++) {
                    numVars[i] = new Term(strings[i]);
                }
            }
            String[] strings = denominator.split("\\*");
            denVars = new Term[strings.length];
            for (int i = 0; i < strings.length; i++) {
                denVars[i] = new Term(strings[i]);
            }
        }
        else{
            String[] strings = equation.split("\\*");
            numVars = new Term[strings.length];
            for (int i = 0; i < strings.length; i++) {
                numVars[i] = new Term(strings[i]);
            }
            denVars = new Term[]{};
        }


        StringBuilder builder = new StringBuilder(100);
        int[] coefficients = new int[numVars.length+denVars.length];
        int cIndex = 0;

        // Line 1
        // opening, and first sqrt parenthesis
        builder.append("\\delta ").append(function.formatVar()).append('=').append(function.formatVar()).append("\\sqrt ( ");
        for (Term t : numVars){
            builder.append("(\\partial ").append(function.formatVar()).append("/\\partial ").append(t.formatVar()).append("  ");
            builder.append("\\delta ").append(t.formatVar()).append('/').append(function.formatVar()).append(" ) ^2 +");
        }
        for (Term t : denVars){
            builder.append("(\\partial ").append(function.formatVar()).append("/\\partial ").append(t.formatVar()).append("  ");
            builder.append("\\delta ").append(t.formatVar()).append('/').append(function.formatVar()).append(" ) ^2 +");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(") \n");



        // Line 2
        builder.append("\\delta ").append(function.formatVar()).append('=').append(function.formatVar()).append("\\sqrt ( ");

        // Numerators: decrease power, then increase.
        for (Term t : numVars){
            coefficients[cIndex]=t.power();
            cIndex++;

            builder.append('(');
            if (t.power()!=1) builder.append(t.power());
            t.decreasePower();

            for (Term sub : numVars){
                builder.append(sub.format());
            }
            builder.append('/');
            for (Term sub : denVars){
                builder.append(sub.format());
            }
            builder.append("  ");
            t.increasePower();

            builder.append(inverseOriginal(numVars, denVars)).append(' ');
            builder.append("\\delta ").append(t.formatVar()).append(") ^2 +");
        }

        // Numerators: increase power, then decrease. Make negative.
        for (Term t : denVars){
            coefficients[cIndex]=t.power();
            cIndex++;

            builder.append("(-");
            if (t.power()!=1) builder.append(t.power());
            t.increasePower();

            for (Term sub : numVars){
                builder.append(sub.format());
            }
            builder.append('/');
            for (Term sub : denVars){
                builder.append(sub.format());
            }
            builder.append("  ");
            t.decreasePower();

            builder.append(inverseOriginal(numVars, denVars)).append(' ');
            builder.append("\\delta ").append(t.formatVar()).append(") ^2 +");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(") \n");



        // Line 3
        // last line- 3 and 4 are same
        String lastString;
        {
            StringBuilder last = new StringBuilder();
            cIndex=0;
            last.append("\\delta ").append(function.formatVar()).append('=').append(function.formatVar()).append("\\sqrt ( ");
            for (Term t : numVars){
                last.append("(\\delta ");
                if (coefficients[cIndex]!=1) last.append(coefficients[cIndex]);
                last.append(t.formatVar()).append('/');
                last.append(t.formatVar()).append(" ) ^2 +");
                cIndex++;
            }
            for (Term t : denVars){
                last.append("(-\\delta ");
                if (coefficients[cIndex]!=1) last.append(coefficients[cIndex]);
                last.append(t.formatVar()).append('/');
                last.append(t.formatVar()).append(" ) ^2 +");
                cIndex++;
            }
            last.deleteCharAt(last.length()-1);
            last.append(") \n");

            lastString = last.toString();
        }

        builder.append(lastString.repeat(2));



        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

    private String inverseOriginal(Term[] numVars, Term[] denVars){
        StringBuilder builder = new StringBuilder();

        if (denVars.length==0) builder.append('1');
        else for (Term t : denVars) builder.append(t.format());

        builder.append('/');

        if (numVars.length==0) builder.append('1');
        else for (Term t : numVars) builder.append(t.format());

        builder.append(' ');

        return builder.toString();
    }

    private void backspace(int delay){
        robot.keyPress(KeyEvent.VK_BACK_SPACE);
        robot.delay(delay);
        robot.keyRelease(KeyEvent.VK_BACK_SPACE);
        robot.delay(delay);
    }

    public void type(){
        type(wordEquation);
    }

    public void type(String s){
        robot.delay(3000);
        newEquation(DELAY);
        for (char c : s.toCharArray()){
//            typeChar(c, DELAY);
            if (c!='\n') typeChar(c, DELAY);
            else newEquation(DELAY);
        }
    }

    private void typeChar(char c, int delay){
        if (shiftKeys.containsKey(c)){
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.delay(delay);
            robot.keyPress(shiftKeys.get(c));
            robot.delay(delay);
            robot.keyRelease(KeyEvent.VK_SHIFT);
            robot.keyRelease(shiftKeys.get(c));
            robot.delay(delay);
            return;
        }

        if (c>='A' && c<='Z') { // lowercase letter
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.delay(delay);
            robot.keyPress(c);
            robot.delay(delay);
            robot.keyRelease(KeyEvent.VK_SHIFT);
            robot.keyRelease(c);
            robot.delay(delay);
        } else {
            if (c>='a' && c<='z') c-=32;

            robot.keyPress(c);
            robot.delay(delay);
            robot.keyRelease(c);
            robot.delay(delay);
        }
    }

    private void newEquation(int delay){
        typeChar((char)KeyEvent.VK_END, delay);
        typeChar('\n', delay);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.delay(delay);
        robot.keyPress(KeyEvent.VK_EQUALS);
        robot.delay(delay);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_EQUALS);
        robot.delay(delay);
    }
}

class Term{
    private String variable;
    private int power;
    private boolean spaceAfterVar;

    public Term(String string){
        int index = string.indexOf("^");
        if (index==-1){
            variable = string;
            power = 1;
        } else {
            variable = string.substring(0, index);
            power = Integer.parseInt(string.substring(index+1));
        }
        if (string.contains("_")) spaceAfterVar = true;
    }

    public String format(){
        if (power==1) {
            if (spaceAfterVar)
                return variable+' ';
            else return variable;
        }
        if (power==0) return "";
        return variable+(spaceAfterVar?" ^":'^')+power+' ';
    }

    public String toString(){
        if (power==1) {
            if (spaceAfterVar)
                return variable+' ';
            else return variable;
        }
        if (power==0) return "";
        return variable+"^"+power;
    }

    public String var() {
        return variable;
    }

    public String formatVar() {
        if (spaceAfterVar)
            return variable+' ';
        return variable;
    }

    public int power() {
        return power;
    }

    public void decreasePower(){
        power--;
    }

    public void increasePower(){
        power++;
    }
}