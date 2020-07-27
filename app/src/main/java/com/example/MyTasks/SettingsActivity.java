package com.example.MyTasks;

import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

import com.example.MyTasks.db.TaskDatabase;
import com.example.MyTasks.db.TaskHelper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private TaskHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu);
        
    }

    public void resetDatabase(View view){

        final Context context = this;
        final AlertDialog dialog = new AlertDialog.Builder(this).setTitle("All your Task List will be deleted!").setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mHelper = new TaskHelper(context);
                SQLiteDatabase db = mHelper.getWritableDatabase();
                db.execSQL("delete from "+ TaskDatabase.TaskEntry.TABLE);
                db.close();
            }
        }).setNegativeButton("Cancel", null).create();
        dialog.show();

    }


}
