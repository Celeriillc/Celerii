package com.celerii.celerii.Activities.Inbox.Teacher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.celerii.celerii.Activities.Search.Parent.ParentSearchActivity;
import com.celerii.celerii.Activities.Search.Teacher.SearchActivity;
import com.celerii.celerii.R;
import com.celerii.celerii.adapters.NewChatRowAdapter;
import com.celerii.celerii.helperClasses.Analytics;
import com.celerii.celerii.helperClasses.CheckNetworkConnectivity;
import com.celerii.celerii.helperClasses.Date;
import com.celerii.celerii.helperClasses.SharedPreferencesManager;
import com.celerii.celerii.models.ClassesStudentsAndParentsModel;
import com.celerii.celerii.models.NewChatRowModel;
import com.celerii.celerii.models.Parent;
import com.celerii.celerii.models.Student;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class TeacherParentMessageList extends Fragment {

    Context context;
    SharedPreferencesManager sharedPreferencesManager;

    FirebaseAuth auth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    FirebaseUser mFirebaseUser;

    private ArrayList<NewChatRowModel> newChatRowModelList;
    public RecyclerView recyclerView;
    public NewChatRowAdapter mAdapter;
    LinearLayoutManager mLayoutManager;

    SwipeRefreshLayout mySwipeRefreshLayout;
    RelativeLayout errorLayout, progressLayout;
    TextView errorLayoutText;
    Button errorLayoutButton;

    ArrayList<String> kidsList;
    ArrayList<String> classList, parentIDList;
    ArrayList<ClassesStudentsAndParentsModel> classesStudentsAndParentsModelList;
    HashMap<String, NewChatRowModel> parentList;
    int counter = 0;

    String featureUseKey = "";
    String featureName = "Teacher New Message to Parents";
    long sessionStartTime = 0;
    String sessionDurationInSeconds = "0";

    public TeacherParentMessageList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teacher_parent_message_list, container, false);

        context = getContext();
        sharedPreferencesManager = new SharedPreferencesManager(context);

        auth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mFirebaseUser = auth.getCurrentUser();

        mySwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        errorLayout = (RelativeLayout) view.findViewById(R.id.errorlayout);
        errorLayoutText = (TextView) errorLayout.findViewById(R.id.errorlayouttext);
        errorLayoutButton = (Button) errorLayout.findViewById(R.id.errorlayoutbutton);
        progressLayout = (RelativeLayout) view.findViewById(R.id.progresslayout);

        recyclerView.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);

        newChatRowModelList = new ArrayList<>();
        mAdapter = new NewChatRowAdapter(newChatRowModelList, getContext());
        recyclerView.setAdapter(mAdapter);
        loadFromFirebase();

        kidsList = new ArrayList<>();
        classList = new ArrayList<>();
        parentIDList = new ArrayList<>();
        parentList = new HashMap<>();

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        loadFromFirebase();
                    }
                }
        );

        errorLayoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, SearchActivity.class));
            }
        });

        return view;
    }

    void loadFromFirebase(){
        if (!CheckNetworkConnectivity.isNetworkAvailable(getContext())) {
            mySwipeRefreshLayout.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            progressLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            errorLayoutText.setText("Your device is not connected to the internet. Check your connection and try again.");
            return;
        }

        counter = 0;
        Gson gson = new Gson();
        classesStudentsAndParentsModelList = new ArrayList<>();
        String classStudentParentJSON = sharedPreferencesManager.getClassesStudentParent();
        Type type = new TypeToken<ArrayList<ClassesStudentsAndParentsModel>>() {}.getType();
        classesStudentsAndParentsModelList = gson.fromJson(classStudentParentJSON, type);

        if (classesStudentsAndParentsModelList == null) {
            classesStudentsAndParentsModelList = new ArrayList<>();
            mySwipeRefreshLayout.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            progressLayout.setVisibility(View.GONE);
            mySwipeRefreshLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            errorLayoutText.setText(Html.fromHtml("You don't have any parents to message at this time. If you're not connected to any of your classes' account. Click the " + "<b>" + "Search" + "</b>" + " button to search for your school to access your classes or get started by clicking the " + "<b>" + "Find my school" + "</b>" + " button below"));
            errorLayoutButton.setText("Find my school");
            errorLayoutButton.setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < classesStudentsAndParentsModelList.size(); i++) {
                final ClassesStudentsAndParentsModel classesStudentsAndParentsModel = classesStudentsAndParentsModelList.get(i);
                mDatabaseReference = mFirebaseDatabase.getReference().child("Parent").child(classesStudentsAndParentsModel.getParentID());
                mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Parent parent = dataSnapshot.getValue(Parent.class);
                            final String parentID = dataSnapshot.getKey();
                            final String parentName = parent.getLastName() + " " + parent.getFirstName();
                            final String parentProfilePictureURL = parent.getProfilePicURL();

                            mDatabaseReference = mFirebaseDatabase.getReference().child("Student").child(classesStudentsAndParentsModel.getStudentID());
                            mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    counter++;
                                    if (dataSnapshot.exists()) {
                                        Student student = dataSnapshot.getValue(Student.class);
                                        String studentFirstName = student.getFirstName();
                                        String relationship = studentFirstName + "'s parent";

                                        NewChatRowModel newChatRowModel = new NewChatRowModel(parentName, relationship, parentProfilePictureURL, parentID);
                                        if (!parentList.containsKey(parentID)){
                                            parentList.put(parentID, newChatRowModel);
                                            newChatRowModelList.add(newChatRowModel);
                                        }

                                        if (counter == classesStudentsAndParentsModelList.size()) {
                                            Collections.sort(newChatRowModelList, new Comparator<NewChatRowModel>() {
                                                @Override
                                                public int compare(NewChatRowModel o1, NewChatRowModel o2) {
                                                    return o1.getName().compareTo(o2.getName());
                                                }
                                            });
                                            mAdapter.notifyDataSetChanged();
                                            mySwipeRefreshLayout.setRefreshing(false);
                                            progressLayout.setVisibility(View.GONE);
                                            errorLayout.setVisibility(View.GONE);
                                            recyclerView.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            counter++;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }




//        mDatabaseReference = mFirebaseDatabase.getReference().child("Teacher Class").child(mFirebaseUser.getUid());
//        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//                    kidsList.clear();
//                    classList.clear();
//                    parentList.clear();
//                    newChatRowModelList.clear();
//                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
//                        String classID = postSnapshot.getKey();
//                        classList.add(classID);
//                    }
//
//                    for (final String classID : classList) {
//                        mDatabaseReference = mFirebaseDatabase.getReference().child("Class Students").child(classID);
//                        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                if (dataSnapshot.exists()){
//                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
//                                        final String kidID = postSnapshot.getKey();
//                                        kidsList.add(kidID);
//
//                                        mDatabaseReference = mFirebaseDatabase.getReference().child("Student").child(kidID);
//                                        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                                            @Override
//                                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                                if (dataSnapshot.exists()){
//                                                    Student student = dataSnapshot.getValue(Student.class);
//                                                    final String studentName = student.getFirstName() + "'s";
//
//                                                    mDatabaseReference = mFirebaseDatabase.getReference().child("Student Parent").child(kidID);
//                                                    mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                                                        @Override
//                                                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                                            if (dataSnapshot.exists()){
//                                                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
//                                                                    final String parentID = postSnapshot.getKey();
//                                                                    parentIDList.add(parentID);
//
//                                                                    mDatabaseReference = mFirebaseDatabase.getReference().child("Parent").child(parentID);
//                                                                    mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                                                                        @Override
//                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                                                            if (dataSnapshot.exists()){
//                                                                                Parent parent = dataSnapshot.getValue(Parent.class);
//                                                                                String parentName = parent.getLastName() + " " + parent.getFirstName();
//                                                                                String relationship = studentName + " Parent";
//                                                                                NewChatRowModel newChatRowModel = new NewChatRowModel(parentName, relationship, parent.getProfilePicURL(), parentID);
//
//                                                                                if (!parentList.containsKey(parentID)){
//                                                                                    parentList.put(parentID, newChatRowModel);
//                                                                                    newChatRowModelList.add(newChatRowModel);
//                                                                                }
////                                                                                else {
////                                                                                    String newRelationship = parentList.get(parentID).getRelationship() + ", " + relationship;
////                                                                                    parentList.get(parentID).setRelationship(newRelationship);
////                                                                                    mAdapter.notifyDataSetChanged();
////                                                                                }
//
//                                                                                Collections.sort(newChatRowModelList, new Comparator<NewChatRowModel>() {
//                                                                                    @Override
//                                                                                    public int compare(NewChatRowModel o1, NewChatRowModel o2) {
//                                                                                        return o1.getName().compareTo(o2.getName());
//                                                                                    }
//                                                                                });
//                                                                                mAdapter.notifyDataSetChanged();
//                                                                                mySwipeRefreshLayout.setRefreshing(false);
//                                                                                progressLayout.setVisibility(View.GONE);
//                                                                                recyclerView.setVisibility(View.VISIBLE);
//                                                                            }
//                                                                        }
//
//                                                                        @Override
//                                                                        public void onCancelled(DatabaseError databaseError) {
//
//                                                                        }
//                                                                    });
//                                                                }
//                                                            }
//                                                        }
//
//                                                        @Override
//                                                        public void onCancelled(DatabaseError databaseError) {
//
//                                                        }
//                                                    });
//                                                }
//                                            }
//
//                                            @Override
//                                            public void onCancelled(DatabaseError databaseError) {
//
//                                            }
//                                        });
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (sharedPreferencesManager.getActiveAccount().equals("Parent")) {
            featureUseKey = Analytics.featureAnalytics("Parent", mFirebaseUser.getUid(), featureName);
        } else {
            featureUseKey = Analytics.featureAnalytics("Teacher", mFirebaseUser.getUid(), featureName);
        }
        sessionStartTime = System.currentTimeMillis();
    }

    @Override
    public void onStop() {
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
}
