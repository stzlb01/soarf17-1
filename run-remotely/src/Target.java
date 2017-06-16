import java.io.*;

public class Target {

    public static void main(String[] args) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter("test.txt"));
            out.println("It worked!");
            out.close();
        }
        catch (Exception ex) {
            System.out.println("Ops, something went wrong: " + ex);
        }
    }
}
