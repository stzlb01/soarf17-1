/**
 * Created by meotm01 on 6/1/17.
 */
public class Main {

    public static void main(String[] args) {
        try {
            /*ProcessBuilder processBuilder = new ProcessBuilder(
                    "scp", "meotm01@gaia", "\"java -cp ~/temp Target\"");
            processBuilder.start();*/

            String command = "scp meotm01@gaia \"java -cp ~/temp Target\"";
            Runtime.getRuntime().exec(command);
        }
        catch (Exception ex) {
            System.out.println("Ops, something went wrong: " + ex);
        }
    }

}
