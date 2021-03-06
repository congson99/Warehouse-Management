package com.example.warehousemanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class EnvDemoActivity extends AppCompatActivity {

    private MQTTHelper mqttHelper;

    DatabaseReference databaseReference;

    TextView show_temperature;
    TextView show_humidity;
    TextView show_speaker;
    TextView best_cel;

    Button bt_save;
    ImageView pre;
    ImageView next;
    ImageView bt_demo1;
    ImageView bt_demo2;
    ImageView refresh;
    Switch fan;

    public boolean fanonoff = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_env_demo);

        //Intent from Home
        Intent intentf = getIntent();
        final String id = intentf.getStringExtra("id");

        //Anh xa
        show_temperature = (TextView) findViewById(R.id.environment_show_temperature2);
        show_humidity = (TextView) findViewById(R.id.environment_show_humidity2);
        show_speaker = (TextView) findViewById(R.id.environment_show_speker2);
        best_cel = (TextView) findViewById(R.id.environment_bestcel2);
        bt_save = (Button) findViewById(R.id.environment_btn_save2);
        pre = (ImageView) findViewById(R.id.environment_bt_pre2);
        next = (ImageView) findViewById(R.id.environment_bt_next2);
        bt_demo1 = (ImageView) findViewById(R.id.demo1ne2);
        bt_demo2 = (ImageView) findViewById(R.id.quat2);
        refresh = (ImageView) findViewById(R.id.demo2ne2);
        fan = (Switch) findViewById(R.id.fanonoff2);

        //Clear
        show_humidity.setText("");
        show_temperature.setText("");
        show_speaker.setText("");
        best_cel.setText("");
        fan.setChecked(true);

        //Set best cel
        databaseReference = FirebaseDatabase.getInstance().getReference("Main").child("BestTemperature");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().toString().length() == 1 && !dataSnapshot.getValue().toString().equals("0")){
                    best_cel.setText("0"+dataSnapshot.getValue().toString());
                } else {
                    best_cel.setText(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    fanonoff = true;
                    startMQTTTempHumi("TempHumi", "Topic/TempHumi", show_temperature, show_humidity, best_cel);
                    startMQTTSpeaker("Speaker", "Topic/Speaker", show_speaker);
                    Toast.makeText(EnvDemoActivity.this, "Turn on the fan", Toast.LENGTH_LONG).show();
                }else {
                    fanonoff = false;
                    startMQTTTempHumi("TempHumi", "Topic/TempHumi", show_temperature, show_humidity, best_cel);
                    startMQTTSpeaker("Speaker", "Topic/Speaker", show_speaker);
                    Toast.makeText(EnvDemoActivity.this, "Turn off the fan", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Get value and set
        startMQTTTempHumi("TempHumi", "Topic/TempHumi", show_temperature, show_humidity, best_cel);
        startMQTTSpeaker("Speaker", "Topic/Speaker", show_speaker);

        //Chang best cel
        pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference = FirebaseDatabase.getInstance().getReference("Main").child("BestTemperature");
                String temp = best_cel.getText().toString();
                int integer = Integer.parseInt(temp) - 1;
                if (integer < 0) integer = 0;
                if (String.valueOf(integer).length() == 1){
                    best_cel.setText("0"+String.valueOf(integer));
                } else {
                    best_cel.setText(String.valueOf(integer));
                }
                databaseReference.setValue(String.valueOf(integer));
                startMQTTTempHumi("TempHumi", "Topic/TempHumi", show_temperature, show_humidity, best_cel);
                startMQTTSpeaker("Speaker", "Topic/Speaker", show_speaker);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference = FirebaseDatabase.getInstance().getReference("Main").child("BestTemperature");
                String temp = best_cel.getText().toString();
                int integer = Integer.parseInt(temp) + 1;
                if (integer > 29) integer = 29;
                if (String.valueOf(integer).length() == 1){
                    best_cel.setText("0"+String.valueOf(integer));
                } else {
                    best_cel.setText(String.valueOf(integer));
                }
                databaseReference.setValue(String.valueOf(integer));
                startMQTTTempHumi("TempHumi", "Topic/TempHumi", show_temperature, show_humidity, best_cel);
                startMQTTSpeaker("Speaker", "Topic/Speaker", show_speaker);
            }
        });

        //Save to history
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference = FirebaseDatabase.getInstance().getReference("Main").child("History");
                Date date = new Date();
                if (!show_temperature.getText().toString().equals("") && !show_humidity.getText().toString().equals("") && !show_speaker.getText().toString().equals("")){
                    databaseReference.child(String.valueOf(date.getTime())+id+" saved values:").setValue("Temperature: "+ show_humidity.getText() + " *C - Humidity: "+show_humidity.getText()+" %\nFan: "+show_speaker.getText()+" % - Standard Temperature: "+best_cel.getText().toString()+" *C");
                    String temp = (String) android.text.format.DateFormat.format("dd-MM-yyyy hh:mm:ss", Long.parseLong(String.valueOf(date.getTime())));
                    Toast.makeText(EnvDemoActivity.this, "You have saved to history\nat " + temp, Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(EnvDemoActivity.this, "The value is not valid!", Toast.LENGTH_LONG).show();
                }
            }
        });

        //demo
        bt_demo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EnvDemoActivity.this, DemoActivity.class);
                startActivity(intent);
                Toast.makeText(EnvDemoActivity.this, "Test mode", Toast.LENGTH_LONG).show();
            }
        });
        bt_demo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EnvDemoActivity.this, FanActivity.class);
                startActivity(intent);
                Toast.makeText(EnvDemoActivity.this, "Test mode", Toast.LENGTH_LONG).show();
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMQTTTempHumi("TempHumi", "Topic/TempHumi", show_temperature, show_humidity, best_cel);
                startMQTTSpeaker("Speaker", "Topic/Speaker", show_speaker);
                Toast.makeText(EnvDemoActivity.this, "Refreshed successfully", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startMQTTTempHumi(String ID, String topic, final  TextView a,final TextView b, final  TextView cel){
        mqttHelper = new MQTTHelper(getApplicationContext(), ID, topic);
        mqttHelper.setCallBack(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println(message.toString());
                JSONArray jsonArray = new JSONArray(message.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String device_id = jsonObject.getString("device_id");
                    String location = jsonObject.getString("values");

                    JSONArray arr_value = new JSONArray(location);
                    a.setText(arr_value.getString(0));
                    b.setText(arr_value.getString(1));
                    float longitude = Float.parseFloat(a.getText().toString());
                    if (!a.getText().toString().equals("") && !cel.getText().toString().equals("")){
                        if(longitude > Integer.parseInt(cel.getText().toString()) + 50){
                            sendDataToMQTT("Speaker", "1", "5000");
                        }
                        else {
                            if(longitude <= Integer.parseInt(cel.getText().toString())){
                                sendDataToMQTT("Speaker", "0", "1");
                            }
                            else {
                                String temp = String.valueOf((Integer.parseInt(a.getText().toString())-Integer.parseInt(cel.getText().toString()))*2*50);
                                sendDataToMQTT("Speaker", "1", temp);
                            }
                        }
                    }
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void startMQTTSpeaker(String ID, String topic, final  TextView a){
        mqttHelper = new MQTTHelper(getApplicationContext(), ID, topic);
        mqttHelper.setCallBack(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println(message.toString());
                JSONArray jsonArray = new JSONArray(message.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String device_id = jsonObject.getString("device_id");
                    String location = jsonObject.getString("values");

                    JSONArray arr_value = new JSONArray(location);
                    if (arr_value.getString(0).equals("0") || arr_value.getString(1).equals("0")){
                        a.setText("0");
                    }
                    else {
                        a.setText(String.valueOf(Integer.parseInt(arr_value.getString(1))/50));
                    }
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void sendDataToMQTT(String ID, String value1, String value2) throws JSONException {

        if (ID.equals("Speaker") && !fanonoff){
            value1 = "0";
            value2 = "1";
        }

        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(true);

        String x = "\"".substring(0,1);
        JSONObject payload = new JSONObject();
        payload.put("device_id", ID);
        payload.put("values", "");

        String list = "["+x+value1+x+","+x+value2+x+"]";
        String a = "[" + payload.toString().substring(0,payload.toString().length()-3) + list + "}]";
        byte[] b = a.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish("Topic/" + ID, msg);
        }catch (MqttException ignored){
        }
    }
}