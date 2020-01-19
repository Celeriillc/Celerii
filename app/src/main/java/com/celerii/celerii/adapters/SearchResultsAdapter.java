package com.celerii.celerii.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.celerii.celerii.R;
import com.celerii.celerii.Activities.Profiles.SchoolProfile.SchoolProfileActivity;
import com.celerii.celerii.Activities.Profiles.StudentProfileActivity;
import com.celerii.celerii.Activities.Profiles.TeacherProfileOneActivity;
import com.celerii.celerii.helperClasses.CheckNetworkConnectivity;
import com.celerii.celerii.helperClasses.CustomProgressDialogOne;
import com.celerii.celerii.helperClasses.CustomToast;
import com.celerii.celerii.helperClasses.Date;
import com.celerii.celerii.helperClasses.SharedPreferencesManager;
import com.celerii.celerii.models.DisconnectionModel;
import com.celerii.celerii.models.NotificationModel;
import com.celerii.celerii.models.Parent;
import com.celerii.celerii.models.ParentSchoolConnectionRequest;
import com.celerii.celerii.models.SearchHistoryRow;
import com.celerii.celerii.models.SearchResultsRow;
import com.celerii.celerii.models.TeacherSchoolConnectionRequest;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by DELL on 9/2/2017.
 */

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.MyViewHolder> {

    SharedPreferencesManager sharedPreferencesManager;
    private List<SearchResultsRow> searchResultsRowList;
    private Context context;
    private ArrayList<String> existingConnections;
    private ArrayList<String> pendingIncomingRequests;
    private ArrayList<String> pendingOutgoingRequests;
    public HashMap<String, ArrayList<String>> guardians;
    FirebaseAuth auth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    FirebaseUser mFirebaseUser;
    int classesToBeRemovedCounter = 0;
    CustomProgressDialogOne customProgressDialogOne;
    int parentsToBeRemovedCounter = 0;
    boolean connected = false;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView entityName, entityLocationClass;
        public ImageView entityPic;
        Button sendRequest;
        public View clickableView;

        public MyViewHolder(final View view) {
            super(view);
            entityName = (TextView) view.findViewById(R.id.entityname);
            entityLocationClass = (TextView) view.findViewById(R.id.entitylocation_class);
            entityPic = (ImageView) view.findViewById(R.id.entitypic);
            sendRequest = (Button) view.findViewById(R.id.sendrequest);
            clickableView = view;
        }
    }

    public SearchResultsAdapter(List<SearchResultsRow> searchResultsRowList, Context context, ArrayList<String> existingConnections,
                                ArrayList<String> pendingIncomingRequests, ArrayList<String> pendingOutgoingRequests) {
        sharedPreferencesManager = new SharedPreferencesManager(context);
        this.searchResultsRowList = searchResultsRowList;
        this.context = context;
        this.existingConnections = existingConnections;
        this.pendingIncomingRequests = pendingIncomingRequests;
        this.pendingOutgoingRequests = pendingOutgoingRequests;
        guardians = new HashMap<>();
        auth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mFirebaseUser = auth.getCurrentUser();
        customProgressDialogOne = new CustomProgressDialogOne(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_results_row, parent, false);
        return new MyViewHolder(itemView);
    }

    int counter = 0;
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final SearchResultsRow searchResultsRow = searchResultsRowList.get(position);

        holder.entityName.setText(searchResultsRow.getEntityName());
        holder.entityLocationClass.setText(searchResultsRow.getEntityAddress());
        final String entityId = searchResultsRow.getEntityId();
        final String entityName = searchResultsRow.getEntityName();
        final String entityAddressID = searchResultsRow.getEntityAddressID();

        Glide.with(context)
                .load(searchResultsRow.getEntityPic())
                .crossFade()
                .placeholder(R.drawable.profileimageplaceholder)
                .error(R.drawable.profileimageplaceholder)
                .centerCrop().bitmapTransform(new CropCircleTransformation(context))
                .into(holder.entityPic);

        if (sharedPreferencesManager.getActiveAccount().equals("Teacher")) {
            if (searchResultsRow.getEntityType().equals("School")) {
                holder.sendRequest.setVisibility(View.VISIBLE);

                if (existingConnections.contains(entityId)) {
                    holder.sendRequest.setText("Disconnect");
                    holder.sendRequest.setBackgroundResource(R.drawable.roundedbuttonaccent);
                    holder.sendRequest.setTextColor(Color.WHITE);
                } else if (pendingIncomingRequests.contains(entityId)) {
                    holder.sendRequest.setText("Respond");
                    holder.sendRequest.setBackgroundResource(R.drawable.roundedbutton);
                    holder.sendRequest.setTextColor(Color.WHITE);
                } else if (pendingOutgoingRequests.contains(entityId)) {
                    holder.sendRequest.setText("Revoke");
                    holder.sendRequest.setBackgroundResource(R.drawable.roundedbuttonwhite);
                    holder.sendRequest.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryPurple));
                } else {
                    holder.sendRequest.setText("Connect");
                    holder.sendRequest.setBackgroundResource(R.drawable.roundedbutton);
                    holder.sendRequest.setTextColor(Color.WHITE);
                }

            } else {
                holder.sendRequest.setVisibility(View.GONE);
            }
        }
        else if (sharedPreferencesManager.getActiveAccount().equals("Parent")) {
            if (searchResultsRow.getEntityType().equals("Student")) {
                holder.sendRequest.setVisibility(View.VISIBLE);

                if (existingConnections.contains(entityId)) {
                    holder.sendRequest.setText("Disconnect");
                    holder.sendRequest.setBackgroundResource(R.drawable.roundedbuttonaccent);
                    holder.sendRequest.setTextColor(Color.WHITE);
                } else if (pendingIncomingRequests.contains(entityId)) {
                    holder.sendRequest.setText("Respond");
                    holder.sendRequest.setBackgroundResource(R.drawable.roundedbutton);
                    holder.sendRequest.setTextColor(Color.WHITE);
                } else if (pendingOutgoingRequests.contains(entityId)) {
                    holder.sendRequest.setText("Revoke");
                    holder.sendRequest.setBackgroundResource(R.drawable.roundedbuttonwhite);
                    holder.sendRequest.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryPurple));
                } else {
                    holder.sendRequest.setText("Connect");
                    holder.sendRequest.setBackgroundResource(R.drawable.roundedbutton);
                    holder.sendRequest.setTextColor(Color.WHITE);
                }
            } else {
                holder.sendRequest.setVisibility(View.GONE);
            }
        }

        holder.sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!CheckNetworkConnectivity.isNetworkAvailable(context)) {
                    CustomToast.blueBackgroundToast(context, "Your device is not connected to the internet. Check your connection and try again.");
                    return;
                }

                if (sharedPreferencesManager.getActiveAccount().equals("Teacher")) {

                    if (holder.sendRequest.getText().equals("Connect")) {

                        customProgressDialogOne.show();
                        holder.sendRequest.setText("Revoke");
                        holder.sendRequest.setBackgroundResource(R.drawable.roundedbuttonwhite);
                        holder.sendRequest.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryPurple));

                        mDatabaseReference = mFirebaseDatabase.getReference("Teacher To School Request Teacher").child(mFirebaseUser.getUid()).child(entityId).push();
                        String refKey = mDatabaseReference.getKey();

                        String timeSent = Date.getDate();
                        String sorttableTimeSent = Date.convertToSortableDate(timeSent);
                        TeacherSchoolConnectionRequest teacherSchoolConnectionRequest = new TeacherSchoolConnectionRequest("Pending", timeSent, sorttableTimeSent, mFirebaseUser.getUid(), entityId);
                        NotificationModel notificationModel = new NotificationModel(mFirebaseUser.getUid(), entityId, "School", "Teacher", timeSent, sorttableTimeSent, refKey, "Connection Request", "", "", false);

                        Map<String, Object> newRequestMap = new HashMap<String, Object>();
                        mDatabaseReference = mFirebaseDatabase.getReference();
                        newRequestMap.put("Teacher To School Request Teacher/" + mFirebaseUser.getUid() + "/" + entityId + "/" + refKey, teacherSchoolConnectionRequest);
                        newRequestMap.put("Teacher To School Request School/" + entityId + "/" + mFirebaseUser.getUid() + "/" + refKey, teacherSchoolConnectionRequest);
                        newRequestMap.put("NotificationSchool/" + entityId + "/" + refKey, notificationModel);

                        mDatabaseReference.updateChildren(newRequestMap);
                        pendingOutgoingRequests.add(entityId);
                        notifyDataSetChanged();
                        customProgressDialogOne.dismiss();

                    }
                    else if (holder.sendRequest.getText().equals("Revoke")) {

                        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                        int width = metrics.widthPixels;
                        int height = metrics.heightPixels;
                        final Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.custom_dialog_request_connection);
                        TextView message = (TextView) dialog.findViewById(R.id.dialogmessage);
                        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
                        TextView action = (TextView) dialog.findViewById(R.id.action);
                        dialog.show();
