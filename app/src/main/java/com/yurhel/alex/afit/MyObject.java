package com.yurhel.alex.afit;

public class MyObject {
    int id;

    String name;
    String start;
    String end;
    int color;
    int rest;
    int reps;
    int sets;
    double weight;

    long date;
    double mainValue;
    String longerValue;
    String time;
    String allWeights;

    /**
     * Exercise main object
     */
    public MyObject(int id, String name, int rest, int reps, int sets, String start, String end, Double weight, int color) {
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.color = color;

        this.rest = rest;
        this.reps = reps;
        this.sets = sets;
        this.weight = weight;
    }

    /**
     * Stats main object
     */
    public MyObject(int id, String name, String start, String end, int color) {
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.color = color;
    }

    /**
     * Exercise entry object
     */
    public MyObject(int id, double mainValue, String longerValue, String time, String date, String allWeights) {
        this.id = id;
        this.date = Long.parseLong(date);
        this.mainValue = mainValue;
        this.longerValue = longerValue;

        this.time = time;
        this.allWeights = allWeights;
    }

    /**
     * Stats entry object
     */
    public MyObject(int id, double mainValue, String date, String longerValue) {
        this.id = id;
        this.date = Long.parseLong(date);
        this.mainValue = mainValue;
        this.longerValue = longerValue;
    }
}
