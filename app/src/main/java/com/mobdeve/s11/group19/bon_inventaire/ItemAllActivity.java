package com.mobdeve.s11.group19.bon_inventaire;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ItemAllActivity extends AppCompatActivity {

    private RecyclerView rvAllItems;
    private LinearLayoutManager llmManager;
    private ItemAllAdapter itemAllAdapter;
    private ArrayList<Item> dataItem;
    private FloatingActionButton fabAllItemsAdd;
    private ImageButton ibCancel;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private ActivityResultLauncher allItemsAddActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent intent = result.getData();

                        String name =  intent.getStringExtra(AddItemActivity.KEY_NAME);
                        String list = intent.getStringExtra(AddItemActivity.KEY_LIST);
                        int numStocks = intent.getIntExtra(AddItemActivity.KEY_NUM_STOCKS,0);
                        String expireDate = intent.getStringExtra(AddItemActivity.KEY_EXPIRE_DATE);
                        String note = intent.getStringExtra(AddItemActivity.KEY_NOTE);
                        int id = intent.getIntExtra(AddItemActivity.KEY_ID,0);

                        dataItem.add(0 , new Item(name, list, note, numStocks, expireDate, id));
                        itemAllAdapter.notifyItemChanged(0);
                        itemAllAdapter.notifyItemRangeChanged(0, itemAllAdapter.getItemCount());
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_items);

        initFirebase();
        initConfiguration();
        initRecyclerView();
        initAllItemsAdd();
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

    private void initRecyclerView () {
        Toast.makeText(getApplicationContext(), "Retrieving items from the database...", Toast.LENGTH_SHORT).show();

        mDatabase.getReference(Collections.users.name())
                .child(mAuth.getCurrentUser().getUid()).child(Collections.items.name())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GenericTypeIndicator<ArrayList<Item>> t = new GenericTypeIndicator<ArrayList<Item>>() {};
                        dataItem =  snapshot.getValue(t);

                        if(dataItem == null){
                            dataItem = new ArrayList<Item>();
                            dataItem.add(new Item("Example Item", "Example Item", "Example Item", 1,"2022",0));
                        }

                        rvAllItems = findViewById(R.id.rv_all_items);

                        llmManager = new LinearLayoutManager(ItemAllActivity.this, LinearLayoutManager.VERTICAL, false);
                        rvAllItems.setLayoutManager(llmManager);


                        itemAllAdapter = new ItemAllAdapter(dataItem);
                        rvAllItems.setAdapter(itemAllAdapter);

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Can't retrieve data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initAllItemsAdd() {
        this.fabAllItemsAdd = findViewById(R.id.fab_all_items_add);
        this.fabAllItemsAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ItemAllActivity.this, AddItemActivity.class);

                allItemsAddActivityResultLauncher.launch(intent);
            }
        });
    }

    private void initCancel() {
        this.ibCancel = findViewById(R.id.ib_all_items_back);
        this.ibCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}