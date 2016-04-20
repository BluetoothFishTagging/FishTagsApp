package bft.fishtagsapp.Storage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import bft.fishtagsapp.R;

public class StorageActivty extends AppCompatActivity {
    EditText msgEdit;
    EditText nameEdit;

    Button saveBtn;
    Button readBtn;
    Button deleteBtn;

    Storage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        storage = new Storage(getApplicationContext(),"FishTagsData");

        msgEdit = (EditText) findViewById(R.id.msgEdit);
        nameEdit = (EditText) findViewById(R.id.nameEdit);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        readBtn = (Button) findViewById(R.id.readBtn);
        deleteBtn = (Button) findViewById(R.id.deleteBtn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEdit.getText().toString();
                String msg = msgEdit.getText().toString();
                storage.save(name,msg);
            }
        });
        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEdit.getText().toString();
                String msg = storage.read(name);
                msgEdit.setText(msg);
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEdit.getText().toString();
                storage.delete(name);
            }
        });
    }
}
