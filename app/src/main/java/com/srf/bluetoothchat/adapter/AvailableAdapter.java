package com.srf.bluetoothchat.adapter;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.srf.bluetoothchat.R;
import com.srf.bluetoothchat.activity.LoginActivity;

import java.util.ArrayList;
import java.util.Collections;

public class AvailableAdapter extends RecyclerView.Adapter<AvailableAdapter.ViewHolder> {
    private Context context;
    private ArrayList<BluetoothDevice> deviceArrayList;
    private ArrayList<Boolean> deviceBondedStates;
    private static final int REQUEST_BLUETOOTH_CONNECT = 1;
    private BroadcastReceiver pairingReceiver;
    private boolean isReceiverRegistered = false;

    public AvailableAdapter(Context context, ArrayList<BluetoothDevice> deviceArrayList) {
        this.context = context;
        this.deviceArrayList = deviceArrayList;
        this.deviceBondedStates = new ArrayList<>(Collections.nCopies(deviceArrayList.size(), false));
        registerReceiver();
    }

    private void registerReceiver() {
        pairingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (state == BluetoothDevice.BOND_BONDED) {
                        // Ket noi voi thiet bi thanh cong
                        Toast.makeText(context, "Kết nối thành công", Toast.LENGTH_SHORT).show();
                        updateBondedDevice(device, true);
                        // Chuyen sang cua so dang nhap tai khoan
                        Intent connectedIntent = new Intent(context, LoginActivity.class);
                        connectedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(connectedIntent);
                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDING) {
                        // Ket noi that bai
                        Toast.makeText(context, "Kết nối thất bại", Toast.LENGTH_SHORT).show();
                        updateBondedDevice(device, false);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(pairingReceiver, filter);
        isReceiverRegistered = true;
    }

    private void updateBondedDevice(BluetoothDevice device, boolean isBonded) {
        int position = deviceArrayList.indexOf(device);
        if (position >= 0 && position < deviceBondedStates.size()) {
            deviceBondedStates.set(position, isBonded);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public AvailableAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.device_available_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvailableAdapter.ViewHolder holder, int position) {
        BluetoothDevice device = deviceArrayList.get(position);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_CONNECT);
            return;
        }

        holder.txtDeviceAvailable.setText(device.getName());
        holder.txtAvailableMAC.setText(device.getAddress());

        holder.btnAvailable.setOnClickListener(v -> {
            holder.btnAvailable.setVisibility(View.GONE);
            holder.pbAvailable.setVisibility(View.VISIBLE);

            device.createBond();
            Toast.makeText(context, "Đang kết nối với " + device.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return deviceArrayList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        unregisterReceiver();
    }

    private void unregisterReceiver() {
        if (isReceiverRegistered) {
            context.unregisterReceiver(pairingReceiver);
            isReceiverRegistered = false;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvailable;
        LinearLayout layoutAvailable;
        TextView txtDeviceAvailable, txtAvailableMAC, txtAvailableStatus;
        Button btnAvailable;
        ProgressBar pbAvailable;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvailable = itemView.findViewById(R.id.imgAvailable);
            txtDeviceAvailable = itemView.findViewById(R.id.txtDeviceAvailable);
            txtAvailableMAC = itemView.findViewById(R.id.txtAvailableMAC);
            txtAvailableStatus = itemView.findViewById(R.id.txtAvailableStatus);
            btnAvailable = itemView.findViewById(R.id.btnAvailable);
            pbAvailable = itemView.findViewById(R.id.pbAvailable);
            layoutAvailable = itemView.findViewById(R.id.layoutAvailable);
        }
    }
}
