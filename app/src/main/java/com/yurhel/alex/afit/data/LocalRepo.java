package com.yurhel.alex.afit.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.yurhel.alex.afit.R;
import com.yurhel.alex.afit.ui.screen_settings.Hidden;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LocalRepo extends SQLiteOpenHelper {

    private static LocalRepo uniqueInstance;
    public static LocalRepo getInstance(@Nullable Context context) {
        if (uniqueInstance == null) {
            uniqueInstance = new LocalRepo(context);
        }
        return uniqueInstance;
    }
    private LocalRepo(@Nullable Context context) {
        super(context, "afit.db", null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE exercises ("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "name TEXT, "+
                "rest INT, "+
                "reps INT, "+
                "sets INT, "+
                "start TEXT, "+
                "ended TEXT, "+
                "weight REAL, "+
                "color INT, "+
                "isWeightShow INT)"
        );
        db.execSQL("CREATE TABLE stats ("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "name TEXT, "+
                "start TEXT, "+
                "ended TEXT, "+
                "color INT)"
        );
    }

    private String strCreateExEntryTable(int id) {
        return "CREATE TABLE exercise_"+id+" ("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "resultShort INT, "+
                "resultLong TEXT, "+
                "time TEXT, "+
                "date TEXT, "+
                "weights TEXT)";
    }
    private String strCreateStEntryTable(int id) {
        return "CREATE TABLE stats_"+id+" ("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "value REAL, "+
                "date TEXT, "+
                "note TEXT)";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldDB, int newDB) {
        if (newDB == 4 && oldDB < 4) {
            db.execSQL("ALTER TABLE exercises ADD COLUMN isWeightShow INT DEFAULT 0");
        }
    }

    SQLiteDatabase db = this.getWritableDatabase();
    public void closeDB() {
        db.close();
    }


    // EDIT ACTIVITY. WORKOUT WITH WEIGHT
    public void setWeights(int id, Boolean isWeightShow) {
        if (!db.isOpen()) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("isWeightShow", (isWeightShow) ? 1 : 0);
        db.update("exercises", cv, "id = '"+id+"'", null);
    }

    public boolean getIsWeightShowForMainStat(int id) {
        boolean l = false;
        if (!db.isOpen()) db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT isWeightShow FROM exercises WHERE id = "+id, null);
        if (c.moveToFirst()) l = c.getInt(0) == 1;
        c.close();
        return l;
    }


    // GET OBJs
    public ArrayList<Obj> getAllExercises() {
        ArrayList<Obj> l = new ArrayList<>();
        if (!db.isOpen()) db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM exercises", null);
        if (c.moveToFirst()) {
            do {
                Cursor c1 = db.rawQuery("SELECT * FROM exercise_"+c.getInt(0), null);
                if (c1.moveToFirst()) {
                    do {
                        Obj o = createObj(c1, Obj.exerciseEntry);
                        o.name = c.getString(1);
                        o.color = c.getInt(8);
                        l.add(o);
                    } while (c1.moveToNext());
                }
                c1.close();
            } while (c.moveToNext());
        }
        c.close();
        return l;
    }

    public ArrayList<Obj> getFilteredAllData(long minTime, long maxTime) {
        ArrayList<Obj> l = new ArrayList<>();
        if (!db.isOpen()) db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM exercises", null);
        if (c.moveToFirst()) {
            do {
                Cursor c1 = db.rawQuery(
                        "SELECT * FROM exercise_"+c.getInt(0)+" WHERE date BETWEEN "+minTime+" AND "+maxTime,
                        null
                );
                if (c1.moveToFirst()) {
                    do {
                        Obj o = createObj(c1, Obj.exerciseEntry);
                        o.name = c.getString(1);
                        o.color = c.getInt(8);
                        l.add(o);
                    } while (c1.moveToNext());
                }
                c1.close();
            } while (c.moveToNext());
        }
        c.close();
        c = db.rawQuery("SELECT * FROM stats", null);
        if (c.moveToFirst()) {
            do {
                Cursor c1 = db.rawQuery(
                        "SELECT * FROM stats_"+c.getInt(0)+" WHERE date BETWEEN "+minTime+" AND "+maxTime,
                        null
                );
                if (c1.moveToFirst()) {
                    do {
                        Obj o = createObj(c1, Obj.statsEntry);
                        o.name = c.getString(1);
                        o.color = c.getInt(4);
                        l.add(o);
                    } while (c1.moveToNext());
                }
                c1.close();
            } while (c.moveToNext());
        }
        c.close();
        return l;
    }

    public ArrayList<Obj> getTableEntries(
            Integer tableId,
            boolean isExercise,
            long minTime,
            long maxTime
    ) {
        ArrayList<Obj> list = new ArrayList<>();
        if (!db.isOpen()) db = this.getWritableDatabase();
        String sqlStr;
        if (minTime == 0 && maxTime == 0) {
            // Get data without boundaries
            sqlStr = "SELECT * FROM "+((isExercise) ? "exercise_" : "stats_")+tableId;
        } else if (minTime == 0) {
            // Get data before max
            sqlStr = "SELECT * FROM "+((isExercise) ? "exercise_": "stats_")+tableId+" WHERE date <= "+maxTime;
        } else if (maxTime == 0) {
            // Get data after min
            sqlStr = "SELECT * FROM "+((isExercise) ? "exercise_": "stats_")+tableId+" WHERE date >= "+minTime;
        } else {
            // Get data between min & max
            sqlStr = "SELECT * FROM "+((isExercise) ? "exercise_": "stats_")+tableId+" WHERE date BETWEEN "+minTime+" AND "+maxTime;
        }
        Cursor cursor = db.rawQuery(sqlStr, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(createObj(cursor, (isExercise) ? Obj.exerciseEntry : Obj.statsEntry));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public ArrayList<Obj> getMainTableEntries(boolean isExercise, boolean notShowHidden) {
        ArrayList<Obj> list = new ArrayList<>();
        if (!db.isOpen()) db = this.getWritableDatabase();
        ArrayList<Hidden> hidden = getHidden();
        String sqlStr = "SELECT * FROM "+((isExercise) ? "exercises": "stats");
        Cursor c = db.rawQuery(sqlStr, null);
        if (c.moveToFirst()) {
            do {
                boolean objIsHidden = hidden.stream()
                        .filter(it -> it.getStatsId() == c.getInt(0) && it.isExercise() == isExercise)
                        .toArray().length > 0;
                if (!(notShowHidden && objIsHidden)) {
                    list.add(createObj(c, (isExercise) ? Obj.exerciseMain : Obj.statsMain));
                }
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public Obj getOneMainObj(int id, boolean isExercise) {
        if (!db.isOpen()) db = this.getWritableDatabase();
        Obj obj;
        Cursor cursor;
        if (isExercise) {
            cursor = db.rawQuery("SELECT * FROM exercises WHERE id = "+id, null);
            cursor.moveToFirst();
            obj = createObj(cursor, Obj.exerciseMain);
        } else {
            cursor = db.rawQuery("SELECT * FROM stats WHERE id = "+id, null);
            cursor.moveToFirst();
            obj = createObj(cursor, Obj.statsMain);
        }
        cursor.close();
        return obj;
    }

    // ADD OBJs
    public void addStats(String name, int color) {
        if (!db.isOpen()) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("start", "");
        cv.put("ended", "");
        cv.put("color", color);
        db.insert("stats", null, cv);
        // Take ID from new created row
        int id = 0;
        Cursor cursor = db.rawQuery("SELECT * FROM stats", null);
        if (cursor.moveToLast())
            id = cursor.getInt(0);
        cursor.close();
        // Create table with user custom statistic
        db.execSQL(strCreateStEntryTable(id));
    }

    public void addStatsEntry(int statsNamesId, Double value, String date, String note) {
        if (!db.isOpen()) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("value", value);
        cv.put("date", date);
        cv.put("note", note);
        db.insert("stats_"+statsNamesId, null, cv);
    }

    public void addExercise(String name, int rest, int sets, double weight, int color) {
        if (!db.isOpen()) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("rest", rest);
        cv.put("reps", 5);
        cv.put("sets", sets);
        cv.put("weight", weight);
        cv.put("start", "");
        cv.put("ended", "");
        cv.put("color", color);
        db.insert("exercises", null, cv);
        // Take ID from new created row
        int id = 0;
        Cursor cursor = db.rawQuery("SELECT * FROM exercises", null);
        if (cursor.moveToLast())
            id = cursor.getInt(0);
        cursor.close();
        // Create table with exercise statistics
        db.execSQL(strCreateExEntryTable(id));
    }

    public void addExerciseEntry(int exId, int result_s, String result_l, String time, String date, String weights) {
        if (!db.isOpen()) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("resultShort", result_s);
        cv.put("resultLong", result_l);
        cv.put("time", time);
        cv.put("date", date);
        cv.put("weights", weights);
        db.insert("exercise_"+exId, null, cv);
        // Clear scores (so they are recalculated when accessed)
        clearScores();
    }


    // DEL OBJs
    public void deleteObj(int id, boolean isExercise) {
        if (!db.isOpen()) db = this.getWritableDatabase();
        if (isExercise) {
            db.execSQL("DELETE FROM exercises WHERE id = "+id);
            db.execSQL("DROP TABLE exercise_"+id);
        } else {
            db.execSQL("DELETE FROM stats WHERE id = "+id);
            db.execSQL("DROP TABLE stats_"+id);
        }
        // Clear scores (so they are recalculated when accessed)
        clearScores();
    }

    public void deleteSmallObj(int tableId, int entryId, boolean isExercise) {
        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+((isExercise) ? "exercise_": "stats_")+tableId+" WHERE id = "+entryId);
        // Clear scores (so they are recalculated when accessed)
        clearScores();
    }


    // UPDATE OBJs
    public void updateStats(String name, int id, int color) {
        if (!db.isOpen()) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("color", color);
        db.update("stats", cv, "id = "+id, null);
    }

    public void updateStatsEntry(int id, int statsNamesId, Double value, String note) {
        if (!db.isOpen()) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("value", value);
        cv.put("note", note);
        db.update("stats_"+statsNamesId, cv, "id = "+id, null);
    }

    public void updateExercise(String name, int id, int rest, int reps, int sets, double weight, int color) {
        if (!db.isOpen()) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("rest", rest);
        cv.put("reps", reps);
        cv.put("sets", sets);
        cv.put("weight", weight);
        cv.put("color", color);
        db.update("exercises", cv, "id = "+id, null);
    }

    public void setDate(String date, int id, boolean isExercise, boolean start) {
        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL(
                "UPDATE "+((isExercise) ? "exercises": "stats")+" SET "+((start) ? "start": "ended")+" = '"+date+"' WHERE id = "+id
        );
    }


    // EXPORT / IMPORT
    public JSONArray exportDB() {
        try {
            JSONArray jArray = new JSONArray();
            if (!db.isOpen()) db = this.getWritableDatabase();
            for (String table: new String[]{"exercises", "stats"}) {
                Cursor c = db.rawQuery("SELECT * FROM "+table, null);
                if (c.moveToFirst()) {
                    do {
                        int id = c.getInt(0);
                        JSONObject jObj = new JSONObject();
                        if (table.equals("exercises")) {
                            jObj.put("type", "ex");
                            jObj.put("ex_id", id);
                            jObj.put("ex_name", c.getString(1));
                            jObj.put("ex_rest", c.getInt(2));
                            jObj.put("ex_reps", c.getInt(3));
                            jObj.put("ex_sets", c.getInt(4));
                            jObj.put("ex_start", c.getString(5));
                            jObj.put("ex_end", c.getString(6));
                            jObj.put("ex_weight", c.getDouble(7));
                            jObj.put("ex_color", c.getDouble(8));
                            jArray.put(jObj);
                            Cursor c1 = db.rawQuery("SELECT * FROM exercise_"+id, null);
                            if (c1.moveToFirst()) {
                                do {
                                    JSONObject jObj1 = new JSONObject();
                                    jObj1.put("type", "ex_e");
                                    jObj1.put("ex_e_id", c1.getInt(0));
                                    jObj1.put("ex_e_res_s", c1.getInt(1));
                                    jObj1.put("ex_e_res_l", c1.getString(2));
                                    jObj1.put("ex_e_time", c1.getString(3));
                                    jObj1.put("ex_e_date", c1.getString(4));
                                    jObj1.put("ex_e_weights", c1.getString(5));
                                    jObj1.put("parent_id", id);
                                    jArray.put(jObj1);
                                } while (c1.moveToNext());
                            }
                            c1.close();
                        } else if (table.equals("stats")) {
                            jObj.put("type", "st");
                            jObj.put("st_id", id);
                            jObj.put("st_name", c.getString(1));
                            jObj.put("st_start", c.getString(2));
                            jObj.put("st_end", c.getString(3));
                            jObj.put("st_color", c.getString(4));
                            jArray.put(jObj);
                            Cursor c1 = db.rawQuery("SELECT * FROM stats_"+id, null);
                            if (c1.moveToFirst()) {
                                do {
                                    JSONObject jObj1 = new JSONObject();
                                    jObj1.put("type", "st_e");
                                    jObj1.put("st_e_id", c1.getInt(0));
                                    jObj1.put("st_e_value", c1.getDouble(1));
                                    jObj1.put("st_e_date", c1.getString(2));
                                    jObj1.put("st_e_notes", c1.getString(3));
                                    jObj1.put("parent_id", id);
                                    jArray.put(jObj1);
                                } while (c1.moveToNext());
                            }
                            c1.close();
                        }
                    } while (c.moveToNext());
                }
                c.close();
            }
            return jArray;
        }  catch (Exception e) {
            return new JSONArray();
        }
    }

    public boolean importDB(String data, Context context) {
        JSONArray oldData = exportDB();
        try {
            cleanDatabase();
            // Loop data
            JSONArray jsonData = new JSONArray(data);
            for (int i = 0; i < jsonData.length(); i++) {
                JSONObject obj = jsonData.getJSONObject(i);
                if (!db.isOpen()) db = this.getWritableDatabase();
                ContentValues cvDB = new ContentValues();
                String type = obj.getString("type");
                int id;
                // Insert obj to DB
                switch (type) {
                    case "ex":
                        id = obj.getInt("ex_id");
                        cvDB.put("id", id);
                        cvDB.put("name", obj.getString("ex_name"));
                        cvDB.put("rest", obj.getInt("ex_rest"));
                        cvDB.put("reps", obj.getInt("ex_reps"));
                        cvDB.put("sets", obj.getInt("ex_sets"));
                        cvDB.put("start", obj.getString("ex_start"));
                        cvDB.put("ended", obj.getString("ex_end"));
                        cvDB.put("weight", obj.getDouble("ex_weight"));
                        try {// Try import color
                            cvDB.put("color", obj.getInt("ex_color"));
                        } catch (Exception e) {
                            cvDB.put("color", context.getColor(R.color.green_main));
                        }
                        db.insert("exercises", null, cvDB);
                        db.execSQL(strCreateExEntryTable(id));
                        break;
                    case "st":
                        id = obj.getInt("st_id");
                        cvDB.put("id", id);
                        cvDB.put("name", obj.getString("st_name"));
                        cvDB.put("start", obj.getString("st_start"));
                        cvDB.put("ended", obj.getString("st_end"));
                        try {// Try import color
                            cvDB.put("color", obj.getInt("st_color"));
                        } catch (Exception e) {
                            cvDB.put("color", context.getColor(R.color.green_main));
                        }
                        db.insert("stats", null, cvDB);
                        db.execSQL(strCreateStEntryTable(id));
                        break;
                    case "ex_e":
                        cvDB.put("id", obj.getInt("ex_e_id"));
                        cvDB.put("resultShort", obj.getInt("ex_e_res_s"));
                        cvDB.put("resultLong", obj.getString("ex_e_res_l"));
                        cvDB.put("time", obj.getString("ex_e_time"));
                        cvDB.put("date", obj.getString("ex_e_date"));
                        cvDB.put("weights", obj.getString("ex_e_weights"));
                        db.insert("exercise_"+obj.getInt("parent_id"), null, cvDB);
                        break;
                    case "st_e":
                        cvDB.put("id", obj.getInt("st_e_id"));
                        cvDB.put("value", obj.getDouble("st_e_value"));
                        cvDB.put("date", obj.getString("st_e_date"));
                        try {// Try import note
                            cvDB.put("note", obj.getString("st_e_notes"));
                        } catch (Exception ignore) {
                            cvDB.put("note", "");
                        }
                        db.insert("stats_"+ obj.getInt("parent_id"), null, cvDB);
                        break;
                }
            }
            return true;
        } catch (Exception e) {
            cleanDatabase();
            importDB(oldData.toString(), context);
            return false;
        }
    }

    public void cleanDatabase() {
        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL("DELETE FROM exercises");
        db.execSQL("DELETE FROM stats");
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (cursor.moveToFirst()) {
            do {
                String tableName = cursor.getString(0);
                // Clear positions
                if (tableName.equals("positions")) db.execSQL("DELETE FROM positions");
                // Clear scores
                if (tableName.equals("scores")) db.execSQL("DELETE FROM scores");
                // Clear entries tables
                for (String name : new String[]{"stats_", "exercise_"}) {
                    if (tableName.contains(name))
                        db.execSQL("DROP TABLE " + tableName);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }


    // SCORES
    private String getScoresTableScheme() {
        return "CREATE TABLE IF NOT EXISTS scores (id INTEGER, allPoints TEXT, weekPoints TEXT, weekDate TEXT)";
    }

    public void setScores(ScoresObj scores) {
        ContentValues values = new ContentValues();
        values.put("allPoints", scores.getAllPoints());
        values.put("weekPoints", scores.getWeekPoints());
        values.put("weekDate", scores.getWeekDate());

        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL(getScoresTableScheme());
        int result = db.update("scores", values, "id = 1", null);
        if (result == 0) {
            values.put("id", 1);
            db.insert("scores", null, values);
        }
    }

    public ScoresObj getScores() {
        ScoresObj scores = null;
        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL(getScoresTableScheme());
        Cursor cursor = db.rawQuery("SELECT * FROM scores", null);
        if (cursor.moveToFirst()) {
            scores = new ScoresObj(cursor.getInt(1), cursor.getInt(2), cursor.getLong(3));
        }
        cursor.close();
        return scores;
    }

    public void clearScores() {
        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL(getScoresTableScheme());
        db.execSQL("DELETE FROM scores");
    }


    // DEVICE
    private String getDeviceTableScheme() {
        return "CREATE TABLE IF NOT EXISTS device (id INTEGER, cardViewType TEXT)";
    }

    /**
     * Set 'col' or 'grid'
     * */
    public void setDevice(String cardViewType) {
        ContentValues values = new ContentValues();
        values.put("cardViewType", cardViewType);

        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL(getDeviceTableScheme());
        int result = db.update("device", values, "id = 1", null);
        if (result == 0) {
            values.put("id", 1);
            db.insert("device", null, values);
        }
    }

    /**
     * Get 'col' or 'grid'
     * */
    public String getDevice() {
        String cardViewType = "col";
        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL(getDeviceTableScheme());
        Cursor cursor = db.rawQuery("SELECT * FROM device", null);
        if (cursor.moveToFirst()) {
            cardViewType = cursor.getString(1);
        }
        cursor.close();
        return cardViewType;
    }


    // SAVED WORKOUT
    private String getSavedWorkoutTableScheme() {
        return "CREATE TABLE IF NOT EXISTS savedWorkout ("+
                "id INTEGER, "+
                "stage TEXT, "+
                "msg TEXT, "+
                "restTime INT, "+
                "exId INT, "+
                "repsList TEXT, "+
                "weightsList TEXT, "+
                "startTime INTEGER)";
    }

    public void setSavedWorkout(SavedWorkout workout) {
        ContentValues values = new ContentValues();
        values.put("stage", workout.getStage());
        values.put("msg", workout.getMsg());
        values.put("restTime", workout.getRestTime());
        values.put("exId", workout.getExId());
        values.put("repsList", workout.getRepsList());
        values.put("weightsList", workout.getWeightsList());
        values.put("startTime", workout.getStartTime());

        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL(getSavedWorkoutTableScheme());
        int result = db.update("savedWorkout", values, "id = 1", null);
        if (result == 0) {
            values.put("id", 1);
            db.insert("savedWorkout", null, values);
        }
    }

    public SavedWorkout getSavedWorkout() {
        SavedWorkout obj = null;
        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL(getSavedWorkoutTableScheme());
        Cursor cursor = db.rawQuery("SELECT * FROM savedWorkout", null);
        if (cursor.moveToFirst()) {
            obj = new SavedWorkout(
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getLong(7)
            );
        }
        cursor.close();
        return obj;
    }

    public void clearSavedWorkout() {
        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL(getSavedWorkoutTableScheme());
        db.execSQL("DELETE FROM savedWorkout");
    }


    // HIDDEN
    private String getHiddenScheme() {
        return "CREATE TABLE IF NOT EXISTS hidden ("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "statsId INTEGER, "+
                "isExercise INTEGER)";
    }

    public void addHidden(Hidden obj) {
        ContentValues values = new ContentValues();
        values.put("statsId", obj.getStatsId());
        values.put("isExercise", (obj.isExercise()) ? 1 : 0);
        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL(getHiddenScheme());
        db.insert("hidden", null, values);
    }

    public void removeHidden(Hidden obj) {
        int isExercise = (obj.isExercise()) ? 1 : 0;
        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL(getHiddenScheme());
        db.delete("hidden", "statsId = " + obj.getStatsId() + " AND isExercise = " + isExercise, null);
    }

    public ArrayList<Hidden> getHidden() {
        ArrayList<Hidden> list = new ArrayList<>();
        if (!db.isOpen()) db = this.getWritableDatabase();
        db.execSQL(getHiddenScheme());
        Cursor c = db.rawQuery("SELECT * FROM hidden", null);
        if (c.moveToFirst()) {
            do {
                list.add(
                        new Hidden(c.getInt(1), c.getInt(2) == 1)
                );
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }


    // UTILS
    private Obj createObj(Cursor c, String objType) {
        Obj obj;
        switch (objType) {
            case Obj.exerciseEntry:
                obj = new Obj(
                        c.getInt(0), true, c.getInt(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5)
                );
                break;
            case Obj.exerciseMain:
                obj = new Obj(
                        c.getInt(0), true, c.getString(1), c.getInt(2),
                        c.getInt(3), c.getInt(4), c.getString(5), c.getString(6),
                        c.getDouble(7), c.getInt(8)
                );
                break;
            case Obj.statsEntry:
                obj = new Obj(
                        c.getInt(0), false, c.getDouble(1), c.getString(2),
                        c.getString(3)
                );
                break;
            default: // Obj.statsMain
                obj = new Obj(
                        c.getInt(0), false, c.getString(1), c.getString(2),
                        c.getString(3), c.getInt(4)
                );
                break;
        }
        return obj;
    }
}