package com.celerii.celerii.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.celerii.celerii.R;
import com.celerii.celerii.helperClasses.Date;
import com.celerii.celerii.helperClasses.SharedPreferencesManager;
import com.celerii.celerii.helperClasses.Term;
import com.celerii.celerii.models.AcademicRecordStudent;
import com.celerii.celerii.models.SubscriptionModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by DELL on 9/25/2018.
 */

public class AcademicRecordDetailAdapter extends RecyclerView.Adapter<AcademicRecordDetailAdapter.MyViewHolder> {

    private List<AcademicRecordStudent> AcademicRecordStudentList;
    SharedPreferencesManager sharedPreferencesManager;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView examType, score, className, date, maxObtainable, percentageOfTotal, term, year, newBadge;
        public View clickableView;

        public MyViewHolder(final View view) {
            super(view);
            examType = (TextView) view.findViewById(R.id.examtype);
            score = (TextView) view.findViewById(R.id.score);
            className = (TextView) view.findViewById(R.id.classname);
            date = (TextView) view.findViewById(R.id.date);
            maxObtainable = (TextView) view.findViewById(R.id.maxobtainable);
            percentageOfTotal = (TextView) view.findViewById(R.id.percentageoftotal);
            term = (TextView) view.findViewById(R.id.term);
            year = (TextView) view.findViewById(R.id.year);
            newBadge = (TextView) view.findViewById(R.id.newbadge);
            clickableView = view;
        }
    }

    public AcademicRecordDetailAdapter(List<AcademicRecordStudent> AcademicRecordStudentList, Context context) {
        this.AcademicRecordStudentList = AcademicRecordStudentList;
        sharedPreferencesManager = new SharedPreferencesManager(context);
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.academic_record_detail, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        AcademicRecordStudent academicRecordStudent = AcademicRecordStudentList.get(position);

        if (sharedPreferencesManager.getActiveAccount().equals("Parent")) {
            if (academicRecordStudent.isNew()) {
                holder.newBadge.setVisibility(View.VISIBLE);
            } else {
                holder.newBadge.setVisibility(View.GONE);
            }
        }

        holder.examType.setText(academicRecordStudent.getTestType());
        holder.score.setText(academicRecordStudent.getScore());
        holder.className.setText(academicRecordStudent.getClassName());
        holder.date.setText(Date.DateFormatMMDDYYYY(academicRecordStudent.getDate()));
        holder.maxObtainable.setText(academicRecordStudent.getMaxObtainable());
        holder.percentageOfTotal.setText(academicRecordStudent.getPercentageOfTotal() + "%");
        holder.term.setText(Term.Term(academicRecordStudent.getTerm()));
        holder.year.setText(academicRecordStudent.getAcademicYear());

        Boolean isOpenToAll = sharedPreferencesManager.getIsOpenToAll();
        Gson gson = new Gson();
        String subscriptionModelJSON = sharedPreferencesManager.getSubscriptionInformationTeachers();
        Type type = new TypeToken<HashMap<String, SubscriptionModel>>() {}.getType();
        HashMap<String, SubscriptionModel> subscriptionModelMap = gson.fromJson(subscriptionModelJSON, type);
        SubscriptionModel subscriptionModel = new SubscriptionModel();
        if (subscriptionModelMap != null) {
            subscriptionModel = subscriptionModelMap.get(academicRecordStudent.getStudentID());
            if (subscriptionModel == null) {
                subscriptionModel = new SubscriptionModel();
            }
        }
        if (subscriptionModel.getStudentAccount().equals("")) {
            gson = new Gson();
            subscriptionModelJSON = sharedPreferencesManager.getSubscriptionInformationParents();
            type = new TypeToken<HashMap<String, ArrayList<SubscriptionModel>>>() {}.getType();
            HashMap<String, ArrayList<SubscriptionModel>> subscriptionModelMapParent = gson.fromJson(subscriptionModelJSON, type);
            subscriptionModel = new SubscriptionModel();
            if (subscriptionModelMapParent != null) {
                ArrayList<SubscriptionModel> subscriptionModelList = subscriptionModelMapParent.get(academicRecordStudent.getStudentID());
                String latestSubscriptionDate = "0000/00/00 00:00:00:000";
                for (SubscriptionModel subscriptionModel1: subscriptionModelList) {
                    if (Date.compareDates(subscriptionModel1.getExpiryDate(), latestSubscriptionDate)) {
                        subscriptionModel = subscriptionModel1;
                        latestSubscriptionDate = subscriptionModel1.getExpiryDate();
                    }
                }
            }
        }
        Boolean isExpired = Date.compareDates(academicRecordStudent.getDate(), subscriptionModel.getExpiryDate());

        if (isOpenToAll) {
            holder.score.setText(academicRecordStudent.getScore());
        } else {
            if (!isExpired) {
                holder.score.setText(academicRecordStudent.getScore());
            } else {
                holder.score.setText(R.string.not_subscribed_long);
            }
        }
    }

    @Override
    public int getItemCount() {
        return AcademicRecordStudentList.size();
    }
}
