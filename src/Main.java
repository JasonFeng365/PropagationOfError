import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
//        System.out.println(new Propagation("f", "a*b/c").getWordEquation());
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.print("Enter function name: ");
            String func = scanner.nextLine();
            System.out.print("Enter expression: ");
            String eq = scanner.nextLine();

            new Propagation(func, eq).type();
        }
    }
}