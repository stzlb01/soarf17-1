import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Calendar;

/*
 * SOAR Project Fall 2017
 * Advisor: Thyago Mota
 * Student: Zach Balga
 * Description: this class will get the system current date/time and save it in a local file
 */
public class Target {

    private static final String FILE_NAME = Target.class + ".txt";

    public static void main(String[] args) throws Exception {
        PrintWriter out = new PrintWriter(new FileWriter(Target.FILE_NAME));
        Calendar calendar = Calendar.getInstance();
        out.println(calendar.getTime());
        out.close();
    }
}
