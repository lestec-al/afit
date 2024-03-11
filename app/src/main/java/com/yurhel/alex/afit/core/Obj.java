package com.yurhel.alex.afit.core;

public class Obj {
    public int id;

    public String name;
    public String start;
    public String end;
    public int color;
    public int rest;
    public int reps;
    public int sets;
    public double weight;

    public long date;
    public double mainValue;
    public String longerValue;
    public String time;
    public String allWeights;

    public int entriesQuantity = -1;
    public int parentId = -1;

    /**
     * Exercise main object
     */
    public Obj(
            int id,
            String name,
            int rest,
            int reps,
            int sets,
            String start,
            String end,
            Double weight,
            int color
    ) {
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
    public Obj(
            int id,
            String name,
            String start,
            String end,
            int color
    ) {
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.color = color;
    }

    /**
     * Exercise entry object
     */
    public Obj(
            int id,
            double mainValue,
            String longerValue,
            String time,
            String date,
            String allWeights
    ) {
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
    public Obj(
            int id,
            double mainValue,
            String date,
            String longerValue
    ) {
        this.id = id;
        this.date = Long.parseLong(date);
        this.mainValue = mainValue;
        this.longerValue = longerValue;
    }
}
