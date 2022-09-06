package com.example.roberto.petfeedmonitor;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roberto.heartratemonitor.R;

import java.util.Objects;

import me.aflak.bluetooth.Bluetooth;

public class MainPage extends AppCompatActivity implements Bluetooth.CommunicationCallback {
    private Bluetooth b;

    private Button button_yes;
    private Button button_no;

    private boolean registered = false;

    TextView question_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_feeder);

        question_text = (TextView)findViewById(R.id.question_text);

        button_no = (Button)findViewById(R.id.button_no);
        button_yes = (Button)findViewById(R.id.button_yes);

        b = new Bluetooth(this);
        b.enableBluetooth();

        b.setCommunicationCallback(this);

        int pos = Objects.requireNonNull(getIntent().getExtras()).getInt("pos");

        Toast.makeText(this, "Се поврзува...", Toast.LENGTH_SHORT).show();
        b.connectToDevice(b.getPairedDevices().get(pos));

        button_yes.setOnClickListener(v -> {
            String msg = "y";
            b.send(msg);

            Toast.makeText(MainPage.this, "Feeding...", Toast.LENGTH_LONG).show();
        });

        button_no.setOnClickListener(v -> {
            String msg = "n";
            b.send(msg);

            Toast.makeText(MainPage.this, "Not feeding...", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(registered) {
            unregisterReceiver(mReceiver);
            registered=false;
        }
    }

    @Override
    public void onConnect(BluetoothDevice device) {
        this.runOnUiThread(() -> {
            button_yes.setEnabled(true);
            button_no.setEnabled(true);
        });
    }

    @Override
    public void onDisconnect(BluetoothDevice device, String message) {
        Toast.makeText(this, "Изгубено поврзување!", Toast.LENGTH_SHORT).show();
        b.connectToDevice(device);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onMessage(String message) {
        question_text.setText(message);
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, "Грешкa: "+ message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectError(final BluetoothDevice device, String message) {
        runOnUiThread(() -> {
            Handler handler = new Handler();
            handler.postDelayed(() -> b.connectToDevice(device), 2000);
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        assert action != null;
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            Intent intent1 = new Intent(MainPage.this, SelectBluetooth.class);

            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                case BluetoothAdapter.STATE_TURNING_OFF:
                if(registered) {
                    unregisterReceiver(mReceiver);
                    registered=false;
                }
                startActivity(intent1);
                finish();
                break;
            }
        }
        }
    };
}