package com.mobdeve.s11.group19.bon_inventaire;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListViewHolder> {

    public static final String KEY_NAME = "KEY_NAME";
    public static final String KEY_DESCRIPTION = "KEY_DESCRIPTION";
    public static final String KEY_ID = "KEY_ID";

    private ArrayList<List> dataList;

    public ListAdapter(ArrayList<List> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.all_list_list, parent, false);

        ListViewHolder listViewHolder = new ListViewHolder(itemView);

        listViewHolder.getClAllList().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ItemListActivity.class);

                intent.putExtra(KEY_NAME, dataList.get(listViewHolder.getBindingAdapterPosition()).getListName());
                intent.putExtra(KEY_DESCRIPTION, dataList.get(listViewHolder.getBindingAdapterPosition()).getListDescription());
                intent.putExtra(KEY_ID, dataList.get(listViewHolder.getBindingAdapterPosition()).getListID());

                v.getContext().startActivity(intent);
            }
        });

        return listViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull ListViewHolder holder, int position) {
        holder.setTvAllListName(dataList.get(position).getListName());
    }

    @Override
    public int getItemCount() {
        return this.dataList.size();
    }
}
