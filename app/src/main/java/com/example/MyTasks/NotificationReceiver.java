package com.example.MyTasks;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.MyTasks.db.TaskDatabase;
import com.example.MyTasks.db.TaskHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "MainActivity";

    private TaskHelper mHelper;

    private Calendar calendar;

    @Override
    public void onReceive(Context context, Intent intent){
        Log.i(TAG, "Alarm in receiver1");
        try {
            if(databaseCheck(context) && intent.getStringExtra("myAction") != null && intent.getStringExtra("myAction").equals("notify")){
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Log.i(TAG, "Alarm in receiver2");
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "MyNotification").setSmallIcon(R.drawable.app_launcher_icon).setContentTitle("PENDING TASKS").setContentText("You have pending tasks!!!").setOngoing(false).setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true);
                Intent intent1 = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_ONE_SHOT);
                builder.setContentIntent(pendingIntent);
                notificationManager.notify(12345, builder.build());
                Log.i(TAG, "Alarm in receiver3");
            }
        } catch (ParseException e) {
            Log.i(TAG, "Exception found");
            e.printStackTrace();
        }
    }

    public boolean databaseCheck(Context context) throws ParseException {
        Log.i(TAG,"ENTERING CHECK DB");
        mHelper = new TaskHelper(context);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskDatabase.TaskEntry.TABLE, new String[]{TaskDatabase.TaskEntry._ID, TaskDatabase.TaskEntry.COL_TASK_DATE},null,null,null,null,null );
        if(cursor == null)
            return false;
        while(cursor.moveToNext()){
            int idxDate = cursor.getColumnIndex(TaskDatabase.TaskEntry.COL_TASK_DATE);
            String dateString = cursor.getString(idxDate);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date date = sdf.parse(dateString);
            Log.i(TAG,"ENTERING CHECK DB1");

            calendar = Calendar.getInstance();
            int presentYear = calendar.get(Calendar.YEAR);
            int presentMonth = calendar.get(Calendar.MONTH) + 1;
            int presentDay = calendar.get(Calendar.DAY_OF_MONTH);
            String presentDate;
            if(presentMonth<10 && presentDay<10)
                presentDate = "0" + presentDay + "/0" + presentMonth + "/" + presentYear;
            else if(presentMonth<10)
                presentDate = presentDay + "/0" + presentMonth + "/" + presentYear;
            else if(presentDay<10)
                presentDate = "0" + presentDay + "/" + presentMonth + "/" + presentYear;
            else
                presentDate = presentDay + "/" + presentMonth + "/" + presentYear;

            Date dateToday = sdf.parse(presentDate);
            Log.i(TAG,  presentDate +" " + dateString);
            if(date.before(dateToday) || presentDate.compareTo(dateString)==0){
                cursor.close();
                db.close();
                Log.i(TAG, "Returning true cursor");
                return true;
            }

        }
        return false;
    }

}
