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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ItemListActivity extends AppCompatActivity {

    public static final String KEY_LIST = "KEY_LIST";
    public static final String KEY_DESCRIPTION = "KEY_DESCRIPTION";
    public static final String KEY_ID = "KEY_ID";

    private RecyclerView rvListItems;
    private LinearLayoutManager llmManager;
    private ItemListAdapter itemListAdapter;
    private ArrayList<Item> dataItem;
    private FloatingActionButton fabListItemsAdd;
    private FloatingActionButton fabListItemsSettings;
    private ImageButton ibBack;

    private TextView tvTitle;
    private TextView tvDescription;

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
                        itemListAdapter.notifyItemChanged(0);
                        itemListAdapter.notifyItemRangeChanged(0, itemListAdapter.getItemCount());
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list_items);

        this.tvTitle = findViewById(R.id.tv_list_items_title);
        this.tvDescription = findViewById(R.id.tv_list_items_description);

        Intent intent = getIntent();

        String name =  intent.getStringExtra(ListAdapter.KEY_NAME);
        String description = intent.getStringExtra(ListAdapter.KEY_DESCRIPTION);

        tvTitle.setText(name);
        tvDescription.setText(description);

        initFirebase();
        initConfiguration();
        initRecyclerView();
        initListItemsAdd();
        initListItemsEdit();
        initBack();
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

                        Intent intent = getIntent();
                        String name =  intent.getStringExtra(ListAdapter.KEY_NAME);

                        if(dataItem == null){
                            dataItem = new ArrayList<Item>();
                            dataItem.add(new Item("Example Item", name, "Example Item", 1,"2022",0));
                        }

                        filterList(dataItem);

                        rvListItems = findViewById(R.id.rv_list_items);

                        llmManager = new LinearLayoutManager(ItemListActivity.this, LinearLayoutManager.VERTICAL, false);
                        rvListItems.setLayoutManager(llmManager);

                        itemListAdapter = new ItemListAdapter(dataItem);
                        rvListItems.setAdapter(itemListAdapter);

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Can't retrieve data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterList (ArrayList<Item> dataItem) {
        Intent intent = getIntent();

        String name =  intent.getStringExtra(ListAdapter.KEY_NAME);
        ArrayList<Item> tempDataItem = new ArrayList<Item>();

        for(int i = 0; i < dataItem.size(); i++) {
            Item tempItem = dataItem.get(i);
            if(!tempItem.getItemList().equals(name)) {
                tempDataItem.add(tempItem);
            }
        }

        this.dataItem.removeAll(tempDataItem);

    }

    private void initListItemsAdd() {
        this.fabListItemsAdd = findViewById(R.id.fab_list_items_add);
        this.fabListItemsAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ItemListActivity.this, AddItemActivity.class);

                Intent info = getIntent();

                intent.putExtra(KEY_LIST, info.getStringExtra(ListAdapter.KEY_NAME));


                allItemsAddActivityResultLauncher.launch(intent);
            }
        });
    }

    private void initListItemsEdit() {
        this.fabListItemsSettings = findViewById(R.id.fab_list_items_settings);
        this.fabListItemsSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ItemListActivity.this, SettingsListActivity.class);

                Intent info = getIntent();

                intent.putExtra(KEY_LIST, info.getStringExtra(ListAdapter.KEY_NAME));
                intent.putExtra(KEY_DESCRIPTION, info.getStringExtra(ListAdapter.KEY_DESCRIPTION));
                intent.putExtra(KEY_ID, info.getIntExtra(ListAdapter.KEY_ID,0));

                startActivity(intent);
            }
        });
    }

    private void initBack() {
        this.ibBack = findViewById(R.id.ib_list_items_back);
        this.ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadData() {
        Intent intent = getIntent();

        String list = intent.getStringExtra(EditListActivity.KEY_LIST);
        String desc = intent.getStringExtra(EditListActivity.KEY_DESCRIPTION);
        int id = intent.getIntExtra(EditListActivity.KEY_ID,0);

        tvTitle.setText(list);
        tvDescription.setText(desc);

        initRecyclerView();

    }

    @Override
    protected void onResume() {
        super.onResume();

        this.loadData();
    }
}