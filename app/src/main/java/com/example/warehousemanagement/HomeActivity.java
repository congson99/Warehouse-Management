package com.example.warehousemanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    DatabaseReference databaseReference;

    TextView name;
    ImageView avatar;
    CardView bt_environment;
    CardView bt_weather;
    CardView bt_history;
    CardView bt_products;
    CardView bt_aboutus;
    CardView bt_account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Intent from Login
        Intent intentf = getIntent();
        final String id = intentf.getStringExtra("id");

        //Anh xa
        initView();

        //link to account info
        databaseReference = FirebaseDatabase.getInstance().getReference("Main").child("Account");

        //Set value
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Set value
                avatar.setImageResource(R.drawable.farmericon);
                if(!dataSnapshot.child(id).child("Avatar").getValue().toString().equals("None")){
                    byte[] mangGet = Base64.decode(dataSnapshot.child(id).child("Avatar").getValue().toString(), Base64.DEFAULT);
                    Bitmap bm = BitmapFactory.decodeByteArray(mangGet, 0, mangGet.length);
                    avatar.setImageBitmap(bm);
                }
                name.setText(dataSnapshot.child(id).child("Name").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Environment_button
        bt_environment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent to Environment
                Intent intent = new Intent(HomeActivity.this, EnvironmentActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        //History_button
        bt_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent to Environment
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        //Product_button
        bt_products.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent to Product
                Intent intent = new Intent(HomeActivity.this, ProductActivity.class);
                startActivity(intent);
            }
        });

        //Weather_button
        bt_weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent to Weather
                Intent intent = new Intent(HomeActivity.this, WeatherActivity.class);
                startActivity(intent);
            }
        });

        //Account_button
        bt_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent to Account
                Intent intent = new Intent(HomeActivity.this, InfoActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        //About_us_button
        bt_aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent to About Us
                Intent intent = new Intent(HomeActivity.this, AboutUsActivity.class);
                startActivity(intent);
            }
        });

        //Test mode
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, EnvDemoActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
                Toast.makeText(HomeActivity.this, "Test mode", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initView(){
        name = (TextView) findViewById(R.id.home_name);
        avatar = (ImageView) findViewById(R.id.home_circle_img_avt);
        bt_environment = (CardView) findViewById(R.id.home_acti_environment_card);
        bt_weather = (CardView) findViewById(R.id.home_acti_weather_card);
        bt_account = (CardView) findViewById(R.id.home_acti_account_card);
        bt_aboutus = (CardView) findViewById(R.id.home_acti_aboutus_card);
        bt_products = (CardView) findViewById(R.id.home_acti_products_card);
        bt_history = (CardView) findViewById(R.id.home_acti_history_card);
    }

}