package com.CodeEngine.ThinhDinh.timecloud.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.CodeEngine.ThinhDinh.timecloud.Model.AccountModel;
import com.CodeEngine.ThinhDinh.timecloud.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 3/15/2018.
 */

public class MemberListViewAdapter extends BaseAdapter {

    public MemberListViewAdapter(Context context, ArrayList<AccountModel> accounts) {
        this.context = context;
        this.accounts = accounts;
    }

    @Override
    public int getCount() {
        return accounts.size();
    }

    @Override
    public Object getItem(int position) {
        return accounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        AccountModel account = (AccountModel) getItem(position);
        if (view == null) {
            LayoutInflater li = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.memberlist_item, null);
        }

        TextView email = view.findViewById(R.id.email_textview);
        email.setText(account.getEmail());

        Button role = view.findViewById(R.id.role_btn);
        role.setText(account.getRole());
        if (account.getRole().equals("Admin"))
            role.setBackground(view.getResources().getDrawable(R.drawable.btn_green_shadow));
        return view;
    }

    private ArrayList<AccountModel> accounts;
    private Context context;
}
