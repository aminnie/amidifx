package amidifx.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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

        AppConfig apconfig = AppConfig.getInstance();

        // Read log directory override
        if (apconfig.getLogDirectory() != null) {
            logDirectory = apconfig.getLogDirectory();
        }

        // Mode = 0: Only log Err in Production to Log File
        // Mode = 1: Log Out and Err in Production Mode to Log File
        // Mode = 2: Log Out and Err to IDE Console Debug Console
        if (apconfig.getDebugMode() == 0) {
            System.out.println("Logger Prod(0): Redirecting System.Err Logger Files to " + logDirectory + " (" + dtf.format(now) + ")");
            setSystemErr();
            setSystemOutNull();
        }
        else if (apconfig.getDebugMode() == 1) {
            System.out.println("Logger Prod(1): Redirecting System.Out and System.Err Logger Files to " + logDirectory + " (" + dtf.format(now) + ")");
            setSystemErr();
            setSystemOut();
        }
        else
            System.out.println("Logger Debug(2): System.Out and System.Err Logger Files to IDE Console (" + dtf.format(now) + ")");
    }

    // Output Stream Gobbler for System.Out
    private static boolean setSystemOutNull() {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {

            }
        }));
        return true;
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
