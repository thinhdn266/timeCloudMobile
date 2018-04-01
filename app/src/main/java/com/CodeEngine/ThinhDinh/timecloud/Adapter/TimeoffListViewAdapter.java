package com.CodeEngine.ThinhDinh.timecloud.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.CodeEngine.ThinhDinh.timecloud.Model.TimeoffModel;
import com.CodeEngine.ThinhDinh.timecloud.R;

import org.w3c.dom.Text;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 3/23/2018.
 */

public class TimeoffListViewAdapter extends BaseAdapter {
    private Context context;
    ArrayList<TimeoffModel> timeoffModels;

    public TimeoffListViewAdapter(Context context, ArrayList<TimeoffModel> timeoffModels) {
        this.context = context;
        this.timeoffModels = timeoffModels;
    }

    @Override
    public int getCount() {
        return timeoffModels.size();
    }

    @Override
    public Object getItem(int position) {
        return timeoffModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        TimeoffModel timeoffModel = (TimeoffModel) getItem(position);
        if (view == null) {
            LayoutInflater li = (LayoutInflater) this.context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.timeofflist_item, null);
        }

        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(timeoffModel.getStartTime());
        Calendar endDate = Calendar.getInstance();
        endDate.setTimeInMillis(timeoffModel.getEndTime());

        TextView startMonth = view.findViewById(R.id.start_month);
        startMonth.setText(getMonth(startDate.get(Calendar.MONTH)));

        TextView startDay = view.findViewById(R.id.start_day);
        startDay.setText(String.valueOf(startDate.get(Calendar.DAY_OF_MONTH)));

        TextView endMonth = view.findViewById(R.id.end_month);
        endMonth.setText(getMonth(endDate.get(Calendar.MONTH)));

        TextView endDay = view.findViewById(R.id.end_day);
        endDay.setText(String.valueOf(endDate.get(Calendar.DAY_OF_MONTH)));

        TextView timeoffState = view.findViewById(R.id.time_off_state);
        if (timeoffModel.getState().equals("Rejected")) {
            timeoffState.setText("Rejected");
            timeoffState.setBackground(context.getResources().getDrawable(R.drawable
                    .background_request_rejected));
        }

        return view;
    }

    private String getMonth(long time) {
        return new SimpleDateFormat("MMM").format(new Date(time));
    }
}
