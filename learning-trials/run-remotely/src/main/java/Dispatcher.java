import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.security.InvalidParameterException;

/*
 * SOAR Project Fall 2017
 * Advisor: Thyago Mota
 * Student: Zach Balga
 * Description: this class will run Target remotely
 */
public class Dispatcher {

    private static final String REMOTE_CLASSPATH = "~/Development/temp";
    private static final String JAR_FILE = "run-remotely";
    private static final String MAIN_CLASS = "Target";

    Dispatcher(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Use: " + Dispatcher.class + " user@host");
            throw new InvalidParameterException();
        }
        String userAtHost = args[0];;
        DefaultExecutor executor = new DefaultExecutor();
        CommandLine cmdLine = new CommandLine("ssh");
        cmdLine.addArgument(userAtHost);
        cmdLine.addArgument("\"java -cp " + Dispatcher.REMOTE_CLASSPATH + " " + Dispatcher.MAIN_CLASS + "\"", false);
        System.out.println("Running: " + cmdLine);
        executor.execute(cmdLine);
    }

    public static void main(String[] args) throws Exception {
        new Dispatcher(args);
    }
}
