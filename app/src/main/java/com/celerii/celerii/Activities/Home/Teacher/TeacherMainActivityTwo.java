package com.celerii.celerii.Activities.Home.Teacher;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.notification.AHNotification;
import com.celerii.celerii.Activities.Home.RemoteCampaignActivity;
import com.celerii.celerii.Activities.Inbox.InboxFragment;
import com.celerii.celerii.Activities.Intro.IntroSlider;
import com.celerii.celerii.Activities.Search.Teacher.SearchActivity;
import com.celerii.celerii.Activities.Settings.TutorialsActivity;
import com.celerii.celerii.R;
import com.celerii.celerii.helperClasses.Analytics;
import com.celerii.celerii.helperClasses.ApplicationLauncherSharedPreferences;
import com.celerii.celerii.helperClasses.CheckDeletedState;
import com.celerii.celerii.helperClasses.CheckLoggedInState;
import com.celerii.celerii.helperClasses.CustomToast;
import com.celerii.celerii.helperClasses.LogoutProtocol;
import com.celerii.celerii.helperClasses.ServerDeviceTimeDifference;
import com.celerii.celerii.helperClasses.SharedPreferencesManager;
import com.celerii.celerii.helperClasses.UpdateDataFromFirebase;
import com.celerii.celerii.models.Class;
import com.celerii.celerii.models.NotificationBadgeModel;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.celerii.celerii.models.RemoteCampaign;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TeacherMainActivityTwo extends AppCompatActivity {
    Context context = this;

    SharedPreferencesManager sharedPreferencesManager;
    ApplicationLauncherSharedPreferences applicationLauncherSharedPreferences;
    FirebaseAuth auth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    FirebaseUser mFirebaseUser;
    FirebaseAuth.IdTokenListener authIDTokenListener;
    AHBottomNavigation bottomNavigation;
    FragmentTransaction mFragmentTransaction;
    Fragment frag1, frag2, frag3, frag4, frag5, active;
    Toolbar toolbar;
    TextView toolbarTitle;

    //Onboard variables
    Dialog dialog;
    int[] layouts;
    ViewPager viewPager;
    LinearLayout dotsLayout;
    Button next;
    private TextView[] dots;
    MyViewPagerAdapter myViewPagerAdapter;
    LinearLayout onBoardingSearchBalloon;
    LinearLayout tutorialBalloon;
    Button dismissTutorialBalloon;

    long sessionStartTime = 0;
    String sessionDurationInSeconds = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main_two);

        sharedPreferencesManager = new SharedPreferencesManager(this);
        applicationLauncherSharedPreferences = new ApplicationLauncherSharedPreferences(this);
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        toolbar = (Toolbar) findViewById(R.id.hometoolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbartitle);
        onBoardingSearchBalloon = (LinearLayout) findViewById(R.id.onboardingsearchballon);
        onBoardingSearchBalloon = (LinearLayout) findViewById(R.id.onboardingsearchballon);
        tutorialBalloon = (LinearLayout) findViewById(R.id.tutorialballoon);
        dismissTutorialBalloon = (Button) findViewById(R.id.dismisstutorialballoon);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.drawable.ic_icons_google);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTitle.setText("Celerii Teacher");
//        getSupportActionBar().setElevation(0);

        auth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mFirebaseUser = auth.getCurrentUser();
        if (mFirebaseUser == null){
            sharedPreferencesManager.clear();
            Intent I = new Intent(TeacherMainActivityTwo.this, IntroSlider.class);
            applicationLauncherSharedPreferences.setLauncherActivity("IntroSlider");
            startActivity(I);
            finish();
            return;
        }

        // Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Class Home", R.drawable.ic_home_white_24dp, R.color.colorPrimary);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("Class Feed", R.drawable.ic_newspaper, R.color.colorPrimary);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem("Inbox", R.drawable.ic_chat_black_24dp, R.color.colorPrimary);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem("Notifications", R.drawable.ic_notifications_black_24dp, R.color.colorPrimary);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem("More", R.drawable.ic_more_black_24dp, R.color.colorPrimary);

        // Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
        bottomNavigation.addItem(item4);
        bottomNavigation.addItem(item5);

        bottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimaryPurple));
        bottomNavigation.setInactiveColor(ContextCompat.getColor(this, R.color.colorDeepGray));
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bottomNavigation.setCurrentItem(0);
        loadBadgesFromFirebase();
        initFCM();
        onBoardFirebaseCheck();
        onBoardingSearchBalloonCheck();
