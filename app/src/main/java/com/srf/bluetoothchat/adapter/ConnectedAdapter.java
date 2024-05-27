package com.srf.bluetoothchat.adapter;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.recyclerview.widget.RecyclerView;

import com.srf.bluetoothchat.R;
import com.srf.bluetoothchat.activity.ConnectActivity;
import com.srf.bluetoothchat.model.OnItemClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ConnectedAdapter extends RecyclerView.Adapter<ConnectedAdapter.ViewHolder> {
    private Context context;
    private ArrayList<BluetoothDevice> deviceArrayList;
    private static final int REQUEST_BLUETOOTH_CONNECT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public ConnectedAdapter(Context context, ArrayList<BluetoothDevice> deviceArrayList) {
        this.context = context;
        this.deviceArrayList = deviceArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.device_connect_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = deviceArrayList.get(position);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_CONNECT);
            return;
        }
        holder.txtDeviceConnect.setText(device.getName());
        holder.txtDeviceMAC.setText(device.getAddress());
        holder.btnConnect.setOnClickListener(v -> {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());

            try {
                BluetoothSocket socket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgConnected;
        TextView txtDeviceConnect, txtDeviceMAC, txtDeviceStatus;
        Button btnConnect;
        ProgressBar pbConnect;
        LinearLayout layoutConnected;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgConnected = itemView.findViewById(R.id.imgConnected);
            txtDeviceConnect = itemView.findViewById(R.id.txtDeviceConnect);
            txtDeviceMAC = itemView.findViewById(R.id.txtDeviceMAC);
            txtDeviceStatus = itemView.findViewById(R.id.txtDeviceStatus);
            btnConnect = itemView.findViewById(R.id.btnConnect);
            pbConnect = itemView.findViewById(R.id.pbConnect);
            layoutConnected = itemView.findViewById(R.id.layoutConnected);
        }
    }
}
