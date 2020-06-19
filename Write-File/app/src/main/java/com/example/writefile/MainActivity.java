package com.example.writefile;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import devin.com.linkmanager.LinkManager;
import devin.com.linkmanager.bean.Angle;
import devin.com.linkmanager.bean.DataType;
import devin.com.linkmanager.bean.Power;

public class MainActivity extends AppCompatActivity {
    public static final String TAG ="SENSOR";
    public static final String FILE_NAME = "link.csv";
    private String data;
    private TextView tv,ctv;
    private List<Signals> signalsList = new ArrayList<>();
    private LineGraphSeries<DataPoint> mSeries = new LineGraphSeries<>();
    private double graph2LastXValue = 5d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.loadedData);
        ctv= findViewById(R.id.connectStatus);

        /*Permissions*/
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        Dexter.withActivity(this)
                .withPermissions(permissions)
                .withListener(permissionsListener)
                .check();
        /*EEG Sensor-----------------------*/
        try {
            LinkManager.getInstance().init(getApplication());
        }catch (Exception e){
            e.printStackTrace();
        }

        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.addSeries(mSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);
       // mSeries.appendData(new DataPoint(graph2LastXValue+=1d, data), true, 40);
    }

    MultiplePermissionsListener permissionsListener = new MultiplePermissionsListener() {
        @Override
        public void onPermissionsChecked(MultiplePermissionsReport report) {
        }

        @Override
        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

        }
    };

    public void saveFile(View view){
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(FILE_NAME,MODE_WORLD_READABLE);
            /*fos.write(data.getBytes());*/
            for(int i=0; i<signalsList.size(); i++){
                Signals s = signalsList.get(i);
                fos.write(s.toString().getBytes());
            }
            Log.d("WRITESTATUS","Success");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("WRITESTATUS","failed");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("WRITESTATUS","failed");
        }finally {
            if(fos!=null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadFile(View view){
        FileInputStream fis = null;
        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            while((text=br.readLine())!=null){
                sb.append(text).append("\n");
            }
            tv.setText(sb);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void appendToFile(String str) {
        File file = getFileStreamPath("data.csv");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream writer = null;
        try {
            writer = openFileOutput(file.getName(), MODE_APPEND | MODE_WORLD_READABLE);
            writer.write(str.getBytes());
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*NUROLINK-----------------*/
    public void startSensor(View view){
        startLink();
    }

    public void stopSensor(View view){
        stopLink();
    }
    private void startLink(){
        LinkManager.getInstance().setConnectiDeviceFirst(handler);
    }

    private void stopLink(){
        LinkManager.getInstance().close();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case DataType.CODE_UP_SEND_START:
                    break;
                case DataType.CODE_UP_SEND_ING:
                    break;
                case DataType.CODE_UP_SEND_END:
                    //升级包发送完毕
                    break;
                case DataType.CODE_UP_SUCCEED:
                    Log.d("TESTING","0");
                    break;
                case DataType.CODE_CONNECT_SUCCEED:
                    ctv.setText("connected");
                    break;
                case DataType.CODE_CONNECT_FAIL:
                    ctv.setText("dis_connected");
                    break;
                case DataType.CODE_POOR_SIGNAL:
                    //eeg_signalstrength = 200-msg.arg1+55;
                    break;
                case DataType.CODE_ATTENTION:
                    long t1 = System.currentTimeMillis();
                    signalsList.add(new Signals(t1, Signals.Signal.ATTENTION,msg.arg1));
                    Log.d(TAG,"Attention:"+  msg.arg1);
                    break;
                case DataType.CODE_MEDITATION:
                    long t2 = System.currentTimeMillis();
                    signalsList.add(new Signals(t2, Signals.Signal.MEDITATION,msg.arg1));
                    Log.d(TAG,"Meditation:"+  msg.arg1);
                    break;
                case DataType.CODE_RAW:
                    long t3 = System.currentTimeMillis();
                    signalsList.add(new Signals(t3, Signals.Signal.RAW,msg.arg1));
                    Log.d(TAG,"Raw:"+  msg.arg1);
                    break;
                case DataType.CODE_EEGPOWER:
                    Power power = (Power)msg.obj;
                    long t4 = System.currentTimeMillis();
                    signalsList.add(new Signals(t4, Signals.Signal.LOALPHA,power.lowAlpha));
                    signalsList.add(new Signals(t4, Signals.Signal.HIALPHA,power.highAlpha));
                    signalsList.add(new Signals(t4, Signals.Signal.LOBETA,power.lowBeta));
                    signalsList.add(new Signals(t4, Signals.Signal.HIBETA,power.highBeta));
                    signalsList.add(new Signals(t4, Signals.Signal.LOGAMMA,power.lowGamma));
                    signalsList.add(new Signals(t4, Signals.Signal.MIDGAMMA,power.middleGamma));
                    signalsList.add(new Signals(t4, Signals.Signal.THETA,power.theta));
                    signalsList.add(new Signals(t4, Signals.Signal.DELTA,power.delta));
                    break;
                case DataType.CODE_ANGLE:
                    Angle angle = (Angle) msg.obj;
                    break;
            }
            invalidateOptionsMenu();
        }
    };
}