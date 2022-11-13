package amidifx.utils;

import java.io.File;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// https://www.baeldung.com/java-system-out-println-vs-loggers

public class Logger {

    private static final String fileLogOut = "logout.log";
    private static final String fileLogErr = "logerr.log";
    private static String logDirectory = "C:/amidifx/";

    public static void logSystemToFile () {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        System.out.println("Redirecting System.Out and System.Err Logger Files if Prod: " + dtf.format(now));

        AppConfig apconfig = AppConfig.getInstance();
        if (apconfig.getDebugMode() == 0) {
            setSystemErr();
            setSystemOut();
        }

        if (apconfig.getLogDirectory() != null) {
            logDirectory = apconfig.getLogDirectory();
        }
    }

    private static boolean setSystemOut() {

        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            // Create new Log Out file for every session
            File fileOut = new File(logDirectory + fileLogOut);
            if (fileOut.exists()) fileOut.delete();

            PrintStream outStream = new PrintStream(fileOut);
            System.setOut(outStream);
            System.out.println("Starting System.out logger: " + dtf.format(now));
        }
        catch (Exception ex) {
            System.err.println(" Exception while attempting to redirect System.out to: " + logDirectory + fileLogOut);
            return false;
        }

        return true;
    }

    private static boolean setSystemErr() {

        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            // Create new Log Err file for every session
            File fileErr = new File(logDirectory + fileLogErr);
            if (fileErr.exists()) fileErr.delete();

            PrintStream errStream = new PrintStream(fileErr);
            System.setErr(errStream);
            System.err.println("Starting System.err logger: " + dtf.format(now));
        }
        catch (Exception ex) {
            System.err.println(" Exception while attmnpting to redirect System.err to: " + logDirectory + fileLogErr);
            return false;
        }

        return true;
    }

}
