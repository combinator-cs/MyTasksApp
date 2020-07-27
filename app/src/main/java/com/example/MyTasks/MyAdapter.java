package com.example.MyTasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.MyTasks.db.TaskDatabase;
import com.example.MyTasks.db.TaskHelper;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private static final String TAG = "MainActivity";

    private List<String> valuesTask;
    private List<String> valuesDate;
    Context context;
    Activity activity;

    TaskHelper mHelper;


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView taskName;
        public TextView taskDate;
        public View layout;
        public Button doneButton;

        public ViewHolder(View v){
            super(v);
            layout = v;
            taskName = (TextView) v.findViewById(R.id.task_name);
            taskDate = (TextView) v.findViewById(R.id.task_date);
            doneButton = (Button) v.findViewById(R.id.task_done);

            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Log.i(TAG, "ON LONG CLICK ENTERED");
                    mHelper = new TaskHelper(context);
                    final SQLiteDatabase db = mHelper.getWritableDatabase();

                    final ArrayList<String> dataTask = new ArrayList<>();
                    final ArrayList<String> dataDate = new ArrayList<>();

                    int position = getAdapterPosition()+1;
                    Log.i(TAG, Integer.toString(position));
                    int counter = 0;
                    int idx = -1;
                    Cursor cursor = db.query(TaskDatabase.TaskEntry.TABLE, new String[]{TaskDatabase.TaskEntry._ID, TaskDatabase.TaskEntry.COL_TASK_TITLE, TaskDatabase.TaskEntry.COL_TASK_DATE},null,null,null,null,null );
                    if(cursor == null)
                        return true;
                    else{
                        while (cursor.moveToNext()) {
                            counter++;
                            if (counter == position) {
                            }
                            else {
                                dataTask.add(cursor.getString(cursor.getColumnIndex(TaskDatabase.TaskEntry.COL_TASK_TITLE)));
                                dataDate.add(cursor.getString(cursor.getColumnIndex(TaskDatabase.TaskEntry.COL_TASK_DATE)));
                            }

                        }
                        counter = 0;
                        cursor = db.query(TaskDatabase.TaskEntry.TABLE, new String[]{TaskDatabase.TaskEntry._ID, TaskDatabase.TaskEntry.COL_TASK_TITLE, TaskDatabase.TaskEntry.COL_TASK_DATE},null,null,null,null,null );
                        while (cursor.moveToNext()) {
                            counter++;
                            if (counter == position) {
                                idx = cursor.getColumnIndex(TaskDatabase.TaskEntry.COL_TASK_TITLE);
                                break;
                            }

                        }

                        final Cursor cursor1 = cursor;
                        final int idx1 = idx;
                        Log.i(TAG, Integer.toString(idx1));
                        final AlertDialog dialog = new AlertDialog.Builder(context).setTitle("Do you really want to delete this Task?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String taskToDelete = cursor1.getString(idx1);
                                db.delete(TaskDatabase.TaskEntry.TABLE, TaskDatabase.TaskEntry.COL_TASK_TITLE + " = ?", new String[]{taskToDelete});
                                cursor1.close();
                                db.close();
                                notifyDataSetChanged();
                                update(dataTask, dataDate);
                            }
                        }).setNegativeButton("No", null).create();
                        dialog.show();

                    }
                    return true;
                }
            });

        }

    }

    public void add(int position, String itemTask, String itemDate){
        valuesTask.add(position, itemTask);
        valuesDate.add(position, itemDate);
        notifyItemInserted(position);
    }

    public void remove(int position){
        valuesTask.remove(position);
        valuesDate.remove(position);
        notifyItemRemoved(position);
    }


    public MyAdapter(List<String> myDataSetTask, List<String> myDataSetDate, Context context, Activity activity){
        valuesTask = myDataSetTask;
        valuesDate = myDataSetDate;
        this.activity = activity;
        this.context = context;
    }

    public void update(ArrayList<String> dataTask, ArrayList<String> dataDate){
        valuesTask.clear();
        valuesTask.addAll(dataTask);
        valuesDate.clear();
        valuesDate.addAll(dataDate);
        notifyDataSetChanged();
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.my_list_view, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position){
        final String name = valuesTask.get(position);
        final String date = valuesDate.get(position);
        holder.taskName.setText(name);
        holder.taskDate.setText(date);

    }

    @Override
    public int getItemCount(){
        return valuesTask.size();
    }




}
