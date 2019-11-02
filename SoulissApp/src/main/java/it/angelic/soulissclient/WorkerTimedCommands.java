package it.angelic.soulissclient;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.LinkedList;

import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.db.SoulissDBHelper;

import static it.angelic.soulissclient.Constants.TAG;
import static it.angelic.soulissclient.util.NotificationStaticUtil.sendProgramNotification;

/**
 * TEST with
 * adb shell dumpsys activity service GcmService  | findstr it.angelic
 */

public class WorkerTimedCommands extends Worker {
    private final SoulissDBHelper db;
    private Context context;

    public WorkerTimedCommands(@NonNull Context appContext, @NonNull WorkerParameters params) {

        super(appContext, params);
        context = appContext;
        db = new SoulissDBHelper(context);
    }

    @Override
    public Result doWork() {
        // Do your work here.
        // Timed commands
        int execd = 0;

        SoulissDBHelper.open();
        LinkedList<SoulissCommand> unexecuted = db.getUnexecutedCommands(context);
        Log.w(TAG, String.format("checking %d unexecuted TIMED commands ", unexecuted.size()));
        for (SoulissCommand unexnex : unexecuted) {
            Calendar now = Calendar.getInstance();
            if (unexnex.getType() == Constants.COMMAND_TIMED
                    && now.after(unexnex.getScheduledTime())) {
                // esegui comando
                Log.w(TAG, "issuing command: " + unexnex.toString());
                unexnex.execute();
                //unexnex.persistCommand();
                // Se ricorsivo, ricrea
                if (unexnex.getInterval() > 0) {
                    SoulissCommand nc = new SoulissCommand(
                            unexnex.getParentTypical());
                    nc.setNodeId(unexnex.getNodeId());
                    nc.setSlot(unexnex.getSlot());
                    nc.setCommand(unexnex.getCommand());
                    nc.setInterval(unexnex.getInterval());
                    Calendar cop = Calendar.getInstance();
                    cop.add(Calendar.SECOND, unexnex.getInterval());
                    nc.setScheduledTime(cop);
                    //l'ho appena eseguito, questo e` il 'figlio'
                    nc.setExecutedTime(Calendar.getInstance());
                    nc.setType(Constants.COMMAND_TIMED);
                    nc.persistCommand();
                    Log.w(TAG, "recreate recursive command");
                }
                sendProgramNotification(context, context.getString(R.string.timed_program_executed),
                        unexnex.toString() + " " + unexnex.getParentTypical().toString(),
                        R.drawable.clock1, unexnex);
                execd++;
            } else if (unexnex.getType() != Constants.COMMAND_TIMED) {
                //this is only a check
                Log.e(TAG, "WTF? nt TIMED?? " + unexnex.getType());
            }
        }
        // db.close();

        Data outputData = new Data.Builder()
                .putString("checkedPrograms", "" + unexecuted.size())
                .putString("executedCommands", "" + execd)
                .build();
        return Result.success(outputData);
    }

    @Override
    public void onStopped() {
        // Cleanup because you are being stopped.
        db.close();
    }
}