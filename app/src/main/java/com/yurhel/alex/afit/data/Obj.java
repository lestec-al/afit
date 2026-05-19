package com.yurhel.alex.afit.data;

public class Obj {
    public static final String exerciseMain = "Exercise main";
    public static final String statsMain = "Stats main";
    public static final String exerciseEntry = "Exercise entry";
    public static final String statsEntry = "Stats entry";

    public int id;
    public boolean isExercise;

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

    /**
     * Exercise main object
     */
    public Obj(
            int id,
            boolean isExercise,
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
        this.isExercise = isExercise;
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
            boolean isExercise,
            String name,
            String start,
            String end,
            int color
    ) {
        this.id = id;
        this.isExercise = isExercise;
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
            boolean isExercise,
            double mainValue,
            String longerValue,
            String time,
            String date,
            String allWeights
    ) {
        this.id = id;
        this.isExercise = isExercise;
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
            boolean isExercise,
            double mainValue,
            String date,
            String longerValue
    ) {
        this.id = id;
        this.isExercise = isExercise;
        this.date = Long.parseLong(date);
        this.mainValue = mainValue;
        this.longerValue = longerValue;
    }
}