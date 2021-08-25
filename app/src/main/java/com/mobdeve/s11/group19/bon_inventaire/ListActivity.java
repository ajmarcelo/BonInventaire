package com.mobdeve.s11.group19.bon_inventaire;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    private RecyclerView rvAllLists;
    private LinearLayoutManager llmManager;
    private ListAdapter listAdapter;
    protected ArrayList<List> dataList;
    private FloatingActionButton fabAllListAdd;
    private ImageButton ibCancel;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;


    private ActivityResultLauncher allListAddActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent intent = result.getData();

                        String name =  intent.getStringExtra(AddListActivity.KEY_NAME);
                        String desc = intent.getStringExtra(AddListActivity.KEY_DESCRIPTION);
                        int id = intent.getIntExtra(AddListActivity.KEY_ID,0);

                        dataList.add(1 , new List(name,desc,id));
                        listAdapter.notifyItemChanged(0);
                        listAdapter.notifyItemRangeChanged(0, listAdapter.getItemCount());
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_lists);

        initFirebase();
        initConfiguration();
        initRecyclerView();
        initAllListAdd();
        initCancel();
    }

    private void initFirebase() {
        this.mAuth = FirebaseAuth.getInstance();
        this.mDatabase = FirebaseDatabase.getInstance();
    }

    private void initConfiguration() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void initRecyclerView() {
        Toast.makeText(getApplicationContext(), "Retrieving list from the database...", Toast.LENGTH_SHORT).show();

        mDatabase.getReference(Collections.users.name())
                .child(mAuth.getCurrentUser().getUid()).child(Collections.lists.name())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GenericTypeIndicator<ArrayList<List>> t = new GenericTypeIndicator<ArrayList<List>>() {};
                        dataList =  snapshot.getValue(t);

                        rvAllLists = findViewById(R.id.rv_all_lists);

                        llmManager = new LinearLayoutManager(ListActivity.this, LinearLayoutManager.VERTICAL, false);
                        rvAllLists.setLayoutManager(llmManager);

                        listAdapter = new ListAdapter(dataList);
                        rvAllLists.setAdapter(listAdapter);

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Can't retrieve data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initAllListAdd() {
        this.fabAllListAdd = findViewById(R.id.fab_all_lists_add);
        this.fabAllListAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListActivity.this, AddListActivity.class);

                allListAddActivityResultLauncher.launch(intent);
            }
        });
    }

    private void initCancel() {
        this.ibCancel = findViewById(R.id.ib_all_lists_back);
        this.ibCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}