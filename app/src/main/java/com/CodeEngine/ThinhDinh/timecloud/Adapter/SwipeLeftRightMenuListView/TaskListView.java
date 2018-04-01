package com.CodeEngine.ThinhDinh.timecloud.Adapter.SwipeLeftRightMenuListView;

import android.content.Context;
import android.view.ViewGroup;


/**
 * Created by Administrator on 2/26/2018.
 */

public class TaskListView extends SwipeMenuListView{
    public TaskListView(Context context) {
        super(context);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }

    public static interface OnItemClickListener {
        void onItemClick(int position, SwipeMenu menu, int index);
    }
}
