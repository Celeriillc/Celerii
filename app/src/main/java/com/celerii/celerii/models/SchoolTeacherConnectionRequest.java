package com.celerii.celerii.models;

/**
 * Created by DELL on 4/6/2019.
 */

public class SchoolTeacherConnectionRequest {
    String status, timeSent, sorttableTimeSent, teacher, school;

    public SchoolTeacherConnectionRequest() {
        this.status = "";
        this.timeSent = "";
        this.sorttableTimeSent = "";
        this.teacher = "";
        this.school = "";
    }

    public SchoolTeacherConnectionRequest(String status, String timeSent, String sorttableTimeSent, String teacher, String school) {
        this.status = status;
        this.timeSent = timeSent;
        this.sorttableTimeSent = sorttableTimeSent;
        this.teacher = teacher;
        this.school = school;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(String timeSent) {
        this.timeSent = timeSent;
    }

    public String getSorttableTimeSent() {
        return sorttableTimeSent;
    }

    public void setSorttableTimeSent(String sorttableTimeSent) {
        this.sorttableTimeSent = sorttableTimeSent;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }
}
