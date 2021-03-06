package com.celerii.celerii.models;

/**
 * Created by DELL on 3/16/2018.
 */

public class ParentAttendanceHeader {
    String studentID, classID, term, year, subject;

    public ParentAttendanceHeader() {
    }

    public ParentAttendanceHeader(String term, String year, String subject) {
        this.term = term;
        this.year = year;
        this.subject = subject;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
