
package org.telegram.messenger;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JellogramCrashHandler implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final Context context;

    public JellogramCrashHandler(Context context) {
        this.context = context;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            String crashLog = generateCrashLog(thread, throwable);
            
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("Jellogram Crash Log", crashLog);
                clipboard.setPrimaryClip(clip);
                
                new Handler(Looper.getMainLooper()).post(() -> 
                    Toast.makeText(context, "Crash log copied to clipboard!", Toast.LENGTH_LONG).show()
                );
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, throwable);
        } else {
            System.exit(1);
        }
    }

    private String generateCrashLog(Thread thread, Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        pw.println("=== Jellogram Crash Log ===");
        pw.println("Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        pw.println("App Version: " + BuildVars.BUILD_VERSION_STRING);
        pw.println("App Version Code: " + BuildVars.BUILD_VERSION);
        pw.println("Android Version: " + android.os.Build.VERSION.RELEASE);
        pw.println("Device: " + android.os.Build.MODEL);
        pw.println("Thread: " + thread.getName());
        pw.println();
        pw.println("Stack Trace:");
        throwable.printStackTrace(pw);
        
        return sw.toString();
    }
}
