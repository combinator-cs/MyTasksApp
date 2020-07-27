package com.example.MyTasks.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class TaskHelper extends SQLiteOpenHelper {

    private static final String TAG = "MainActivity";


    public TaskHelper(Context context){
        super(context, TaskDatabase.DB_NAME, null, TaskDatabase.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){

        Log.i(TAG , "Creating table");
        String query = "CREATE TABLE "  + TaskDatabase.TaskEntry.TABLE + " ( " + TaskDatabase.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TaskDatabase.TaskEntry.COL_TASK_TITLE + " TEXT NOT NULL , " + TaskDatabase.TaskEntry.COL_TASK_DATE + " TEXT );" ;
        db.execSQL(query);
        Log.i(TAG , "Created table");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        String query = "DROP TABLES IF EXISTS " + TaskDatabase.TaskEntry.TABLE;
        onCreate(db);
    }

}