//                        dialog.getWindow().setLayout((19 * width) / 20, RecyclerView.LayoutParams.WRAP_CONTENT);

                        String messageString = "Do you want to revoke your request to connect to " + "<b>" + entityName + "</b>" + "?";
                        message.setText(Html.fromHtml(messageString));

                        action.setText("Revoke");

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        action.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                dialog.dismiss();
                                customProgressDialogOne.show();
                                holder.sendRequest.setEnabled(false);

                                mDatabaseReference = mFirebaseDatabase.getReference("Teacher To School Request Teacher").child(mFirebaseUser.getUid()).child(entityId);
                                mDatabaseReference.orderByChild("status").equalTo("Pending").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {

                                            Map<String, Object> newRequestMap = new HashMap<String, Object>();
                                            DatabaseReference newRef = mFirebaseDatabase.getReference();
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                String pendingRequestKey = postSnapshot.getKey();
                                                newRequestMap.put("Teacher To School Request Teacher/" + mFirebaseUser.getUid() + "/" + entityId + "/" + pendingRequestKey + "/" + "status", "Revoked");
                                                newRequestMap.put("Teacher To School Request School/" + entityId + "/" + mFirebaseUser.getUid() + "/" + pendingRequestKey + "/" + "status", "Revoked");
                                                newRequestMap.put("NotificationSchool/" + entityId + "/" + pendingRequestKey, null);
                                            }

                                            newRef.updateChildren(newRequestMap);
                                        }

                                        holder.sendRequest.setText("Connect");
                                        holder.sendRequest.setBackgroundResource(R.drawable.roundedbutton);
                                        holder.sendRequest.setTextColor(Color.WHITE);
                                        pendingOutgoingRequests.remove(entityId);
                                        holder.sendRequest.setEnabled(true);
                                        notifyDataSetChanged();
                                        customProgressDialogOne.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                    }
                    else if (holder.sendRequest.getText().equals("Disconnect")) {

                        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                        int width = metrics.widthPixels;
                        int height = metrics.heightPixels;
                        final Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.custom_dialog_request_connection);
                        TextView message = (TextView) dialog.findViewById(R.id.dialogmessage);
                        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
                        TextView action = (TextView) dialog.findViewById(R.id.action);
                        dialog.show();
