package com.example.hidratz;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.AlarmManager;

import com.example.hidratz.R;

import org.json.JSONObject;

import java.sql.Time;
import java.util.Date;

public class MainActivity extends AppCompatActivity{

    //variable used to hold the text field that displays the water amount
    TextView waterAmountText;
    //variable used to connect to the progress bar
    ProgressBar waterProgressBar;
    //variable used to connect to the water bottle image that is used as a button
    ImageButton waterBottle;
    //variable used to connect to the weekly report button
    Button weeklyReportButton;
    Spinner dropdown;
    //variable used to hold the current containerSize
    float containerSize;
    //variable used to hold the goal amount
    float goal;
    //variable used to define the amount that the progress bar should advance based on the container size
    float incrementCount;
    //variable used to hold the currentAmount of water that has been drank for the day
    float currentAmount;

    //For recurring notifications
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    //string array that contains the titles of buttons in the navigation drawer
    private String[] navigationDrawerItemTitles;
    //variable that connects to the DrawerLayout in the xml file
    private DrawerLayout drawerLayout;
    //ListView that is contained inside of the navigation drawer
    private ListView drawerList;
    //variable that holds the hamburger button ActionBarDrawerToggle
    ActionBarDrawerToggle drawerToggle;

    SharedPreferences pref;

    TextView recommendedGoal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //stores the default shared preferences into pref
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = pref.edit();


        ManageDrawables.setDefaultDayOfWeekColors(this, Color.GRAY);


        Date date = new Date();
        if(pref.getString("date", "empty").equals("empty")) {
            edit.putString("date", date.toString());
        }

        if(pref.getString("date", "empty").compareTo(date.toString()) < 0) {
            edit.putString("date", date.toString());
            edit.putFloat("current_amount", 0);
        }

        //this flips all of the layout elements and is a quick and dirty solution to open the navigation drawer from right to left
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        navigationDrawerItemTitles = getResources().getStringArray(R.array.navigation_drawer_items_main_array);

        drawerLayout = findViewById(R.id.drawer_main_layout);
        drawerList = findViewById(R.id.left_drawer);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        //removes the title of the app from the toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recommendedGoal = findViewById(R.id.recommended_goal);
        String weight = pref.getString("weight", "140");
        String height = pref.getString("height", "140");
        try{
            new GetWebServiceData().execute(new JSONObject("{'weight': "+weight+", 'height': "+height+" }"));
        } catch (Exception e){

        }

        //array adapter created for the titles in the navigation drawer
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, navigationDrawerItemTitles);
        drawerList.setAdapter(adapter);

        //listener that switches activities when the user presses a button in the navigation drawer
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        //when first item is selected
                        startActivity(new Intent(MainActivity.this, Settings.class));
                        break;
                    case 1:
                        //when second item is selected
                        startActivity(new Intent(MainActivity.this, Alarm.class));
                        break;
                }
            }
        });

        //set drawerLayout to the DrawerLayout that is the top level layout in activity_main.xml
        drawerLayout = findViewById(R.id.drawer_main_layout);
        //add a drawer listener that allows the navigation drawer to open when the hamburger button is pressed
        drawerLayout.addDrawerListener(drawerToggle);
        //create a new ActionBarDrawerToggle that is the hamburger button that will be pressed
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.app_name, R.string.app_name);
        //This is necessary to change the icon of the Drawer Toggle upon state change.
        drawerToggle.syncState();

        waterAmountText = findViewById(R.id.waterAmount);
        waterProgressBar = findViewById(R.id.progressBar);
        waterBottle = findViewById(R.id.WaterBottle);
        weeklyReportButton = findViewById(R.id.weeklyReport);
        dropdown = findViewById(R.id.spinner1);
        //get the spinner from the xml.
        //create a list of items for the spinner.
        String[] items = new String[]{"100", "250", "500"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter1);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                containerSize = pref.getFloat("container_size", Float.parseFloat(parent.getItemAtPosition(position).toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //defines values based on the default shared preferences
        goal = Float.parseFloat(pref.getString("goal", "0"));
        incrementCount = (containerSize/goal) * 100;
        currentAmount = pref.getFloat("current_amount", 0);

        waterProgressBar.setProgress((int)((currentAmount / goal) * 100));

        //sets the text above the waterProgressBar to the currentAmount next to the goal value
        waterAmountText.setText(currentAmount/1000 + " L / " + goal/1000 + " L");

        //creates a OnClickListener for the weeklyReportButton that starts the WeeklyReport activity
        //when the button is clicked
        weeklyReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //code to update the circles to the right colors based on the database goes here
                DatabaseHelper db = new DatabaseHelper(MainActivity.this);
                int[] completeDays = db.getCompleteDays();
                //resets colors before the colors are updated from the database
                ManageDrawables.setDefaultDayOfWeekColors(MainActivity.this, Color.GRAY);
                ManageDrawables.setDayOfWeekColors(completeDays, MainActivity.this);
                Intent intent = new Intent(MainActivity.this, WeeklyReport.class);
                startActivity(intent);
            }
        });

    }

    public void waterOnClick(View view) {
        //increments the waterProgressBar by the amount determined by the container size
        waterProgressBar.incrementProgressBy(Math.round(incrementCount));
        //increases the currentAmount based on the current containerSize
        currentAmount += Math.round(containerSize);
        waterAmountText.setText(currentAmount/1000 + " L / " + goal/1000 + " L");
        SharedPreferences.Editor edit = pref.edit();
        edit.putFloat("current_amount", currentAmount);
        edit.apply();

        java.util.Date utilDate = new java.util.Date();
        java.sql.Date date = new java.sql.Date(utilDate.getTime());
        Time time = new Time(utilDate.getTime());
        DatabaseHelper db = new DatabaseHelper(this);
        db.insertData(time, date, goal, currentAmount, containerSize);
    }


    //class used to get data from the custom web service and set the value of a text view
    public class GetWebServiceData extends AsyncTask {

        //method that will run in a separate worker thread to access the web service the weight
        //value will be passed into this method as a string to change the json that is returned
        @Override
        protected Object doInBackground(Object[] objects) {
            String responseText;
            String returnedGoal = "";

            try {
                Integer peso = Integer.parseInt(new JSONObject(objects[0].toString()).get("weight").toString());
                float objetivo = (float) (peso * 35) / 1000;

                responseText = "{goal: "+objetivo+"}";
                JSONObject jsonResponse = new JSONObject(responseText);

                //if the json has a goal field set the value of returned goal to that value
                if (jsonResponse.has("goal")) {
                    returnedGoal = jsonResponse.getString("goal");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //return the goal retrieved from the webservice
            return returnedGoal;
        }

        //method that uses the goal returned from doInBackground() and set the text of
        //the recommendedGoal text view
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            System.out.println(o);
            recommendedGoal.setText("Meta sugerida: " + (String)o + " l");
        }
    }



}

