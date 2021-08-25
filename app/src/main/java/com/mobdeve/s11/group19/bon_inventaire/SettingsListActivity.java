package com.mobdeve.s11.group19.bon_inventaire;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SettingsListActivity extends AppCompatActivity {

    public static final String KEY_LIST = "KEY_LIST";
    public static final String KEY_DESCRIPTION = "KEY_DESCRIPTION";
    public static final String KEY_ID = "KEY_ID";

    private TextView tvEdit;
    private TextView tvDelete;
    private ImageButton ibBack;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_list);

        initFirebase();
        initConfiguration();
        initEdit();
        initDelete();
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

    private void initEdit() {
        this.tvEdit = findViewById(R.id.tv_settings_list_edit);
        this.tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsListActivity.this, EditListActivity.class);

                Intent info = getIntent();
                Toast.makeText(getApplicationContext(), info.getStringExtra(ItemListActivity.KEY_LIST), Toast.LENGTH_SHORT).show();


                intent.putExtra(KEY_LIST, info.getStringExtra(ItemListActivity.KEY_LIST));
                intent.putExtra(KEY_DESCRIPTION, info.getStringExtra(ItemListActivity.KEY_DESCRIPTION));
                intent.putExtra(KEY_ID, info.getIntExtra(ItemListActivity.KEY_ID,0));

                startActivity(intent);
                finish();
            }
        });
    }

    private void initDelete() {
        this.tvDelete = findViewById(R.id.tv_settings_list_delete);
        this.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();

                String name = intent.getStringExtra(ItemListActivity.KEY_LIST);
                String description = intent.getStringExtra(ItemListActivity.KEY_DESCRIPTION);
                int id = intent.getIntExtra(ItemListActivity.KEY_ID,0);

                if (name.length() > 0 && description.length() > 0) {
                    //database
                    List list = new List(name,description,id);
                    retrieveList(list);
                }
                else
                    Toast.makeText(getApplicationContext(), "fields must not be empty. desu~ kusa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void retrieveList(List list) {
        Toast.makeText(getApplicationContext(), "Adding item to the database...", Toast.LENGTH_SHORT).show();

        mDatabase.getReference(Collections.users.name())
                .child(mAuth.getCurrentUser().getUid()).child(Collections.lists.name())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GenericTypeIndicator<ArrayList<List>> t = new GenericTypeIndicator<ArrayList<List>>() {};
                        ArrayList<List> allList = snapshot.getValue(t);

                        int index = findIndex(allList,list);

                        String listName = allList.get(index).getListName();
                        allList.remove(index);

                        storeItem(allList,listName);

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Can't retrieve data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int findIndex(ArrayList<List> allList, List list){
        int sentinel = 0;
        for(int i = 0; i < allList.size(); i++) {
            List tempList = allList.get(i);
            if(tempList.getListID() == list.getListID()){
                return i;
            }
        }
        return sentinel;
    }

    private void storeItem(ArrayList<List> allList, String listName) {

        mDatabase.getReference(Collections.users.name())
                .child(mAuth.getCurrentUser().getUid()).child(Collections.lists.name())
                .setValue(allList)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            retrieveItem(allList, listName);

                        } else {
                            Toast.makeText(getApplicationContext(), "Can't Edit to the database", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void retrieveItem(ArrayList<List> allList, String listName) {
        Toast.makeText(getApplicationContext(), "Editing item to the database...", Toast.LENGTH_SHORT).show();

        mDatabase.getReference(Collections.users.name())
                .child(mAuth.getCurrentUser().getUid()).child(Collections.items.name())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GenericTypeIndicator<ArrayList<Item>> t = new GenericTypeIndicator<ArrayList<Item>>() {};
                        ArrayList<Item> allItem = snapshot.getValue(t);

                        allItem = moveList(allItem,listName);
                        storeItem(allList, allItem);

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Can't retrieve data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private ArrayList<Item> moveList(ArrayList<Item> allItem, String listName){
        ArrayList<Item> tempAllItem = allItem;

        for(int i = 0; i < allItem.size(); i++) {
            Item tempItem = allItem.get(i);
            if(tempItem.getItemList().equals(listName)){
                tempItem.setItemList("Unlisted");
                tempAllItem.set(i,tempItem);
            }
        }

        return tempAllItem;
    }

    private void storeItem(ArrayList<List> allList, ArrayList<Item> allItem) {

        mDatabase.getReference(Collections.users.name())
                .child(mAuth.getCurrentUser().getUid()).child(Collections.items.name())
                .setValue(allItem)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {

                            Toast.makeText(getApplicationContext(), "Successfully Added to the database", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SettingsListActivity.this, HomeActivity.class);

                            intent.putExtra(KEY_LIST, allList.get(1).getListName());
                            intent.putExtra(KEY_DESCRIPTION, allList.get(1).getListDescription());
                            intent.putExtra(KEY_ID, allList.get(1).getListID());

                            setResult(Activity.RESULT_OK, intent);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Can't Add to the database", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initBack() {
        this.ibBack = findViewById(R.id.ib_settings_list_back);
        this.ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsListActivity.this, ListActivity.class);

                startActivity(intent);
            }
        });
    }
}