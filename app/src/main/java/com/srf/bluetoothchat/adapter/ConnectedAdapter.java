package com.srf.bluetoothchat.adapter;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;
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
import com.srf.bluetoothchat.activity.LoginActivity;
import com.srf.bluetoothchat.model.OnItemClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ConnectedAdapter extends RecyclerView.Adapter<ConnectedAdapter.ViewHolder> {
    private Context context;
    private ArrayList<BluetoothDevice> deviceArrayList;
    private static final int REQUEST_BLUETOOTH_CONNECT = 1;
    private static final UUID A2DP_UUID = UUID.fromString("0000110a-0000-1000-8000-00805f9b34fb"); //A2DP là giao thức Bluetooth được sử dụng để truyền âm thanh chất lượng cao từ thiết bị nguồn (như điện thoại hoặc máy tính) tới thiết bị đích (như tai nghe hoặc loa).
    private static final UUID IrDA_UUID = UUID.fromString("00001105-0000-1000-8000-00805f9b34fb"); // IrDA cho phép gửi và nhận các tệp tin (như danh bạ, hình ảnh, tài liệu) giữa các thiết bị. Mã này thường được dùng cho giao thức IrDA nhưng cũng có thể được dùng qua Bluetooth.
    private static final UUID PAN_UUID = UUID.fromString("00001115-0000-1000-8000-00805f9b34fb"); //PAN cho phép các thiết bị tạo một mạng cá nhân, ví dụ, chia sẻ kết nối internet hoặc kết nối mạng giữa các thiết bị qua Bluetooth.
    private static final UUID PBAP_UUID = UUID.fromString("00001116-0000-1000-8000-00805f9b34fb"); // PBAP cho phép các thiết bị trao đổi danh bạ điện thoại, ví dụ, đồng bộ danh bạ từ điện thoại sang hệ thống ô tô hoặc thiết bị khác.
    private static final UUID HFP_UUID = UUID.fromString("0000110e-0000-1000-8000-00805f9b34fb"); // HFP được sử dụng để cung cấp tính năng rảnh tay, ví dụ, kết nối điện thoại với hệ thống ô tô hoặc tai nghe để thực hiện cuộc gọi mà không cần cầm điện thoại.
    private static final UUID OPP_UUID = UUID.fromString("0000112f-0000-1000-8000-00805f9b34fb"); // OPP cho phép gửi và nhận các tệp tin như danh bạ, hình ảnh và tài liệu giữa các thiết bị Bluetooth.
    private static final UUID AVRCP_UUID = UUID.fromString("00001112-0000-1000-8000-00805f9b34fb"); // AVRCP cho phép điều khiển từ xa các thiết bị audio/video, ví dụ, điều khiển phát nhạc từ điện thoại qua tai nghe hoặc hệ thống âm thanh.
    private static final UUID IP_UUID = UUID.fromString("0000111f-0000-1000-8000-00805f9b34fb"); // IP cho phép trao đổi hình ảnh giữa các thiết bị Bluetooth, thường dùng trong việc truyền hình ảnh từ máy ảnh hoặc điện thoại sang máy tính hoặc thiết bị khác.
    private static final UUID HSP_UUID = UUID.fromString("00001132-0000-1000-8000-00805f9b34fb"); // HSP cho phép các thiết bị Bluetooth hoạt động như một tai nghe không dây cho điện thoại di động. AG là thiết bị điều khiển âm thanh, thường là điện thoại di động hoặc máy tính.
    private static final UUID VMS_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // VMS cho phép kết nối với thiết bị dùng để kiểm tra sản phẩm VMS.

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

            device.fetchUuidsWithSdp();
            ParcelUuid[] uuids = device.getUuids();
            if (uuids != null) {
                for (ParcelUuid uuid : uuids) {
                    Log.d("BluetoothUUID", "UUID: " + uuid.toString());
                }
            }

            new Thread(() -> {
                BluetoothSocket socket = null;
                try {
                    // Tao socket va ket noi
                    socket = bluetoothDevice.createRfcommSocketToServiceRecord(OPP_UUID);
                    //bluetoothAdapter.cancelDiscovery();
                    socket.connect();

                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Kết nối thành công ", Toast.LENGTH_SHORT).show();
                        // Chuyen sang cua so dang nhap tai khoan
                        Intent connectedIntent = new Intent(context, LoginActivity.class);
                        connectedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(connectedIntent);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException closeException) {
                        closeException.printStackTrace();
                    }
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Kết nối thất bại với " + device.getName(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
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
