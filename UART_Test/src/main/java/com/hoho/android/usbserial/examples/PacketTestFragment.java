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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

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
            displayPacket(Arrays.copyOf(workingPacket, thisPacketSize));
            // Reset temp vars
            workingPacket = new byte[workingPacket.length];
            workingPacketIndex = 0;
            thisPacketSize = 0;
        }
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02X", b) + " ");
        return sb.toString();
    }

    public static String byteToHex(byte a){
        return String.format("%02X", a);
    }

    public void displayPacket(byte[] arr){
        String[] sensorsInThisPacket = uart_parse.parse_header(Arrays.copyOf(workingPacket, 2));
        int bits = uart_parse.bits_in_data_section(sensorsInThisPacket);
        int dataSize =  (bits != 0) ? ((bits / 8) + ((bits % 8 == 0) ? 0 : 1)) : 0;

        Map<String, Integer> sensor_data = uart_parse.parse_packet(
                Arrays.copyOfRange(
                        workingPacket,
                        Sensors_CFG.HEADER_SIZE_BYTES,
                        Sensors_CFG.HEADER_SIZE_BYTES+dataSize
                ),
                sensorsInThisPacket
        );

        SpannableStringBuilder header_details_bin = new SpannableStringBuilder();
        SpannableStringBuilder header_details_hex = new SpannableStringBuilder();
        for (int i = 0; i < Sensors_CFG.HEADER_SIZE_BYTES; i++){
            SpannableStringBuilder binTemp = new SpannableStringBuilder(uart_parse.print_byte(arr[i]));
            if (i == 0){
                binTemp = binTemp.insert(0, "|");
            }
            binTemp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.SpruhaFavoriteColor)), 0, binTemp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            header_details_bin = header_details_bin.append(binTemp);

            SpannableStringBuilder hexTemp = new SpannableStringBuilder(byteToHex(arr[i]) + " ");
            hexTemp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.SpruhaFavoriteColor)), 0, hexTemp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            header_details_hex = header_details_hex.append(hexTemp);
        }

        for (int i = Sensors_CFG.HEADER_SIZE_BYTES; i < Sensors_CFG.HEADER_SIZE_BYTES + dataSize; i++){
            SpannableStringBuilder binTemp = new SpannableStringBuilder(uart_parse.print_byte(arr[i]));
            binTemp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.ShynnFavoriteColor)), 0, binTemp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            header_details_bin = header_details_bin.append(binTemp);

            SpannableStringBuilder hexTemp = new SpannableStringBuilder(byteToHex(arr[i]) + " ");
            hexTemp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.ShynnFavoriteColor)), 0, hexTemp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            header_details_hex = header_details_hex.append(hexTemp);
        }

        for (int i = Sensors_CFG.HEADER_SIZE_BYTES + dataSize; i < thisPacketSize; i++){
            SpannableStringBuilder binTemp = new SpannableStringBuilder(uart_parse.print_byte(arr[i]));
            binTemp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.RohansFavoriteColor)), 0, binTemp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            header_details_bin = header_details_bin.append(binTemp);

            SpannableStringBuilder hexTemp = new SpannableStringBuilder(byteToHex(arr[i]) + " ");
            hexTemp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.RohansFavoriteColor)), 0, hexTemp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            header_details_hex = header_details_hex.append(hexTemp);
        }
        header_details_bin = header_details_bin.append("\n");
        header_details_hex = header_details_hex.append("\n");


        SpannableStringBuilder spn = new SpannableStringBuilder("Received Packet:\n");


        SpannableStringBuilder spn2 = new SpannableStringBuilder("Header: " + String.join(", ", sensorsInThisPacket) + "\n");
        spn2.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.SpruhaFavoriteColor)), 0, spn2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableStringBuilder spn3 = new SpannableStringBuilder("Body:\n");
        for (String s : sensor_data.keySet()){
            spn3 = spn3.append(s + " (" + (Sensors_CFG.sensor_details.get(s)) + ")" + " returned datapoint " + sensor_data.get(s) +"\n");
        }
        spn3.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.ShynnFavoriteColor)), 0, spn3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        byte[] crc = Arrays.copyOfRange(arr, Sensors_CFG.HEADER_SIZE_BYTES + dataSize, thisPacketSize);
        SpannableStringBuilder spn4 = new SpannableStringBuilder("CRC: " + byteArrayToHex(crc) + "\n");
        spn4 = spn4.append("CRC is valid: " + uart_parse.verify(arr) + "\n");
        spn4.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.RohansFavoriteColor)), 0, spn4.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableStringBuilder spn5 = new SpannableStringBuilder("========================================\n");
        spn5.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.delim)), 0, spn5.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        receiveText.append(spn);
        receiveText.append(header_details_bin);
        receiveText.append(header_details_hex);
        receiveText.append(spn2);
        receiveText.append(spn3);
        receiveText.append(spn4);
        receiveText.append(spn5);
    }
}