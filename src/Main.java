import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
//        System.out.println(new Propagation("f", "a*b/c").getWordEquation());
        Scanner s = new Scanner(System.in);
        propagate(s);
        System.exit(0);

        Robot robot = new Robot();
        robot.delay(3000);

        Scanner scanner = new Scanner(new File("Excel.txt"));
        while (scanner.hasNextLine()){
            String string = scanner.nextLine().toUpperCase(Locale.ROOT);
            int backs = 0;
            for(char c : string.toCharArray()){
                if (c == '\t') backs++;

                    robot.keyPress(c);
                    robot.delay(10);
                    robot.keyRelease(c);
                    robot.delay(10);

            }
            robot.keyPress(KeyEvent.VK_DOWN);
            robot.delay(10);
            robot.keyRelease(KeyEvent.VK_DOWN);
            robot.delay(10);
            for (int i = 0; i < backs; i++) {
                robot.keyPress(KeyEvent.VK_LEFT);
                robot.delay(10);
            }
        }
    }

    static void propagate(Scanner scanner){
        System.out.print("Enter function name: ");
        String func = scanner.nextLine();
        System.out.print("Enter coefficients: ");
        String coefficients = scanner.nextLine();
        System.out.print("Enter expression: ");
        String eq = scanner.nextLine();

        try {
            new Propagation(func, coefficients, eq).type();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}