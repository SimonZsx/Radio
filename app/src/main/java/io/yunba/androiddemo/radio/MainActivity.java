package io.yunba.androiddemo.radio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


import io.yunba.android.manager.YunBaManager;

public class MainActivity extends ActionBarActivity {



    private final static String TAG = "YunBaApplication";
    private static String requestURL = "your HTTP POST request url";
    private static String downloadURL = "your HTTP GET request url";

    private TextView info;
    private File transfer;
    private String downloadname;
    private String audiopath;

    public final static String MESSAGE_RECEIVED_ACTION = "io.yunba.anroiddemo.radio.msg_received_action";


    final private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
           switch(msg.what){

               case 1: // received handler

                   String audio = msg.getData().getString("audio");
                   String topic = msg.getData().getString("topic");

                   info.setText("received audio from " + topic);

                   downloadname=audio;

                   new Thread(download).start();

                  // playAudio(audio);
                   info.setText(info.getText()+"\n"+audio);
                   break;

               case 2:  // status and info handler
                   String status = msg.getData().getString("status");

                   info.setText(info.getText()+"\n"+status);

               case 3: // download finished handler

                   String dlpath= msg.getData().getString("path");
                   String status1= msg.getData().getString("status");

                   audiopath=dlpath;

                   info.setText(info.getText() + "\n" + status1 + ":" + dlpath);

                   //new Thread(play).start();

                   playAudio(dlpath);


               default:
                   break;
           }

        }
    };

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // start YunBa service and subscribe it to certain topics
        YunBaManager.start(getApplicationContext());

        YunBaManager.subscribe(getApplicationContext(), new String[]{"go"}, new IMqttActionListener() {

            @Override
            public void onSuccess(IMqttToken arg0) {
                Log.d(TAG, "Subscribe topic succeed");
            }

            @Override
            public void onFailure(IMqttToken arg0, Throwable arg1) {
                Message handlermsg=new Message();
                handlermsg.what=2;
                Bundle status=new Bundle();
                status.putString("status", "Subscribe failed");
                handlermsg.setData(status);
                mHandler.sendMessage(handlermsg);
            }
        });

        // register the message receiver
        registerMessageReceiver();

        // implement the Radio button, save the audio AMR file first
        RecordButton radio;
        radio=(RecordButton)findViewById(R.id.radioButton);

        info = (TextView)findViewById(R.id.textView);


        String path = Environment.getExternalStorageDirectory()
                .getAbsolutePath();

        String filename=RandomFilenameUtil.getRandomFileName();

        path =path+"/"+filename+".amr";

        radio.setSavePath(path);
        radio.setOnFinishedRecordListener(new RecordButton.OnFinishedRecordListener() {

            @Override
            public void onFinishedRecord(String audioPath) {

                Log.i("RECORD!!!", "finished!!!!!!!!!! save to "
                        + audioPath);

                transfer=new File(audioPath);

                new Thread(upload).start();


                // send the string as message to the certain topic of Yunba
                YunBaManager.publish(getApplicationContext(), "go", transfer.getName(), new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {

                        Message handlermsg=new Message();
                        handlermsg.what=2;
                        Bundle status=new Bundle();
                        status.putString("status", "Published sucessfully!");
                        handlermsg.setData(status);
                        mHandler.sendMessage(handlermsg);

                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {

                        Message handlermsg=new Message();
                        handlermsg.what=2;
                        Bundle status=new Bundle();
                        status.putString("status","Published failed!");
                        handlermsg.setData(status);
                        mHandler.sendMessage(handlermsg);

                    }
                });

            }
        });

    }

    Runnable download= new Runnable() {
        @Override
        public void run() {

            String path = DownloadUtil.downloadFile(downloadURL+downloadname);

            Message msg = new Message();
            Bundle data = new Bundle();
            msg.what=3;
            data.putString("path",path);
            data.putString("status","Download finished, start playing");
            msg.setData(data);
            mHandler.sendMessage(msg);

        }
    };

    Runnable upload = new Runnable(){
        @Override
        public void run() {

            String request = UploadUtil.uploadFile(transfer, requestURL);

            Message msg = new Message();
            Bundle data = new Bundle();
            msg.what=2;
            data.putString("status",request);
            msg.setData(data);
            mHandler.sendMessage(msg);
        }
    };

    Runnable play = new Runnable() {
        @Override
        public void run() {

            MediaPlayer mp = new MediaPlayer();
            try {
                mp.reset();
                mp.setDataSource(audiopath);
                mp.prepare();
                mp.start();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();

                    Message handlermsg=new Message();
                    handlermsg.what=2;
                    Bundle status=new Bundle();
                    String path = Environment.getDataDirectory().getAbsolutePath();
                    status.putString("status", "Audio Played sucessfully!");
                    handlermsg.setData(status);
                    mHandler.sendMessage(handlermsg);
                }
            });

        }
    };
    private void playAudio(String audio){


        MediaPlayer mp = new MediaPlayer();
        try {
            mp.reset();
            mp.setDataSource(audio);
            mp.prepare();
            mp.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
           // Toast.makeText(getBaseContext(),"Music played error",Toast.LENGTH_SHORT ).show();
        }

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();

                Message handlermsg=new Message();
                handlermsg.what=2;
                Bundle status=new Bundle();
                String path = Environment.getDataDirectory().getAbsolutePath();
                status.putString("status", "Audio Played sucessfully!");
                handlermsg.setData(status);
                mHandler.sendMessage(handlermsg);
            }
        });
    }


    private MessageReceiver mMessageReceiver;
    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(YunBaManager.MESSAGE_RECEIVED_ACTION);
        filter.addCategory(getPackageName());
        getApplicationContext().registerReceiver(mMessageReceiver, filter);

        IntentFilter filter_Connect = new IntentFilter();
        filter_Connect.addAction(YunBaManager.MESSAGE_CONNECTED_ACTION);
        filter_Connect.addCategory(getPackageName());
        getApplicationContext().registerReceiver(mMessageReceiver, filter_Connect);

        IntentFilter filter_Disconnect = new IntentFilter();
        filter_Disconnect.addAction(YunBaManager.MESSAGE_DISCONNECTED_ACTION);
        filter_Disconnect.addCategory(getPackageName());
        getApplicationContext().registerReceiver(mMessageReceiver, filter_Disconnect);

        IntentFilter pres = new IntentFilter();
        pres.addAction(YunBaManager.PRESENCE_RECEIVED_ACTION);
        pres.addCategory(getPackageName());
        getApplicationContext().registerReceiver(mMessageReceiver, pres);

    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Action - " + intent.getAction());

            if (YunBaManager.MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                String status = "YunBa - Connected";

                String topic = intent.getStringExtra(YunBaManager.MQTT_TOPIC);
                String msg = intent.getStringExtra(YunBaManager.MQTT_MSG);
                StringBuilder showMsg = new StringBuilder();
                showMsg.append("[Message] ").append(YunBaManager.MQTT_TOPIC)
                        .append(" = ").append(topic).append(" ,")
                        .append(YunBaManager.MQTT_MSG).append(" = ").append(msg);


                Message handlermsg=new Message();
                handlermsg.what=1;
                Bundle audio=new Bundle();
                audio.putString("audio",msg);
                audio.putString("topic",topic);
                handlermsg.setData(audio);
                mHandler.sendMessage(handlermsg);

            } else if(YunBaManager.MESSAGE_CONNECTED_ACTION.equals(intent.getAction())) {

                String status = "YunBa - Connected";
                Toast.makeText(getBaseContext(), status, Toast.LENGTH_LONG).show();

            } else if(YunBaManager.MESSAGE_DISCONNECTED_ACTION.equals(intent.getAction())) {
                String status = "YunBa - DisConnected";
                Toast.makeText(getBaseContext(), status, Toast.LENGTH_LONG).show();
            }


            else if (YunBaManager.PRESENCE_RECEIVED_ACTION.equals(intent.getAction())) {

                String status = "YunBa - Connected";
                String topic = intent.getStringExtra(YunBaManager.MQTT_TOPIC);
                String msg = intent.getStringExtra(YunBaManager.MQTT_MSG);
                StringBuilder showMsg = new StringBuilder();
                showMsg.append("[Message from prensence] ").append(YunBaManager.MQTT_TOPIC)
                        .append(" = ").append(topic).append(" ,")
                        .append(YunBaManager.MQTT_MSG).append(" = ").append(msg);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
