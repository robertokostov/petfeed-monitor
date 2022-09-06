package com.example.roberto.petfeedmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.roberto.heartratemonitor.R;

import java.util.ArrayList;
import java.util.List;
import me.aflak.bluetooth.Bluetooth;

public class SelectBluetooth extends AppCompatActivity {
    private Bluetooth bt;
    private ListView listView;
    private Button not_found;
    private boolean registered=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_bluetooth);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        registered=true;

        bt = new Bluetooth(this);
        bt.enableBluetooth();
        listView =  (ListView)findViewById(R.id.list);
        not_found =  (Button) findViewById(R.id.not_in_list);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(SelectBluetooth.this, MainPage.class);
            i.putExtra("pos", position);
            if(registered) {
                unregisterReceiver(mReceiver);
                registered=false;
            }
            startActivity(i);
            finish();
        });

        not_found.setOnClickListener(v -> {
            Intent i = new Intent(SelectBluetooth.this, ScanBluetooth.class);
            startActivity(i);
        });

        addDevicesToList();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(registered) {
            unregisterReceiver(mReceiver);
            registered=false;
        }
    }

    private void addDevicesToList(){
        List<BluetoothDevice> paired = bt.getPairedDevices();

        List<String> names = new ArrayList<>();
        for (BluetoothDevice d : paired){
            names.add(d.getName());
        }

        String[] array = names.toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, array);

        listView.setAdapter(adapter);

        not_found.setEnabled(true);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        runOnUiThread(() -> listView.setEnabled(false));
                        Toast.makeText(SelectBluetooth.this, "Вклучете Bluetooth!", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        runOnUiThread(() -> {
                            addDevicesToList();
                            listView.setEnabled(true);
                        });
                        break;
                }
            }
        }
    };
}

