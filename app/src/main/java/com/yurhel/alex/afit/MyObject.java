package com.yurhel.alex.afit;

import androidx.annotation.NonNull;

public class MyObject {
    int id;
    int rest;
    int reps;
    int sets;
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
    String notes;
    int color;

    public MyObject(int id, String name, int rest, int reps, int sets, String start, String end, Double weight, int color) {
        // Exercise
        this.id = id;
        this.name = name;
        this.rest = rest;
        this.reps = reps;
        this.sets = sets;
        this.start = start;
        this.end = end;
        this.weight = weight;
        this.color = color;
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

    public MyObject(int id, String name, String start, String end, int color) {
        // Stats
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.color = color;
    }

    public MyObject(int id, double value, String date, String notes) {
        // Stats entry
        this.id = id;
        this.value = value;
        this.date = Long.parseLong(date);
        this.notes = notes;
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
