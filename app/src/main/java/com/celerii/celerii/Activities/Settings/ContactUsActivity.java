package com.celerii.celerii.Activities.Settings;

import android.app.Dialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.celerii.celerii.R;
import com.celerii.celerii.helperClasses.Analytics;
import com.celerii.celerii.helperClasses.CheckNetworkConnectivity;
import com.celerii.celerii.helperClasses.CustomToast;
import com.celerii.celerii.helperClasses.Date;
import com.celerii.celerii.helperClasses.SharedPreferencesManager;
import com.celerii.celerii.models.ContactUsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ContactUsActivity extends AppCompatActivity {
    SharedPreferencesManager sharedPreferencesManager;

    FirebaseAuth auth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    FirebaseUser mFirebaseUser;

    Toolbar toolbar;
    EditText subject, message;
    Button sendMessage;
    String subjectString = "", messageString = "";

    String featureUseKey = "";
    String featureName = "Contact Us";
    long sessionStartTime = 0;
    String sessionDurationInSeconds = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        sharedPreferencesManager = new SharedPreferencesManager(this);

        auth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mFirebaseUser = auth.getCurrentUser();

        toolbar = (Toolbar) findViewById(R.id.hometoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Contact Us");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        subject = (EditText) findViewById(R.id.messagesubject);
        message = (EditText) findViewById(R.id.message);
        sendMessage = (Button) findViewById(R.id.sendmessage);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CheckNetworkConnectivity.isNetworkAvailable(getBaseContext())) {
                    String messageString = "Your device is not connected to the internet. Check your connection and try again.";
                    showDialogWithMessage(messageString);
                    return;
                }

                subjectString = subject.getText().toString().trim();
                messageString = message.getText().toString().trim();

                if (!validate(subjectString, "subject", subject))
                    return;

                if (!validate(messageString, "body", message))
                    return;

                String date = Date.getDate();
                String year = Date.getYear();
                String month = Date.getMonth();
                String day = Date.getDay();
                boolean seen = false;
                boolean responded = false;
                DatabaseReference contactKey = mFirebaseDatabase.getReference("Customer Feedback").child("Contact Us").child(mFirebaseUser.getUid()).push();
                String pushKey = contactKey.getKey();
                DatabaseReference contactRef = mFirebaseDatabase.getReference();
                ContactUsModel contactUsModel = new ContactUsModel(subject.getText().toString().trim(), message.getText().toString().trim(), mFirebaseUser.getUid(), sharedPreferencesManager.getActiveAccount(), mFirebaseUser.getEmail(), date, year, month, day, seen, responded);
                Map<String, Object> newContactUsMap = new HashMap<String, Object>();
                newContactUsMap.put("Customer Feedback/Contact Us/" + pushKey + "/", contactUsModel);
                contactRef.updateChildren(newContactUsMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        CustomToast.blueBackgroundToast(ContactUsActivity.this, "Thank you for reaching out");
                        finish();
                    }
                });
            }
        });
    }

    private boolean validate(String string, String type, EditText editText) {
        if (string.isEmpty()) {
            String messageString = "The " + type + " of your mail is empty";
            showDialogWithMessage(messageString);
            editText.requestFocus();
            return false;
        }

        return true;
    }

    void showDialogWithMessage (String messageString) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_unary_message_dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        TextView message = (TextView) dialog.findViewById(R.id.dialogmessage);
        Button OK = (Button) dialog.findViewById(R.id.optionone);
        try {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        } catch (Exception e) {
            return;
        }

        message.setText(messageString);

        OK.setText("OK");

        OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (sharedPreferencesManager.getActiveAccount().equals("Parent")) {
            featureUseKey = Analytics.featureAnalytics("Parent", mFirebaseUser.getUid(), featureName);
        } else {
            featureUseKey = Analytics.featureAnalytics("Teacher", mFirebaseUser.getUid(), featureName);
        }
        sessionStartTime = System.currentTimeMillis();
    }

    @Override
    protected void onStop() {
        super.onStop();

        sessionDurationInSeconds = String.valueOf((System.currentTimeMillis() - sessionStartTime) / 1000);
        String day = Date.getDay();
        String month = Date.getMonth();
        String year = Date.getYear();
        String day_month_year = day + "_" + month + "_" + year;
        String month_year = month + "_" + year;

        HashMap<String, Object> featureUseUpdateMap = new HashMap<>();
        String mFirebaseUserID = mFirebaseUser.getUid();

        featureUseUpdateMap.put("Analytics/Feature Use Analytics User/" + mFirebaseUserID + "/" + featureName + "/" + featureUseKey + "/sessionDurationInSeconds", sessionDurationInSeconds);
        featureUseUpdateMap.put("Analytics/Feature Daily Use Analytics User/" + mFirebaseUserID + "/" + featureName + "/" + day_month_year + "/" + featureUseKey + "/sessionDurationInSeconds", sessionDurationInSeconds);
        featureUseUpdateMap.put("Analytics/Feature Monthly Use Analytics User/" + mFirebaseUserID + "/" + featureName + "/" + month_year + "/" + featureUseKey + "/sessionDurationInSeconds", sessionDurationInSeconds);
        featureUseUpdateMap.put("Analytics/Feature Yearly Use Analytics User/" + mFirebaseUserID + "/" + featureName + "/" + year + "/" + featureUseKey + "/sessionDurationInSeconds", sessionDurationInSeconds);

        featureUseUpdateMap.put("Analytics/Feature Use Analytics/" + featureName + "/" + featureUseKey + "/sessionDurationInSeconds", sessionDurationInSeconds);
        featureUseUpdateMap.put("Analytics/Feature Daily Use Analytics/" + featureName + "/" + day_month_year + "/" + featureUseKey + "/sessionDurationInSeconds", sessionDurationInSeconds);
        featureUseUpdateMap.put("Analytics/Feature Monthly Use Analytics/" + featureName + "/" + month_year + "/" + featureUseKey + "/sessionDurationInSeconds", sessionDurationInSeconds);
        featureUseUpdateMap.put("Analytics/Feature Yearly Use Analytics/" + featureName + "/" + year + "/" + featureUseKey + "/sessionDurationInSeconds", sessionDurationInSeconds);

        DatabaseReference featureUseUpdateRef = FirebaseDatabase.getInstance().getReference();
        featureUseUpdateRef.updateChildren(featureUseUpdateMap);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
