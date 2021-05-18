package com.example.hidr8;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class Alarm extends AppCompatActivity {

    private static final String KEY_NOTIFY = "key_notify";
    private static final String KEY_INTERVAL = "key_interval";
    private static final String KEY_HOUR = "key_hour";
    private static final String KEY_MINUTE = "key_minute";

    private TimePicker timePicker;
    private Button btnNotify;
    private EditText editMinutes;
    private SharedPreferences storage;
    private boolean activated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        storage = getSharedPreferences("storage", Context.MODE_PRIVATE);

        timePicker = findViewById(R.id.timer_picker);
        btnNotify = findViewById(R.id.button_notify);
        editMinutes = findViewById(R.id.edit_number_interval);

        activated = storage.getBoolean(KEY_NOTIFY, false);

        setupUI(activated, storage);

        timePicker.setIs24HourView(true);

        btnNotify.setOnClickListener(notifyListener);
    }

    private void alert(int resId) {
        Toast.makeText(Alarm.this, resId, Toast.LENGTH_SHORT).show();
    }

    private boolean intervalIsValid() {
        String sInterval = editMinutes.getText().toString();

        if (sInterval.isEmpty()) {
            alert(R.string.validation);
            return false;
        }
        if (sInterval.equals("0")) {
            alert(R.string.zero_value);
            return false;
        }

        return true;
    }

    private void setupUI(boolean activated, SharedPreferences storage) {
        if (activated) {
            btnNotify.setText(R.string.pause);
            btnNotify.setBackgroundResource(R.drawable.bg_button_notify);
            editMinutes.setText(String.valueOf(storage.getInt(KEY_INTERVAL, 0)));
            timePicker.setCurrentHour(storage.getInt(KEY_HOUR, timePicker.getCurrentHour()));
            timePicker.setCurrentMinute(storage.getInt(KEY_MINUTE, timePicker.getCurrentMinute()));

        } else {
            btnNotify.setText(R.string.notify);
            btnNotify.setBackgroundResource(R.drawable.bg_button);
        }
    }

    private void updateStorage(boolean added, int interval, int hour, int minute) {
        SharedPreferences.Editor edit = storage.edit();
        edit.putBoolean(KEY_NOTIFY, added);

        if (added) {
            edit.putInt(KEY_INTERVAL, interval);
            edit.putInt(KEY_HOUR, hour);
            edit.putInt(KEY_MINUTE, minute);
        } else {
            edit.remove(KEY_INTERVAL);
            edit.remove(KEY_HOUR);
            edit.remove(KEY_MINUTE);
        }

        edit.apply();
    }

    private void setupNotification(boolean added, int interval, int hour, int minute) {
        Intent notificationIntent = new Intent(Alarm.this, NotificationPublisher.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (added) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION_ID, 1);
            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION, "Hora de beber " +
                    "água!");

            PendingIntent broadcast = PendingIntent.getBroadcast(Alarm.this, 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    interval * 60 * 1000, broadcast);
        } else {
            PendingIntent broadcast = PendingIntent.getBroadcast(Alarm.this, 0,
                    notificationIntent, 0);

            alarmManager.cancel(broadcast);

        }
    }

    private View.OnClickListener notifyListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (!activated) {

                if (!intervalIsValid()) {
                    return;
                }

                int interval = Integer.parseInt(editMinutes.getText().toString());
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();

                updateStorage(true, interval, hour, minute);
                setupUI(true, storage);
                setupNotification(true, interval, hour, minute);

                alert(R.string.notified);

                activated = true;
            } else {

                updateStorage(false, 0, 0, 0);
                setupUI(false, storage);
                setupNotification(true, 0, 0, 0);

                alert(R.string.notified_pause);

                activated = false;
            }

            InputMethodManager im =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(editMinutes.getWindowToken(), 0);
        }
    };
}