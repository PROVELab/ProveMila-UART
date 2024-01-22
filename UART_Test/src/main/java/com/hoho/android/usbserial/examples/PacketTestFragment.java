package com.hoho.android.usbserial.examples;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hoho.android.usbserial.BuildConfig;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;

public class PacketTestFragment extends TerminalFragment{
    byte[] workingPacket = new byte[Sensors_CFG.HEADER_SIZE_BYTES+
                                        Sensors_CFG.MAX_DATA_SIZE_BYTES+Sensors_CFG.CRC_SIZE_BYTES];
    int workingPacketIndex = 0;
    int thisPacketSize = 0;
    public PacketTestFragment(){
        super();
    }

    @Override
    public void receive(byte[] data) {
        Log.d("PacketTestFragment", "Received chunk of data");
        Log.d("PacketTestFragment", HexDump.dumpHexString(data));
        for (int i = 0; i < data.length; i++){
            workingPacket[workingPacketIndex] = data[i];
            workingPacketIndex++;
        }
        if (thisPacketSize == 0 && workingPacketIndex > 1){
            String[] sensorsInThisPacket = uart_parse.parse_header(Arrays.copyOf(workingPacket, 2));
            int bits = uart_parse.bits_in_data_section(sensorsInThisPacket);
            thisPacketSize =  (bits != 0) ? ((bits / 8) + ((bits % 8 == 0) ? 0 : 1)) : 0;
            thisPacketSize += Sensors_CFG.HEADER_SIZE_BYTES + Sensors_CFG.CRC_SIZE_BYTES;
        }
        if (workingPacketIndex == thisPacketSize){
            // We got a full packet, parse its stuff and hexdump to compare
            Log.d("PacketTestFragment", "Full Packet received: ");
            Log.d("PacketTestFragment", HexDump.dumpHexString(workingPacket));
            // Reset temp vars
            workingPacket = new byte[workingPacket.length];
            workingPacketIndex = 0;
            thisPacketSize = 0;
        }
    }
}