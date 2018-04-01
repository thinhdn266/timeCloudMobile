package com.CodeEngine.ThinhDinh.timecloud.Adapter.SwipeLeftRightMenuListView;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SwipeMenuListView extends ListView {

    private static final int TOUCH_STATE_NONE = 0;
    private static final int TOUCH_STATE_X = 1;
    private static final int TOUCH_STATE_Y = 2;
    private final int mWidth = this.getResources().getDisplayMetrics().widthPixels;

    private int MAX_Y = 5;
    private int MAX_X = 3;
    private float mDownX;
    private float mDownY;
    private int mTouchState;
    private int mTouchPosition;
    private SwipeMenuLayout mTouchView;
    private OnSwipeListener mOnSwipeListener;

    private SwipeMenuCreator mLeftMenuCreator;
    private SwipeMenuCreator mRightMenuCreator;
    private OnMenuItemClickListener mOnMenuItemClickListener;
    private OnMenuItemClickListener mOnLeftMenuItemClickListener;
    private Interpolator mCloseInterpolator;
    private Interpolator mOpenInterpolator;

    public SwipeMenuListView(Context context) {
        super(context);
        init();
    }

    public SwipeMenuListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SwipeMenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        MAX_X = dp2px(MAX_X);
        MAX_Y = dp2px(MAX_Y);
        mTouchState = TOUCH_STATE_NONE;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(new SwipeMenuAdapter(getContext(), adapter) {

            @Override
            public void createLeftMenu(SwipeMenu menu) {
                if (mLeftMenuCreator != null) {
                    mLeftMenuCreator.create(menu);
                }
            }

            @Override
            public void createRightMenu(SwipeMenu menu) {
                if (mRightMenuCreator != null) {
                    mRightMenuCreator.create(menu);
                }
            }

            @Override
            public void onItemClick(SwipeMenuView view, SwipeMenu menu, int index) {
                if (mOnMenuItemClickListener != null) {
                    mOnMenuItemClickListener.onMenuItemClick(view.getPosition(), menu, index);
                }
                if (mTouchView != null) {
                    if (mTouchView.isOpenLeft())
                        mTouchView.smoothCloseMenu();
                }
            }
        });
    }

    public void setCloseInterpolator(Interpolator interpolator) {
        mCloseInterpolator = interpolator;
    }

    public void setOpenInterpolator(Interpolator interpolator) {
        mOpenInterpolator = interpolator;
    }

    public Interpolator getOpenInterpolator() {
        return mOpenInterpolator;
    }

    public Interpolator getCloseInterpolator() {
        return mCloseInterpolator;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() != MotionEvent.ACTION_DOWN && mTouchView == null)
            return super.onTouchEvent(ev);
        int action = MotionEventCompat.getActionMasked(ev);
        action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                int oldPos = mTouchPosition;
                mDownX = ev.getX();
                mDownY = ev.getY();
                mTouchState = TOUCH_STATE_NONE;

                mTouchPosition = pointToPosition((int) ev.getX(), (int) ev.getY());

                if (mTouchView != null && mTouchView.isOpen()) {
                    mTouchView.smoothCloseMenu();
                    mTouchView = null;
                }
                if (mTouchView != null) {
                    mTouchView.onSwipe(ev);
                }
                View view = getChildAt(mTouchPosition - getFirstVisiblePosition());
                if (view instanceof SwipeMenuLayout) {
                    mTouchView = (SwipeMenuLayout) view;
                }
                if (mTouchView.isOpen()) {
                    if (mTouchView.isOpenLeft()) {
                        if (mDownX > 120) mTouchView.smoothCloseMenu();
                    } else if (mTouchView.isOpenRight()) {
                        if (mDownX < (mWidth - 120)) mTouchView.smoothCloseMenu();
                    }
                } else if (mDownX > (mWidth - 120) && mDownX < mWidth)
                    mTouchView.smoothOpenRightMenu();
                else if (mDownX < 120) mTouchView.smoothOpenLeftMenu();
                break;
        }
        return super.onTouchEvent(ev);
    }

    public void smoothOpenRightMenu(int position) {
        if (position >= getFirstVisiblePosition() && position <= getLastVisiblePosition()) {
            View view = getChildAt(position - getFirstVisiblePosition());
            if (view instanceof SwipeMenuLayout) {
                mTouchPosition = position;
                if (mTouchView != null && mTouchView.isOpen()) {
                    mTouchView.smoothCloseMenu();
                }
                mTouchView = (SwipeMenuLayout) view;
                mTouchView.smoothOpenRightMenu();
            }
        }
    }

    public void smoothOpenLeftMenu(int position) {
        if (position >= getFirstVisiblePosition() && position <= getLastVisiblePosition()) {
            View view = getChildAt(position - getFirstVisiblePosition());
            if (view instanceof SwipeMenuLayout) {
                mTouchPosition = position;
                if (mTouchView != null && mTouchView.isOpen()) {
                    mTouchView.smoothCloseMenu();
                }
                mTouchView = (SwipeMenuLayout) view;
                mTouchView.smoothOpenLeftMenu();
            }
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }

    public void setRightMenuCreator(SwipeMenuCreator menuCreator) {
        this.mRightMenuCreator = menuCreator;
    }

    public void setLeftMenuCreator(SwipeMenuCreator menuCreator) {
        this.mLeftMenuCreator = menuCreator;
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        this.mOnMenuItemClickListener = onMenuItemClickListener;
    }

    public void setOnLeftMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        this.mOnLeftMenuItemClickListener = onMenuItemClickListener;
    }

    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        this.mOnSwipeListener = onSwipeListener;
    }

    public static interface OnMenuItemClickListener {
        void onMenuItemClick(int position, SwipeMenu menu, int index);
    }

    public static interface OnSwipeListener {
        void onSwipeStart(int position);

        void onSwipeEnd(int position);
    }
}
