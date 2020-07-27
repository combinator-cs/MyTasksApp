package com.example.MyTasks;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.example.MyTasks.db.TaskHelper;
import com.example.MyTasks.db.TaskDatabase;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private TaskHelper mHelper;

    //private DatePicker datePicker;
    private Calendar calendar;
    private int year, month, day;
    private static StringBuilder dateSelected;


    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent  = new Intent(this, NotificationReceiver.class);
        intent.putExtra("myAction", "notify");
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 30*1000, alarmIntent);
        Log.i(TAG, "alarm IN MAIN");

        mHelper = new TaskHelper(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        dateSelected = new StringBuilder().append(day).append("/").append(month).append("/").append(year);

        updateUI();


    }

    @Override
    public void onResume(){
        super.onResume();
        updateUI();
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() ,10*1000,  alarmIntent);
        Log.i(TAG, "alarm IN MAIN resume");
    }

    @Override
    public void onStop(){
        super.onStop();
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10*1000, alarmIntent);
        Log.i(TAG, "alarm IN MAIN stop");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_add_task:
                Log.i(TAG, "Add a new task");
                addTask(recyclerView); //here recycler view passing is just done to pass a view as was not getting which view to pass so that floating button and this function both can work
                                        // in earlier version of app without floating button addTask method working was directly implemented here.
                return true;

            case R.id.action_settings:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        ArrayList<String> taskDate = new ArrayList<>();

        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskDatabase.TaskEntry.TABLE, new String[]{TaskDatabase.TaskEntry._ID, TaskDatabase.TaskEntry.COL_TASK_TITLE, TaskDatabase.TaskEntry.COL_TASK_DATE},null,null,null,null,null );

        while (cursor.moveToNext()){
            int idxTask = cursor.getColumnIndex(TaskDatabase.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idxTask));
            int idxDate = cursor.getColumnIndex(TaskDatabase.TaskEntry.COL_TASK_DATE);
            taskDate.add(cursor.getString(idxDate));
            Log.d(TAG, "TASK: " + cursor.getString(idxTask));
        }

        if(mAdapter == null){
            mAdapter = new MyAdapter(taskList, taskDate, this, this);
            recyclerView.setAdapter(mAdapter);
        }
        else{
            mAdapter.update(taskList, taskDate);
            mAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();

    }

    public void taskDone(View view){
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_name);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskDatabase.TaskEntry.TABLE, TaskDatabase.TaskEntry.COL_TASK_TITLE + " = ?", new String[]{task});
        Log.i(TAG,"Item going to be deleted");

        /*Cursor cursor = db.query(TaskDatabase.TaskEntry.TABLE, new String[]{TaskDatabase.TaskEntry._ID, TaskDatabase.TaskEntry.COL_TASK_TITLE},null,null,null,null,null );
        int c = 0;
        while (cursor.moveToNext()){
            int idx = cursor.getColumnIndex(TaskDatabase.TaskEntry.COL_TASK_TITLE);
            if(task.compareTo(cursor.getString(idx))==1) {
                ++c;
                break;
            }
            else
                ++c;
        }*/
        updateUI();
        db.close();
        Log.i(TAG,"Item deleted");
    }


    public void addTask(View view){
        final EditText taskEditText = new EditText(this);
        final AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Add a new task!").setView(taskEditText).setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String task = String.valueOf(taskEditText.getText());
                Log.d(TAG, "Task to add: " + task);

                // Picking date and adding task when user selects from Date picker dialog

                DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        Log.i(TAG, "Setting values new");
                        int yr = i;
                        int mn = i1+1;
                        int dy = i2;

                        if(day<10 && month<10)
                            dateSelected = new StringBuilder().append(0).append(dy).append("/").append(0).append(mn).append("/").append(yr);
                        else if(day<10)
                            dateSelected = new StringBuilder().append(0).append(dy).append("/").append(mn).append("/").append(yr);
                        else if(month<10)
                            dateSelected = new StringBuilder().append(dy).append("/").append(0).append(mn).append("/").append(yr);
                        else
                            dateSelected = new StringBuilder().append(dy).append("/").append(mn).append("/").append(yr);

                        Log.i(TAG, "Setting value to " + Integer.toString(i2));
                        Log.i(TAG, "Check 3");
                        SQLiteDatabase db = mHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put(TaskDatabase.TaskEntry.COL_TASK_TITLE, task);
                        values.put(TaskDatabase.TaskEntry.COL_TASK_DATE, dateSelected.toString());
                        db.insertWithOnConflict(TaskDatabase.TaskEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                        db.close();
                        updateUI();

                    }
                };

                Log.i(TAG, "Check 1");
                final DatePickerDialog dialog1 = new DatePickerDialog(MainActivity.this, myDateListener, year, month, day);
                Log.i(TAG, "Check 2");
                dialog1.show();

                        /*
                        Log.i(TAG, "Check 1");
                        final DatePickerDialog dialog1 = new DatePickerDialog(MainActivity.this, myDateListener, year, month, day);
                        Log.i(TAG, "Check 2");
                        Button button = (Button) dialog1.getButton(dialog1.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.i(TAG, "Check 3");
                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(TaskDatabase.TaskEntry.COL_TASK_TITLE, task);
                                values.put(TaskDatabase.TaskEntry.COL_TASK_DATE, dateSelected.toString());
                                db.insertWithOnConflict(TaskDatabase.TaskEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                db.close();
                                updateUI();
                            }
                        });
                        dialog1.show();*/
            }
        }).setNegativeButton("Cancel", null).create();
        dialog.show();
    }



    // Date Dialog Methods


  /*  //Way of selected date without DatePickerFragment

    @SuppressWarnings("deprecation")
    public void setDate(View view){
        showDialog(999);
        Toast.makeText(getApplicationContext(), "ca", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("deprecation")
    protected Dialog onCreateDialog(int id){
        if(id == 999){
            return new DatePickerDialog(this, myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            Log.i(TAG, "Setting values new");
            year = i;
            month = i1+1;
            day = i2;
        }
    };
*/


  /*  public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        public static boolean isDateSet = false;

        @Override
        public Dialog onCreateDialog( Bundle savedInstanceState){

            Log.i(TAG, "Entered Dialog");
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day){
            showDate(year, month+1, day);

            dateSelected = new StringBuilder().append(day).append("/").append(month).append("/").append(year);
            dateView.setText(dateSelected);
            isDateSet = true;
        }


        private static void showDate(int year, int month, int day){
            Log.i(TAG, "Setting Date" + Integer.toString(day));

        }



    }


    public boolean showDatePickerDialog(View v){
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");


        Log.i(TAG, "DATE PICKER RETURNING TRUE");
        return true;
    }
*/




}