//                        dialog.getWindow().setLayout((19 * width) / 20, RecyclerView.LayoutParams.WRAP_CONTENT);

                        String messageString = "Disconnecting would restrict your access to all " + "<b>" + entityName + "</b>" + "'s information, including class and " +
                                "student information. To regain access, you'll need to send a new request. Do you wish to disconnect?";
                        message.setText(Html.fromHtml(messageString));

                        action.setText("Disconnect");

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        action.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                dialog.dismiss();
                                customProgressDialogOne.show();
                                holder.sendRequest.setEnabled(false);

                                String time = Date.getDate();
                                String sorttableTime = Date.convertToSortableDate(time);

                                final Map<String, Object> newDisconnectionMap = new HashMap<String, Object>();
                                final DatabaseReference newDisconnectionRef = mFirebaseDatabase.getReference();
                                String notificationPushID = mFirebaseDatabase.getReference().child("NotificationSchool").child(entityId).push().getKey();
                                String disconnectionKey = mFirebaseDatabase.getReference("Teacher To School Request Teacher").child(mFirebaseUser.getUid()).child(entityId).push().getKey();
                                TeacherSchoolConnectionRequest teacherSchoolConnectionRequest = new TeacherSchoolConnectionRequest("Disconnected", time, sorttableTime, mFirebaseUser.getUid(), entityId);
                                NotificationModel notificationModel = new NotificationModel(mFirebaseUser.getUid(), entityId, "School", "Teacher", time, sorttableTime, notificationPushID, "Disconnection", "", "", false);

                                newDisconnectionMap.put("Teacher School/" + mFirebaseUser.getUid() + "/" + entityId, null);
                                newDisconnectionMap.put("School Teacher/" + entityId + "/" + mFirebaseUser.getUid(), null);
                                newDisconnectionMap.put("School To Teacher Request Teacher/" + mFirebaseUser.getUid() + "/" + entityId + "/" + disconnectionKey, teacherSchoolConnectionRequest);
                                newDisconnectionMap.put("School To Teacher Request School/" + entityId + "/" + mFirebaseUser.getUid() + "/" + disconnectionKey, teacherSchoolConnectionRequest);
                                newDisconnectionMap.put("NotificationSchool/" + entityId + "/" + notificationPushID, notificationModel);

                                final ArrayList<String> classesToBeRemoved = new ArrayList<String>();
                                final ArrayList<String> studentsToBeRemoved = new ArrayList<String>();

                                mDatabaseReference = mFirebaseDatabase.getReference().child("School Class").child(entityId);
                                mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                classesToBeRemoved.add(postSnapshot.getKey());
                                                newDisconnectionMap.put("Teacher Class/" + mFirebaseUser.getUid() + "/" + postSnapshot.getKey(), null);
                                                newDisconnectionMap.put("Class Teacher/" + postSnapshot.getKey() + "/" + mFirebaseUser.getUid(), null);
                                            }
                                        }

                                        for (String classToBeRemoved : classesToBeRemoved) {
                                            final int classesToBeRemovedCount = classesToBeRemoved.size();
                                            mDatabaseReference = mFirebaseDatabase.getReference().child("Class Student").child(classToBeRemoved);
                                            mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    classesToBeRemovedCounter++;
                                                    if (dataSnapshot.exists()) {
                                                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                            studentsToBeRemoved.add(postSnapshot.getKey());
                                                            newDisconnectionMap.put("Teacher Student/" + mFirebaseUser.getUid() + "/" + postSnapshot.getKey(), null);
                                                            newDisconnectionMap.put("Student Teacher/" + postSnapshot.getKey() + "/" + mFirebaseUser.getUid(), null);
                                                        }
                                                    }

                                                    if (classesToBeRemovedCounter == classesToBeRemovedCount) {
                                                        holder.sendRequest.setText("Connect");
                                                        holder.sendRequest.setBackgroundResource(R.drawable.roundedbutton);
                                                        holder.sendRequest.setTextColor(Color.WHITE);
                                                        newDisconnectionRef.updateChildren(newDisconnectionMap);
                                                        existingConnections.remove(entityId);
                                                        holder.sendRequest.setEnabled(true);
                                                        notifyDataSetChanged();
                                                        customProgressDialogOne.dismiss();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                    }
                    else if (holder.sendRequest.getText().equals("Respond")) {

                        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                        int width = metrics.widthPixels;
                        int height = metrics.heightPixels;
                        final Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.custom_dialog_request_connection);
                        TextView message = (TextView) dialog.findViewById(R.id.dialogmessage);
                        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
                        TextView action = (TextView) dialog.findViewById(R.id.action);
                        dialog.show();
