package com.CodeEngine.ThinhDinh.timecloud.Adapter;

/**
 * Created by Administrator on 2/21/2018.
 */


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.CodeEngine.ThinhDinh.timecloud.Model.CategoryModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.ProjectModel;
import com.CodeEngine.ThinhDinh.timecloud.NewCategoryActivity;
import com.CodeEngine.ThinhDinh.timecloud.R;

import java.util.ArrayList;



public class ProjectListViewAdapter extends BaseExpandableListAdapter implements Filterable {

    public ProjectListViewAdapter(Context context, ArrayList<ProjectModel> projectList) {
        this.context = context;
        this.projectList = projectList;
        this.originProjectList = projectList;
    }

    @Override
    public int getGroupCount() {
        return this.projectList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return projectList.get(groupPosition).getCategories().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return projectList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return projectList.get(groupPosition).getCategories().get(childPosititon);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosititon) {
        return childPosititon;
    }

    @Override
    public View getGroupView(int groupPosition, boolean b, View view, ViewGroup viewGroup) {
        final ProjectModel project = (ProjectModel) getGroup(groupPosition);
        if (view == null) {
            LayoutInflater li = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.choose_projectlist_header, null);
        }

        ExpandableListView mExpandableListView = (ExpandableListView) viewGroup;
        mExpandableListView.expandGroup(groupPosition);

        TextView tvHeader = view.findViewById(R.id.projectlist_header);
        tvHeader.setTypeface(null, Typeface.BOLD);
        tvHeader.setText(project.getName());

        ImageView imgHeader = view.findViewById(R.id.project_icon);
        if (project.getColor() != null)
            imgHeader.setBackgroundColor(Integer.parseInt(project.getColor()));

        ImageButton addBtn = view.findViewById(R.id.add_btn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewCategoryActivity.class);
                intent.putExtra("project", project);
                context.startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean b, View view, ViewGroup viewGroup) {
        CategoryModel category = (CategoryModel) getChild(groupPosition, childPosition);
        if (view == null) {
            LayoutInflater li = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.choose_projectlist_item, null);
        }
        TextView tvItem = (TextView) view.findViewById(R.id.projectlist_item);
        tvItem.setText(category.getName());
        return view;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence search) {
                FilterResults filterResults = new FilterResults();
                ArrayList<ProjectModel> filteredProjectList = new ArrayList<>();
                search = search.toString().toLowerCase();
                Boolean isMatched;
                ArrayList<CategoryModel> matchedCategories;
                for (ProjectModel project : originProjectList) {
                    isMatched = false;
                    matchedCategories = new ArrayList<>();
                    if (project.getName().toLowerCase().contains(search)) {
                        filteredProjectList.add(project);
                    } else {
                        for (CategoryModel category : project.getCategories()) {
                            if (category.getName().toLowerCase().contains(search)) {
                                isMatched = true;
                                matchedCategories.add(category);
                            }
                        }
                        if (isMatched) {
                            filteredProjectList.add(new ProjectModel(project.getId(),project.getName(),project.getColor(),matchedCategories));
                        }
                    }

                }
                filterResults.values = filteredProjectList;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                projectList = (ArrayList<ProjectModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    private Context context;
    private ArrayList<ProjectModel> projectList;
    private ArrayList<ProjectModel> originProjectList;
}
