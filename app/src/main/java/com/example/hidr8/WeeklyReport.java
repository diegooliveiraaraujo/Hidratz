package com.example.hidr8;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class WeeklyReport extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_report);

        //create list views to hold the daily input data
        ListView dateList = findViewById(R.id.date_data);
        ListView timeList = findViewById(R.id.time_data);
        ListView amountList = findViewById(R.id.amount_data);


        DatabaseHelper db = new DatabaseHelper(this);

        //populate array adapters with the appropriate data from the database
        Map<String, String> dataConsumo = db.getDate();
        ArrayList<String> listData = new ArrayList<>();
        ArrayList<String> listaConsumo = new ArrayList<>();
        String i = "";
        Iterator<Map.Entry<String, String>> it = dataConsumo.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pair = it.next();
            listData.add(pair.getKey());
            listaConsumo.add(pair.getValue());
        }
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listData);
        ArrayAdapter<String> amountAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaConsumo);
        dateList.setAdapter(dateAdapter);
        amountList.setAdapter(amountAdapter);
    }

    //on click method for the go back button
    public void goBackOnClick(View view) {
        Intent intent = new Intent(WeeklyReport.this, MainActivity.class);
        startActivity(intent);
    }
}
