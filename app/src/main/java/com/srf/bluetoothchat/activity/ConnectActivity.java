package com.srf.bluetoothchat.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.srf.bluetoothchat.R;
import com.srf.bluetoothchat.adapter.AvailableAdapter;
import com.srf.bluetoothchat.adapter.ConnectedAdapter;
import com.srf.bluetoothchat.databinding.ActivityConnectBinding;

import java.util.ArrayList;
import java.util.Set;

public class ConnectActivity extends AppCompatActivity {

    ActivityConnectBinding connectBinding;
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectedAdapter connectedAdapter;
    private AvailableAdapter availableAdapter;
    private ArrayList<BluetoothDevice> pairedDevicesList;
    private ArrayList<BluetoothDevice> availableDeviceList;
    private final static int LOCATION_PERMISSION_REQUEST = 1;
    private final int SELECT_DEVICE = 101;

    // Kiem tra trang thai bluetooth
    private BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (state == BluetoothAdapter.STATE_OFF) {
                Toast.makeText(context, "Bluetooth đã tắt", Toast.LENGTH_SHORT).show();
                connectBinding.txtBluetoothStatus.setVisibility(View.VISIBLE);
                connectBinding.btnBluetooth.setImageResource(R.drawable.bluetooth_off);
            }else{
                connectBinding.txtBluetoothStatus.setVisibility(View.GONE);
                connectBinding.btnBluetooth.setImageResource(R.drawable.bluetooth_on);
            }
        }
    };

    // Tra ve danh sach thiet bi quet duoc
    private BroadcastReceiver bluetoothDeviceReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                checkBluetoothPermission();
                assert device != null;
                if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                    availableDeviceList.add(device);
                    availableAdapter.notifyDataSetChanged();
                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                connectBinding.pbScan.setVisibility(View.GONE);
                Toast.makeText(context, "Hoàn thành quét thiết bị", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectBinding = ActivityConnectBinding.inflate(getLayoutInflater());
        setContentView(connectBinding.getRoot());

        context = this;

        registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(bluetoothDeviceReceive, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(bluetoothDeviceReceive, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        // Thiet lap recycle view
        connectBinding.rvConnectedDevice.setLayoutManager(new LinearLayoutManager(context));
        pairedDevicesList = new ArrayList<>();

        connectBinding.rvAvailableDevice.setLayoutManager(new LinearLayoutManager(context));
        availableDeviceList = new ArrayList<>();

        setupBluetooth();

        // Load lai du lieu khi vuot tu tren xuong
        connectBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setupBluetooth();
                scanDevices();
            }
        });

        connectBinding.btnBluetooth.setOnClickListener(v -> {
            if(bluetoothAdapter!=null && !bluetoothAdapter.isEnabled()) {
                enableBluetooth();
                scanDevices();
            }else{
                Toast.makeText(context, "Bluetooth đã bật", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Thiet lap bluetooth
    private void setupBluetooth() {
        checkBluetoothPermission();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Thiết bị không hỗ trợ bluetooth", Toast.LENGTH_SHORT).show();
        }else {
            if(bluetoothAdapter.isEnabled()){
                connectBinding.btnBluetooth.setImageResource(R.drawable.bluetooth_on);
                // Hien thi ten thiet bi
                connectBinding.txtDevice.setText(bluetoothAdapter.getName());

                // Lay danh sach thiet bi da ket noi
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                pairedDevicesList.clear();
                if(pairedDevices.size() > 0){
                    pairedDevicesList.addAll(pairedDevices);
                }
                connectedAdapter = new ConnectedAdapter(context,pairedDevicesList);
                connectBinding.rvConnectedDevice.setAdapter(connectedAdapter);

                // Lay danh sach thiet bi kha dung
                availableAdapter = new AvailableAdapter(context,availableDeviceList);
                connectBinding.rvAvailableDevice.setAdapter(availableAdapter);

                connectBinding.swipeRefreshLayout.setRefreshing(false);
            }else{
                connectBinding.btnBluetooth.setImageResource(R.drawable.bluetooth_off);
                connectBinding.txtBluetoothStatus.setVisibility(View.VISIBLE);
                connectBinding.rvConnectedDevice.removeAllViews();
                Toast.makeText(context, "Bluetooth đang tắt", Toast.LENGTH_SHORT).show();

                connectBinding.swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    // Kich hoat bluetooth
    private void enableBluetooth() {
        checkBluetoothPermission();
        bluetoothAdapter.enable();
        if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(intent);
        }
    }

    // Quet tim cac thiet bi moi
    private void scanDevices(){
        if(bluetoothAdapter!=null && bluetoothAdapter.isEnabled()) {
            connectBinding.pbScan.setVisibility(View.VISIBLE);
            availableDeviceList.clear();

            checkBluetoothPermission();
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            bluetoothAdapter.startDiscovery();
        }
    }

    //kiem tra quyen ket noi thiet bi lan can
    private void checkBluetoothPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setMessage("Quyền truy cập vị trí bị từ chối, hãy cấp quyền")
                        .setPositiveButton("Cho phép", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkBluetoothPermission();
                            }
                        })
                        .setNegativeButton("Từ chối", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothStateReceiver);
        unregisterReceiver(bluetoothDeviceReceive);
    }
}