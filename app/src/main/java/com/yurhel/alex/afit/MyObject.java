package com.yurhel.alex.afit;

import androidx.annotation.NonNull;

public class MyObject {
    int id;
    int seconds;
    int first;
    int reps;
    double value;
    double weight;
    String weights;
    String name;
    String result_s;
    String result_l;
    String time;
    long date;
    String start;
    String end;

    public MyObject(int id, String name, int seconds, int first, int reps, String start, String end, Double weight) {
        // Exercise obj
        this.id = id;
        this.name = name;
        this.seconds = seconds;
        this.first = first;
        this.reps = reps;
        this.start = start;
        this.end = end;
        this.weight = weight;
    }

    public MyObject(int id, String result_s, String result_l, String time, String date, String weights) {
        // Exercise entry
        this.id = id;
        this.result_s = result_s;
        this.result_l = result_l;
        this.time = time;
        this.date = Long.parseLong(date);
        this.weights = weights;
    }

    public MyObject(int id, String name, String start, String end) {
        // Stats obj
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
    }

    public MyObject(int id, double value, String date) {
        // Stats entry
        this.id = id;
        this.value = value;
        this.date = Long.parseLong(date);
    }

    @NonNull
    @Override
    public String toString() {
        try {
            return name;
        } catch (Exception e) {
            return String.valueOf(id);
        }

    }
}
