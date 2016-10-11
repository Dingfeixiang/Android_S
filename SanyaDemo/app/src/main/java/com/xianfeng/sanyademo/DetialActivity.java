package com.xianfeng.sanyademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

public class DetialActivity extends AppCompatActivity {

    EditText    username,address,number,gasAmount;
    TextView    moneyView;
    Button      submit,facture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detial);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = (EditText) findViewById(R.id.accountEdittext);
        address = (EditText) findViewById(R.id.pwdEdittext);
        number = (EditText) findViewById(R.id.ntext);
        gasAmount = (EditText) findViewById(R.id.atext);

        moneyView = (TextView) findViewById(R.id.mvalue);

        submit = (Button) findViewById(R.id.read);
        facture = (Button) findViewById(R.id.write);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.clear).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:

                break;
        }
        return true;
    }

}
