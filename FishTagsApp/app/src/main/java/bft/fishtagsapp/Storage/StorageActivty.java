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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        Storage.register(this,"testData");
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
                Storage.save(name,msg);
            }
        });

        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEdit.getText().toString();
                String msg = Storage.read(name);
                msgEdit.setText(msg);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEdit.getText().toString();
                Storage.delete(name);
            }
        });
    }
}