//                        dialog.getWindow().setLayout((19 * width) / 20, RecyclerView.LayoutParams.WRAP_CONTENT);

                        String messageString = "<b>" + entityName + "</b>" + " sent you a connection request, accepting this request will give you access to their students, classes and data. Do you " +
                                "wish to accept this request?";
                        message.setText(Html.fromHtml(messageString));

                        action.setText("Accept");
                        cancel.setText("Decline");

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                customProgressDialogOne.show();
                                holder.sendRequest.setEnabled(false);

                                String time = Date.getDate();
                                String sorttableTime = Date.convertToSortableDate(time);

                                final String notificationPushID = mFirebaseDatabase.getReference().child("NotificationSchool").child(entityId).push().getKey();
                                final NotificationModel notificationModel = new NotificationModel(mFirebaseUser.getUid(), entityId, "School", "Teacher", time, sorttableTime, notificationPushID, "ConnectionRequestDeclined", "", "", false);


                                mDatabaseReference = mFirebaseDatabase.getReference("School To Teacher Request Teacher").child(mFirebaseUser.getUid()).child(entityId);
                                mDatabaseReference.orderByChild("status").equalTo("Pending").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {

                                            Map<String, Object> newRequestMap = new HashMap<String, Object>();
                                            DatabaseReference newRef = mFirebaseDatabase.getReference();
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                String pendingRequestKey = postSnapshot.getKey();
                                                newRequestMap.put("School To Teacher Request Teacher/" + mFirebaseUser.getUid() + "/" + entityId + "/" + pendingRequestKey + "/" + "status", "Declined");
                                                newRequestMap.put("School To Teacher Request School/" + entityId + "/" + mFirebaseUser.getUid() + "/" + pendingRequestKey + "/" + "status", "Declined");
                                                newRequestMap.put("NotificationSchool/" + entityId + "/" + notificationPushID, notificationModel);
                                            }
                                            newRef.updateChildren(newRequestMap);
                                        }

                                        holder.sendRequest.setText("Connect");
                                        holder.sendRequest.setBackgroundResource(R.drawable.roundedbutton);
                                        holder.sendRequest.setTextColor(Color.WHITE);
                                        pendingIncomingRequests.remove(entityId);
                                        holder.sendRequest.setEnabled(true);
                                        notifyDataSetChanged();
                                        customProgressDialogOne.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                        action.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                dialog.dismiss();
                                customProgressDialogOne.show();
                                holder.sendRequest.setEnabled(false);

                                String time = Date.getDate();
                                String sorttableTime = Date.convertToSortableDate(time);

                                final Map<String, Object> newConnectionMap = new HashMap<String, Object>();
                                final DatabaseReference newConnectionRef = mFirebaseDatabase.getReference();
                                final String notificationPushID = mFirebaseDatabase.getReference().child("NotificationSchool").child(entityId).push().getKey();
                                final NotificationModel notificationModel = new NotificationModel(mFirebaseUser.getUid(), entityId, "School", "Teacher", time, sorttableTime, notificationPushID, "Connection", "", "", false);

                                mDatabaseReference = mFirebaseDatabase.getReference("School To Teacher Request Teacher").child(mFirebaseUser.getUid()).child(entityId);
                                mDatabaseReference.orderByChild("status").equalTo("Pending").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {

                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                String pendingRequestKey = postSnapshot.getKey();
                                                newConnectionMap.put("School To Teacher Request Teacher/" + mFirebaseUser.getUid() + "/" + entityId + "/" + pendingRequestKey + "/" + "status", "Accepted");
                                                newConnectionMap.put("School To Teacher Request School/" + entityId + "/" + mFirebaseUser.getUid() + "/" + pendingRequestKey + "/" + "status", "Accepted");
                                                newConnectionMap.put("School Teacher/" + entityId + "/" + mFirebaseUser.getUid(), true);
                                                newConnectionMap.put("Teacher School/" + mFirebaseUser.getUid() + "/" + entityId, true);
                                                newConnectionMap.put("NotificationSchool/" + entityId + "/" + notificationPushID, notificationModel);
                                            }
                                        }

                                        holder.sendRequest.setText("Disconnect");
                                        holder.sendRequest.setBackgroundResource(R.drawable.roundedbuttonaccent);
                                        holder.sendRequest.setTextColor(Color.WHITE);
                                        newConnectionRef.updateChildren(newConnectionMap);
                                        existingConnections.add(entityId);
                                        pendingIncomingRequests.remove(entityId);
                                        holder.sendRequest.setEnabled(true);
                                        notifyDataSetChanged();
                                        customProgressDialogOne.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }

                }
                else if (sharedPreferencesManager.getActiveAccount().equals("Parent")) {

                    if (holder.sendRequest.getText().equals("Connect")) {

                        customProgressDialogOne.show();
                        holder.sendRequest.setText("Revoke");
                        holder.sendRequest.setBackgroundResource(R.drawable.roundedbuttonwhite);
                        holder.sendRequest.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryPurple));

                        mDatabaseReference = mFirebaseDatabase.getReference("Student Connection Request Sender").child(mFirebaseUser.getUid()).push();
                        String refKey = mDatabaseReference.getKey();

                        Map<String, Object> newRequestMap = new HashMap<String, Object>();
                        mDatabaseReference = mFirebaseDatabase.getReference();

                        String timeSent = Date.getDate();
                        String sorttableTimeSent = Date.convertToSortableDate(timeSent);
                        ParentSchoolConnectionRequest parentSchoolConnectionRequest = new ParentSchoolConnectionRequest("Pending", timeSent, sorttableTimeSent, mFirebaseUser.getUid(), "Parent", entityId, refKey, "", "", guardians.get(entityId));

                        newRequestMap.put("Student Connection Request Sender/" + mFirebaseUser.getUid() + "/" + refKey, parentSchoolConnectionRequest);

                        if (guardians.get(entityId) != null && guardians.get(entityId).size() != 0) {
                            for (int i = 0; i < guardians.get(entityId).size(); i++) {
                                String recipientID = guardians.get(entityId).get(i).split(" ")[0];
                                String recipientAccountType = guardians.get(entityId).get(i).split(" ")[1];

                                if (!recipientID.equals(mFirebaseUser.getUid())) {
                                    NotificationModel notificationModel = new NotificationModel(mFirebaseUser.getUid(), recipientID, recipientAccountType, "Parent", timeSent, sorttableTimeSent, refKey, "Connection Request", searchResultsRow.getEntityPic(), entityName, false);

                                    newRequestMap.put("Student Connection Request Recipients/" + recipientID + "/" + refKey, parentSchoolConnectionRequest);
                                    if (recipientAccountType.equals("School")) {
                                        newRequestMap.put("NotificationSchool/" + recipientID + "/" + refKey, notificationModel);
                                    } else if (recipientAccountType.equals("Parent")) {
                                        newRequestMap.put("NotificationParent/" + recipientID + "/" + refKey, notificationModel);
                                    }
                                }
                            }
                        } else {
                            //Todo: lost student account
                        }

                        mDatabaseReference.updateChildren(newRequestMap);
                        pendingOutgoingRequests.add(entityId);
                        notifyDataSetChanged();
                        customProgressDialogOne.dismiss();

                    }
                    else if (holder.sendRequest.getText().equals("Revoke")) {

                        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                        int width = metrics.widthPixels;
                        int height = metrics.heightPixels;
                        final Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.custom_dialog_request_connection);
                        TextView message = (TextView) dialog.findViewById(R.id.dialogmessage);
                        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
                        TextView action = (TextView) dialog.findViewById(R.id.action);
                        dialog.show();

                        String messageString = "Do you want to revoke your request to connect to " + "<b>" + entityName + "</b>" + "'s profile?";
                        message.setText(Html.fromHtml(messageString));

                        action.setText("Revoke");

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        action.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                customProgressDialogOne.show();
                                holder.sendRequest.setEnabled(false);

                                final ArrayList<String> pendingRequestKeys = new ArrayList<String>();
                                mDatabaseReference = mFirebaseDatabase.getReference("Student Connection Request Sender").child(mFirebaseUser.getUid());
                                mDatabaseReference.orderByChild("requestStatus").equalTo("Pending").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Map<String, Object> newRequestMap = new HashMap<String, Object>();
                                            DatabaseReference newRef = mFirebaseDatabase.getReference();
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                String pendingRequestKey = postSnapshot.getKey();
                                                ParentSchoolConnectionRequest parentSchoolConnectionRequest = postSnapshot.getValue(ParentSchoolConnectionRequest.class);
                                                if (parentSchoolConnectionRequest.getStudentID().equals(entityId))
                                                {
                                                    ArrayList<String> recipients = parentSchoolConnectionRequest.getRequestReciepients();
                                                    newRequestMap.put("Student Connection Request Sender/" + mFirebaseUser.getUid() + "/" + pendingRequestKey + "/requestStatus", "Revoked");

                                                    if (recipients == null) continue;
                                                    if (recipients.size() == 0) continue;

                                                    for (int i = 0; i < recipients.size(); i++) {
                                                        String recipientID = recipients.get(i).split(" ")[0];
                                                        String recipientAccountType = recipients.get(i).split(" ")[1];

                                                        newRequestMap.put("Student Connection Request Recipients/" + recipientID + "/" + pendingRequestKey + "/requestStatus", "Revoked");
                                                        newRequestMap.put("NotificationSchool/" + recipientID + "/" + pendingRequestKey, null);
                                                        newRequestMap.put("NotificationParent/" + recipientID + "/" + pendingRequestKey, null);
                                                    }
                                                    break;
                                                }
                                            }

                                            holder.sendRequest.setText("Connect");
                                            holder.sendRequest.setBackgroundResource(R.drawable.roundedbutton);
                                            holder.sendRequest.setTextColor(Color.WHITE);
                                            newRef.updateChildren(newRequestMap);
                                            pendingOutgoingRequests.remove(entityId);
                                            holder.sendRequest.setEnabled(true);
                                            notifyDataSetChanged();
                                            customProgressDialogOne.dismiss();
                                        } else {
                                            holder.sendRequest.setEnabled(true);
                                            holder.sendRequest.setText("Connect");
                                            holder.sendRequest.setBackgroundResource(R.drawable.roundedbutton);
                                            holder.sendRequest.setTextColor(Color.WHITE);
                                            customProgressDialogOne.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                    }
                    else if (holder.sendRequest.getText().equals("Disconnect")) {
                        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                        int width = metrics.widthPixels;
                        int height = metrics.heightPixels;
                        final Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.custom_dialog_request_connection);
                        TextView message = (TextView) dialog.findViewById(R.id.dialogmessage);
                        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
                        TextView action = (TextView) dialog.findViewById(R.id.action);
                        dialog.show();

                        String messageString = "Disconnecting would restrict your access to " + "<b>" + entityName + "</b>" + "'s information, including class stories and " +
                                "attendance information. To regain access, you'll need to send a new request to their school. Do you wish to disconnect?";
                        message.setText(Html.fromHtml(messageString));

                        cancel.setText("Cancel");
                        action.setText("Disconnect");

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        action.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                customProgressDialogOne.show();
                                holder.sendRequest.setEnabled(false);

                                String timeSent = Date.getDate();
                                String sorttableTimeSent = Date.convertToSortableDate(timeSent);

                                Map<String, Object> newDisconnectionMap = new HashMap<String, Object>();
                                DatabaseReference newDisconnectionRef = mFirebaseDatabase.getReference().child("Disconnection Subject").child(mFirebaseUser.getUid()).push();
                                String disconnectionRefKey = newDisconnectionRef.getKey();
                                DisconnectionModel disconnectionModel = new DisconnectionModel(mFirebaseUser.getUid(), entityId, disconnectionRefKey, timeSent, sorttableTimeSent);

                                newDisconnectionMap.put("Parents Students/" + mFirebaseUser.getUid() + "/" + entityId, null);
                                newDisconnectionMap.put("Student Parent/" + entityId + "/" + mFirebaseUser.getUid(), null);
                                newDisconnectionMap.put("Disconnection Subject/" + mFirebaseUser.getUid() + "/" + disconnectionRefKey, disconnectionModel);
                                newDisconnectionMap.put("Disconnection Object/" + entityId + "/" + disconnectionRefKey, disconnectionModel);

                                if (guardians.get(entityId) != null) {
                                    if (guardians.get(entityId).size() != 0) {
                                        for (int i = 0; i < guardians.get(entityId).size(); i++) {
                                            String recipientID = guardians.get(entityId).get(i).split(" ")[0];
                                            String recipientAccountType = guardians.get(entityId).get(i).split(" ")[1];

                                            if (!recipientID.equals(mFirebaseUser.getUid())) {
                                                NotificationModel notificationModel = new NotificationModel(mFirebaseUser.getUid(), recipientID, recipientAccountType, "Parent", timeSent, sorttableTimeSent, disconnectionRefKey, "Disconnection", searchResultsRow.getEntityPic(), entityId, false);

                                                if (recipientAccountType.equals("School")) {
                                                    newDisconnectionMap.put("NotificationSchool/" + recipientID + "/" + disconnectionRefKey, notificationModel);
                                                } else if (recipientAccountType.equals("Parent")) {
                                                    newDisconnectionMap.put("NotificationParent/" + recipientID + "/" + disconnectionRefKey, notificationModel);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    //Todo: lost student account
                                }

                                newDisconnectionRef = mFirebaseDatabase.getReference();
                                holder.sendRequest.setText("Connect");
                                holder.sendRequest.setBackgroundResource(R.drawable.roundedbutton);
                                holder.sendRequest.setTextColor(Color.WHITE);
                                newDisconnectionRef.updateChildren(newDisconnectionMap);
                                existingConnections.remove(entityId);
                                notifyDataSetChanged();
                                holder.sendRequest.setEnabled(true);
                                customProgressDialogOne.dismiss();
                            }
                        });
                    }
                }
            }
        });

        holder.clickableView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (sharedPreferencesManager.getActiveAccount().equals("Parent")) {
                    if (searchResultsRow.getEntityType().equals("Student")){
                        if (!existingConnections.contains(entityId)) {
                            String messageString = "You don't have the permission to view " + "<b>" + entityName + "</b>" + "'s information. If you" +
                                    " know " + "<b>" + entityName + "</b>" + " and would like to access their information, send a connection request to their school by using" +
                                    " the " + "<b>" + "Connect" + "</b>" + " button.";
                            showDialogWithMessage(Html.fromHtml(messageString));
                            return;
                        }
                    }
                }

                if (!CheckNetworkConnectivity.isNetworkAvailable(context)) {
                    CustomToast.blueBackgroundToast(context, "Your device is not connected to the internet. Check your connection and try again.");
                    return;
                }

                Bundle bundle = new Bundle();
                Intent I;
                if (searchResultsRow.getEntityType().equals("School")){
                    bundle.putString("schoolID", entityId);
                    I = new Intent(context, SchoolProfileActivity.class);
                }
                else if (searchResultsRow.getEntityType().equals("Student")) {
                    bundle.putString("childID", entityId);
                    I = new Intent(context, StudentProfileActivity.class);
                }
                else {
                    bundle.putString("ID", entityId);
                    I = new Intent(context, TeacherProfileOneActivity.class);
                }

                String time = Date.getDate();

                SearchHistoryRow searchHistoryRow = new SearchHistoryRow(entityId, searchResultsRow.getEntityName(), searchResultsRow.getEntityAddress(), searchResultsRow.getEntityType(), time);
                Map<String, Object> searchHistoryObject = new HashMap<String, Object>();

                if (sharedPreferencesManager.getActiveAccount().equals("Teacher")) {
                    searchHistoryObject.put("MySearchHistory/Teachers/" + mFirebaseUser.getUid() + "/" + entityId, searchHistoryRow);
                }
                else if (sharedPreferencesManager.getActiveAccount().equals("Parent")) {
                    searchHistoryObject.put("MySearchHistory/Parents/" + mFirebaseUser.getUid() + "/" + entityId, searchHistoryRow);
                }

                mDatabaseReference = mFirebaseDatabase.getReference();
                mDatabaseReference.updateChildren(searchHistoryObject);

                I.putExtras(bundle);
                context.startActivity(I);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchResultsRowList.size();
    }

    void showDialogWithMessage (Spanned messageString) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_unary_message_dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        TextView message = (TextView) dialog.findViewById(R.id.dialogmessage);
        TextView OK = (TextView) dialog.findViewById(R.id.optionone);
        dialog.show();

        message.setText(messageString);

        OK.setText("OK");

        OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}