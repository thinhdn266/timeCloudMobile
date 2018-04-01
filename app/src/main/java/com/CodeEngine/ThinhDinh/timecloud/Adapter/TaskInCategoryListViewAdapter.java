package com.CodeEngine.ThinhDinh.timecloud.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.CodeEngine.ThinhDinh.timecloud.MainActivity;
import com.CodeEngine.ThinhDinh.timecloud.Model.CategoryModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.TaskModel;
import com.CodeEngine.ThinhDinh.timecloud.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2/22/2018.
 */

public class TaskInCategoryListViewAdapter extends BaseAdapter {

    public TaskInCategoryListViewAdapter(Context context, CategoryModel category, ArrayList<TaskModel> tasks) {
        this.context = context;
        this.category = category;
        this.tasks = tasks;
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Object getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final TaskModel task = (TaskModel) getItem(position);
        if (view == null) {
            LayoutInflater li = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.categorylist_item, null);
        }

        TextView categoryTextView = view.findViewById(R.id.category_textview);
        categoryTextView.setText(task.getName());

        ImageView playBtn = view.findViewById(R.id.play_btn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),MainActivity.class);
                intent.putExtra("task",task);
                view.getContext().startActivity(intent);
            }
        });

        return view;
    }

    private Context context;
    private CategoryModel category;
    private ArrayList<TaskModel> tasks;
}
