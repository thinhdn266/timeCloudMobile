package com.CodeEngine.ThinhDinh.timecloud.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.CodeEngine.ThinhDinh.timecloud.Model.RecordModel;
import com.CodeEngine.ThinhDinh.timecloud.R;
import com.CodeEngine.ThinhDinh.timecloud.TaskInfoActivity;

import java.util.ArrayList;

/**
 * Created by Administrator on 2/26/2018.
 */

public class TaskListViewAdapter extends BaseAdapter {

    public TaskListViewAdapter(Context _context, ArrayList<RecordModel> _records) {
        this.context = _context;
        this.records = _records;
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public Object getItem(int position) {
        return records.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        final RecordModel record = (RecordModel) getItem(position);
        if (view == null) {
            LayoutInflater li = (LayoutInflater) this.context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.tasklist_item, null);
        }
        TextView taskTextView = view.findViewById(R.id.task_name);
        taskTextView.setText(record.getTask().getName());

        TextView projectCategoryTextView = view.findViewById(R.id.project_category);
        projectCategoryTextView.setText(record.getTask().getProjectCategory());

        TextView taskSecTextView = view.findViewById(R.id.task_sec_textview);
        taskSecTextView.setText(record.getTime().secToText() + "s");

        TextView taskHourMinTextView = view.findViewById(R.id.task_hour_min_textview);
        taskHourMinTextView.setText(record.getTime().hourToText() + ":" + record.getTime().minToText());

        ImageView projectIcon = view.findViewById(R.id.project_icon);
        if (record.getTask().getCategory() == null)
            projectIcon.setBackgroundColor(context.getResources().getColor(R.color.colorGrayText));
        else
            projectIcon.setBackgroundColor(Integer.parseInt(record.getTask().getCategory().getProject().getColor()));

        ImageView taskCenter = view.findViewById(R.id.task_center);
        taskCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, TaskInfoActivity.class);
                intent.putExtra("record", record);
                context.startActivity(intent);
            }
        });

        return view;
    }

    private Context context;
    private ArrayList<RecordModel> records;
}