//        tutorialFirebaseCheck();
        remoteCampaign();
        bottomNavigation.setUseElevation(false);

        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        frag1 = new TeacherHomeClass();
        frag2 = new TeacherHomeClassFeed();
        frag3 = new InboxFragment();
        frag4 = new TeacherHomeNotification();
        frag5 = new MoreTeacherFragment();

        bottomNavigation.setNotification("", 0);
//        mFragmentTransaction.replace(R.id.frame_fragmentholder, frag1);
//        mFragmentTransaction.commit();

        Bundle fromNotificationBundle = getIntent().getExtras();
        if (fromNotificationBundle != null) {
            String fragNumber = fromNotificationBundle.getString("Fragment Int");
            if (fragNumber.equals("1")) {
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag5).hide(frag5);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag4).hide(frag4);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag3).hide(frag3);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag1).hide(frag1);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag2);
                mFragmentTransaction.commit();
                bottomNavigation.setCurrentItem(1);
                bottomNavigation.setNotification("", 1);
                active = frag1;
            } else if (fragNumber.equals("2")) {
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag5).hide(frag5);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag4).hide(frag4);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag2).hide(frag2);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag1).hide(frag1);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag3);
                mFragmentTransaction.commit();
                bottomNavigation.setCurrentItem(2);
                bottomNavigation.setNotification("", 2);
                active = frag1;
            } else if (fragNumber.equals("3")) {
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag5).hide(frag5);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag3).hide(frag3);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag2).hide(frag2);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag1).hide(frag1);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag4);
                mFragmentTransaction.commit();
                bottomNavigation.setCurrentItem(3);
                bottomNavigation.setNotification("", 3);
                active = frag1;
            } else if (fragNumber.equals("4")) {
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag4).hide(frag4);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag3).hide(frag3);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag2).hide(frag2);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag1).hide(frag1);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag5);
                mFragmentTransaction.commit();
                bottomNavigation.setCurrentItem(4);
                bottomNavigation.setNotification("", 4);
                active = frag1;
            } else {
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag5).hide(frag5);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag4).hide(frag4);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag3).hide(frag3);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag2).hide(frag2);
                mFragmentTransaction.add(R.id.frame_fragmentholder, frag1);
                mFragmentTransaction.commit();
                bottomNavigation.setCurrentItem(0);
                bottomNavigation.setNotification("", 0);
                active = frag1;
            }
        } else {
            mFragmentTransaction.add(R.id.frame_fragmentholder, frag5).hide(frag5);
            mFragmentTransaction.add(R.id.frame_fragmentholder, frag4).hide(frag4);
            mFragmentTransaction.add(R.id.frame_fragmentholder, frag3).hide(frag3);
            mFragmentTransaction.add(R.id.frame_fragmentholder, frag2).hide(frag2);
            mFragmentTransaction.add(R.id.frame_fragmentholder, frag1);
            mFragmentTransaction.commit();
            bottomNavigation.setCurrentItem(0);
            bottomNavigation.setNotification("", 0);
            active = frag1;
        }


        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switch (position) {
                    case 0:
                        bottomNavigation.setNotification("", 0);
//                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragmentholder, frag1).commit();
                        getSupportFragmentManager().beginTransaction().hide(active).show(frag1).commit();
                        active = frag1;
                        return true;
                    case 1:
                        bottomNavigation.setNotification("", 1);
//                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragmentholder, frag2).commit();
                        getSupportFragmentManager().beginTransaction().hide(active).show(frag2).commit();
                        active = frag2;
                        return true;
                    case 2:
                        DatabaseReference bottomNavBadgeRef = mFirebaseDatabase.getReference();
                        Map<String, Object> bottomNavBadgeMap = new HashMap<String, Object>();
                        NotificationBadgeModel notificationBadgeModel = new NotificationBadgeModel(false, 0);
                        bottomNavBadgeMap.put("Notification Badges/General/" + mFirebaseUser.getUid() + "/Inbox", notificationBadgeModel);
                        bottomNavBadgeRef.updateChildren(bottomNavBadgeMap);
                        bottomNavigation.setNotification("", 2);
//                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragmentholder, frag3).commit();
                        getSupportFragmentManager().beginTransaction().hide(active).show(frag3).commit();
                        active = frag3;
                        return true;
                    case 3:
                        bottomNavBadgeRef = mFirebaseDatabase.getReference();
                        bottomNavBadgeMap = new HashMap<String, Object>();
                        notificationBadgeModel = new NotificationBadgeModel(false, 0);
                        bottomNavBadgeMap.put("Notification Badges/Teachers/" + mFirebaseUser.getUid() + "/Notifications", notificationBadgeModel);
                        bottomNavBadgeRef.updateChildren(bottomNavBadgeMap);
                        bottomNavigation.setNotification("", 3);
//                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragmentholder, frag4).commit();
                        getSupportFragmentManager().beginTransaction().hide(active).show(frag4).commit();
                        active = frag4;
                        return true;
                    case 4:
                        bottomNavBadgeRef = mFirebaseDatabase.getReference();
                        bottomNavBadgeMap = new HashMap<String, Object>();
                        notificationBadgeModel = new NotificationBadgeModel(false, 0);
                        bottomNavBadgeMap.put("Notification Badges/Teachers/" + mFirebaseUser.getUid() + "/More", notificationBadgeModel);
                        bottomNavBadgeRef.updateChildren(bottomNavBadgeMap);
                        bottomNavigation.setNotification("", 4);
//                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragmentholder, frag5).commit();
                        getSupportFragmentManager().beginTransaction().hide(active).show(frag5).commit();
                        active = frag5;
                        return true;
                }
                return false;
            }
        });

        onBoardingSearchBalloon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBoardingSearchBalloon.setVisibility(View.GONE);
                startActivity(new Intent(TeacherMainActivityTwo.this, SearchActivity.class));
            }
        });

        tutorialBalloon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animatePressed(tutorialBalloon);
                startActivity(new Intent(TeacherMainActivityTwo.this, TutorialsActivity.class));
            }
        });

        dismissTutorialBalloon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateGone(tutorialBalloon);
                tutorialBalloon.setVisibility(View.GONE);
            }
        });

        authIDTokenListener = new FirebaseAuth.IdTokenListener() {
            @Override
            public void onIdTokenChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (auth.getCurrentUser() == null) {
                    LogoutProtocol.goToIntro(context);
                }
            }
        };
    }

    public AHBottomNavigation getData(){
        return bottomNavigation;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_request) {
            startActivity(new Intent(TeacherMainActivityTwo.this, TeacherRequestActivity.class));
            return true;
        } else if (id == R.id.action_search){
            if (onBoardingSearchBalloon.getVisibility() == View.VISIBLE) {
                animateGone(onBoardingSearchBalloon);
            }
            startActivity(new Intent(TeacherMainActivityTwo.this, SearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadBadgesFromFirebase() {
        mDatabaseReference = mFirebaseDatabase.getReference().child("Notification Badges").child("General").child(mFirebaseUser.getUid()).child("Inbox");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    NotificationBadgeModel notificationBadgeModel = dataSnapshot.getValue(NotificationBadgeModel.class);
                    if (notificationBadgeModel.getStatus()){
                        if (sharedPreferencesManager.getActiveAccount().equals("Teacher")) {
                            CustomToast.blueBackgroundToast(getBaseContext(), "New message in your inbox.");
                        }
                        AHNotification notification = new AHNotification.Builder()
                                .setText(" ")
                                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                                .setTextColor(ContextCompat.getColor(context, R.color.white))
                                .build();
                        bottomNavigation.setNotification(notification, 2);
                    } else {
                        bottomNavigation.setNotification("", 2);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseReference = mFirebaseDatabase.getReference().child("Notification Badges").child("Teachers").child(mFirebaseUser.getUid()).child("Notifications");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    NotificationBadgeModel notificationBadgeModel = dataSnapshot.getValue(NotificationBadgeModel.class);
                    if (notificationBadgeModel.getStatus()){
                        if (sharedPreferencesManager.getActiveAccount().equals("Teacher")) {
                            CustomToast.blueBackgroundToast(getBaseContext(), "New notification.");
                        }
                        AHNotification notification = new AHNotification.Builder()
                                .setText(" ")
                                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                                .setTextColor(ContextCompat.getColor(context, R.color.white))
                                .build();
                        bottomNavigation.setNotification(notification, 3);
                    } else {
                        bottomNavigation.setNotification("", 3);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseReference = mFirebaseDatabase.getReference().child("Notification Badges").child("Teachers").child(mFirebaseUser.getUid()).child("More");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    NotificationBadgeModel notificationBadgeModel = dataSnapshot.getValue(NotificationBadgeModel.class);
                    if (notificationBadgeModel.getStatus()){
                        AHNotification notification = new AHNotification.Builder()
                                .setText(" ")
                                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                                .setTextColor(ContextCompat.getColor(context, R.color.white))
                                .build();
                        bottomNavigation.setNotification(notification, 4);
                    } else {
                        bottomNavigation.setNotification("", 4);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addIdTokenListener(authIDTokenListener);
        Analytics.loginAnalytics(context,  mFirebaseUser.getUid(), "Teacher");
        sessionStartTime = System.currentTimeMillis();
    }

    @Override
    protected void onStop () {
        super.onStop();
        try {
            CustomToast.cancelToast();
            auth.removeIdTokenListener(authIDTokenListener);
        } catch (Exception e) {

        }

        tutorialBalloon.setVisibility(View.GONE);
        onBoardingSearchBalloon.setVisibility(View.GONE);

        sessionDurationInSeconds = String.valueOf((System.currentTimeMillis() - sessionStartTime) / 1000);
        String currentSessionLoginKey = sharedPreferencesManager.getCurrentLoginSessionKey();
        String day_month_year = sharedPreferencesManager.getCurrentLoginSessionDayMonthYear();
        String month_year = sharedPreferencesManager.getCurrentLoginSessionMonthYear();
        String year = sharedPreferencesManager.getCurrentLoginSessionYear();
        HashMap<String, Object> loginUpdateMap = new HashMap<>();
        String mFirebaseUserID = mFirebaseUser.getUid();

        loginUpdateMap.put("Analytics/User Login History/" + mFirebaseUserID + "/" + currentSessionLoginKey + "/sessionDurationInSeconds", sessionDurationInSeconds);
        loginUpdateMap.put("Analytics/User Daily Login History/" + mFirebaseUserID + "/" + day_month_year + "/" + currentSessionLoginKey + "/sessionDurationInSeconds", sessionDurationInSeconds);
        loginUpdateMap.put("Analytics/User Monthly Login History/" + mFirebaseUserID + "/" + month_year + "/" + currentSessionLoginKey + "/sessionDurationInSeconds", sessionDurationInSeconds);
        loginUpdateMap.put("Analytics/User Yearly Login History/" + mFirebaseUserID + "/" + year + "/" + currentSessionLoginKey + "/sessionDurationInSeconds", sessionDurationInSeconds);

        loginUpdateMap.put("Analytics/Login History/" + currentSessionLoginKey + "/sessionDurationInSeconds", sessionDurationInSeconds);
        loginUpdateMap.put("Analytics/Daily Login History/" + day_month_year + "/" + currentSessionLoginKey + "/sessionDurationInSeconds", sessionDurationInSeconds);
        loginUpdateMap.put("Analytics/Monthly Login History/" + month_year + "/" + currentSessionLoginKey + "/sessionDurationInSeconds", sessionDurationInSeconds);
        loginUpdateMap.put("Analytics/Yearly Login History/" + year + "/" + currentSessionLoginKey + "/sessionDurationInSeconds", sessionDurationInSeconds);

        DatabaseReference loginUpdateRef = FirebaseDatabase.getInstance().getReference();
        loginUpdateRef.updateChildren(loginUpdateMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int currentItem = bottomNavigation.getCurrentItem();
        if (currentItem == 0){
//            getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragmentholder, frag1).commit();
            getSupportFragmentManager().beginTransaction().hide(active).show(frag1).commit();
            active = frag1;
        } else if (currentItem == 1){
//            getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragmentholder, frag2).commit();
            getSupportFragmentManager().beginTransaction().hide(active).show(frag2).commit();
            active = frag2;
        } else if (currentItem == 2){
//            getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragmentholder, frag3).commit();
            getSupportFragmentManager().beginTransaction().hide(active).show(frag3).commit();
            active = frag3;
        } else if (currentItem == 3){
//            getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragmentholder, frag4).commit();
            getSupportFragmentManager().beginTransaction().hide(active).show(frag4).commit();
            active = frag4;
        } else if (currentItem == 4){
//            getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragmentholder, frag5).commit();
            getSupportFragmentManager().beginTransaction().hide(active).show(frag5).commit();
            active = frag5;
        }
        UpdateDataFromFirebase.populateEssentials(this);
        ServerDeviceTimeDifference.getDeviceServerTimeDifference(this);
        CheckDeletedState.isDeleted(this);
    }

    //region Generate and send token to FireBase
    private void initFCM(){
        String token = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String deviceID = FirebaseInstanceId.getInstance().getId();
        reference.child("UserRoles").child(mFirebaseUser.getUid()).child("token").setValue(token);
    }
    //endregion

    //region onBoarding flow
    private void onBoardFirebaseCheck() {
        mDatabaseReference = mFirebaseDatabase.getReference().child("Analytics").child("User Login History").child(mFirebaseUser.getUid());
        mDatabaseReference.orderByChild("accountType_platform").equalTo("Teacher_Android").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int numberOfAndroidLogins = (int) dataSnapshot.getChildrenCount();
                    if (numberOfAndroidLogins == 1)
                        onBoardDialog();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void onBoardDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_onboarding_dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        viewPager = (ViewPager) dialog.findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) dialog.findViewById(R.id.dots);
        next = (Button) dialog.findViewById(R.id.next);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        try {
            dialog.show();
        } catch (Exception e) {
            return;
        }

        layouts = new int[]{
                R.layout.fragment_welcome_slide_one,
                R.layout.fragment_welcome_slide_one};

//      adding bottom dots
        addBottomDots(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        next.setText("Find your school to get started");
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(25);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(5, 0, 5, 0); //(left, top, right, bottom);
            dots[i].setLayoutParams(llp);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[currentPage].setTextColor(getResources().getColor(R.color.black));
        }
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(final int position) {
            addBottomDots(position);
            if (position == 0) {
                next.setText("Find your school to get started");
                next.setVisibility(View.VISIBLE);
            } else if (position == layouts.length - 1) {
                next.setText("See tutorials");
                next.setVisibility(View.VISIBLE);
            }

            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position == 0) {
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                    }
                }
            });
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    //View pager adapter
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
    //endregion

    //region Show search balloon
    private void onBoardingSearchBalloonCheck() {
        Gson gson = new Gson();
        ArrayList<Class> classList = new ArrayList<>();
        String myClassesJSON = sharedPreferencesManager.getMyClasses();
        Type type = new TypeToken<ArrayList<Class>>() {}.getType();
        classList = gson.fromJson(myClassesJSON, type);

        if (classList == null && tutorialBalloon.getVisibility() == View.GONE) {
            onBoardingSearchBalloon.setVisibility(View.VISIBLE);
            animateVisible(onBoardingSearchBalloon);
        } else {
            onBoardingSearchBalloon.setVisibility(View.GONE);
        }
    }
    //endregion

    //region Show tutorial balloon
    private void tutorialFirebaseCheck() {
        mDatabaseReference = mFirebaseDatabase.getReference().child("Analytics").child("User Login History").child(mFirebaseUser.getUid());
        mDatabaseReference.orderByChild("accountType_platform").equalTo("Parent_Android").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final int numberOfAndroidLogins = (int) dataSnapshot.getChildrenCount();

                    mDatabaseReference = mFirebaseDatabase.getReference().child("Analytics").child("Feature Use Analytics User").child(mFirebaseUser.getUid()).child("Tutorials");
                    mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
//                                if (numberOfAndroidLogins == 3 || ((numberOfAndroidLogins != 0) && (numberOfAndroidLogins % 5) == 0)) {
                                    tutorialBalloon.setVisibility(View.VISIBLE);
                                    animateVisible(tutorialBalloon);
//                                    tutorialBalloon.setVisibility(View.VISIBLE);
                                    onBoardingSearchBalloon.setVisibility(View.GONE);
//                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    //endregion

    //region Remote Campaign
    private void remoteCampaign() {
        mDatabaseReference = mFirebaseDatabase.getReference().child("Campaigns").child("User Campaigns").child(mFirebaseUser.getUid());
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String campaignKey = "";

                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        String key = postSnapshot.getKey();
                        Boolean campaignStatus = postSnapshot.getValue(Boolean.class);

                        if (!campaignStatus) {
                            campaignKey = key;
                        }
                    }

                    if (!campaignKey.equals("")) {
                        mDatabaseReference = mFirebaseDatabase.getReference().child("Campaigns").child("Campaigns").child(campaignKey);
                        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    RemoteCampaign remoteCampaign = dataSnapshot.getValue(RemoteCampaign.class);
                                    remoteCampaign.setCampaignID(dataSnapshot.getKey());
                                    if (remoteCampaign.getCampaignTarget().equals("Teacher")
                                            || remoteCampaign.getCampaignTarget().equals("All")
                                            || remoteCampaign.getCampaignTarget().equals("Parent&Teacher")
                                            || remoteCampaign.getCampaignTarget().equals("Teacher&School")) {

                                        Intent I = new Intent(TeacherMainActivityTwo.this, RemoteCampaignActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("ID", remoteCampaign.getCampaignID());
                                        bundle.putString("URL", remoteCampaign.getCampaignURL());
                                        bundle.putString("BackgroundURL", remoteCampaign.getCampaignBackgroundURL());
                                        bundle.putString("IconURL", remoteCampaign.getCampaignIconURL());
                                        bundle.putString("Title", remoteCampaign.getCampaignTitle());
                                        bundle.putString("Text", remoteCampaign.getCampaignText());
                                        I.putExtras(bundle);
                                        startActivity(I);
                                    }
//                                    else {
//                                        DatabaseReference remoteCampaignUpdate = mFirebaseDatabase.getReference();
//                                        Map<String, Object> remoteCampaignMap = new HashMap<String, Object>();
//                                        remoteCampaignMap.put("Campaigns/User Campaigns/" + mFirebaseUser.getUid() + "/" + remoteCampaign.getCampaignID(), true);
//                                        remoteCampaignMap.put("Campaigns/Campaign Recipients/" + remoteCampaign.getCampaignID() + "/" + mFirebaseUser.getUid(), true);
//                                        remoteCampaignUpdate.updateChildren(remoteCampaignMap);
//                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //endregion

    //region Animations
    public void animateVisible(final LinearLayout view) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        prepareAnimation(scaleAnimation);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        prepareAnimation(alphaAnimation);

        AnimationSet animation = new AnimationSet(true);
        animation.addAnimation(alphaAnimation);
        animation.addAnimation(scaleAnimation);
        animation.setDuration(500);
        animation.setFillAfter(false);

        view.startAnimation(animation);
    }

    public void animateGone(final LinearLayout view) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        prepareAnimation(scaleAnimation);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        prepareAnimation(alphaAnimation);

        AnimationSet animation = new AnimationSet(true);
        animation.addAnimation(alphaAnimation);
        animation.addAnimation(scaleAnimation);
        animation.setDuration(300);
        animation.setFillAfter(false);

        view.startAnimation(animation);
    }

    public void animatePressed(final LinearLayout view) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        prepareAnimation(scaleAnimation);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 1.0f);
        prepareAnimation(alphaAnimation);

        AnimationSet animation = new AnimationSet(true);
        animation.addAnimation(alphaAnimation);
        animation.addAnimation(scaleAnimation);
        animation.setDuration(300);
        animation.setFillAfter(false);

        view.startAnimation(animation);
    }

    private Animation prepareAnimation(Animation animation){
        animation.setRepeatCount(0);
        return animation;
    }
    //endregion
}
