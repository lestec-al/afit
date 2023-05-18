package com.yurhel.alex.afit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DB extends SQLiteOpenHelper {
    Context context;

    public DB(@Nullable Context context) {
        super(context, "afit.db", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE EXERCISES_TABLE (EXERCISE_ID INTEGER PRIMARY KEY AUTOINCREMENT, EXERCISE_NAME TEXT, EXERCISE_SECONDS INT, EXERCISE_FIRST INT, EXERCISE_REPS INT, EXERCISE_START TEXT, EXERCISE_END TEXT, EXERCISE_WEIGHT REAL)"
        );
        sqLiteDatabase.execSQL(
                "CREATE TABLE STATS_NAMES_TABLE (STATS_NAMES_ID INTEGER PRIMARY KEY AUTOINCREMENT, STATS_NAMES_NAME TEXT, STATS_NAMES_START TEXT, STATS_NAMES_END TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

    // STATS
    public LinkedHashMap<MyObject, MyObject> getAll() {
        LinkedHashMap<MyObject, MyObject> l = new LinkedHashMap<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM EXERCISES_TABLE", null);
        if (c.moveToFirst())
            do {
                Cursor c1 = db.rawQuery("SELECT * FROM STATS_EXERCISE_TABLE_"+c.getInt(0), null);
                if (c1.moveToFirst()) {
                    do {
                        l.put(
                                new MyObject(c1.getInt(0), c1.getString(1), c1.getString(2), c1.getString(3), c1.getString(4), c1.getString(5)),
                                new MyObject(c.getInt(0), c.getString(1), c.getInt(2), c.getInt(3), c.getInt(4), c.getString(5), c.getString(6), c.getDouble(7))
                        );
                    } while (c1.moveToNext());
                }
                c1.close();
            } while (c.moveToNext());
        c.close();
        c = db.rawQuery("SELECT * FROM STATS_NAMES_TABLE", null);
        if (c.moveToFirst())
            do {
                Cursor c1 = db.rawQuery("SELECT * FROM STATS_VALUES_TABLE_"+c.getInt(0), null);
                if (c1.moveToFirst()) {
                    do {
                        l.put(
                                new MyObject(c1.getInt(0), c1.getDouble(1), c1.getString(2)),
                                new MyObject(c.getInt(0), c.getString(1), c.getString(2), c.getString(3))
                        );
                    } while (c1.moveToNext());
                }
                c1.close();
            } while (c.moveToNext());
        c.close();
        db.close();
        return l;
    }

    public ArrayList<MyObject> getAllEntries(Integer id, boolean exercise) {
        ArrayList<MyObject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        if (id == null)
            if (exercise)
                cursor = db.rawQuery("SELECT * FROM EXERCISES_TABLE", null);
            else
                cursor = db.rawQuery("SELECT * FROM STATS_NAMES_TABLE", null);
        else
            if (exercise)
                cursor = db.rawQuery("SELECT * FROM STATS_EXERCISE_TABLE_"+id, null);
            else
                cursor = db.rawQuery("SELECT * FROM STATS_VALUES_TABLE_"+id, null);
        if (cursor.moveToFirst()) {
            MyObject obj;
            do {
                if (id == null)
                    if (exercise)
                        obj = new MyObject(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6), cursor.getDouble(7));
                    else
                        obj = new MyObject(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                else
                    if (exercise)
                        obj = new MyObject(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5));
                    else
                        obj = new MyObject(cursor.getInt(0), cursor.getDouble(1), cursor.getString(2));
                list.add(obj);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public MyObject getOneInfo(int id, boolean exercise) {
        SQLiteDatabase db = this.getReadableDatabase();
        MyObject obj;
        Cursor cursor;
        if (exercise) {
            cursor = db.rawQuery("SELECT * FROM EXERCISES_TABLE WHERE EXERCISE_ID = "+id, null);
            cursor.moveToFirst();
            obj = new MyObject(
                    cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6), cursor.getDouble(7)
            );
        } else {
            cursor = db.rawQuery("SELECT * FROM STATS_NAMES_TABLE WHERE STATS_NAMES_ID = "+id, null);
            cursor.moveToFirst();
            obj = new MyObject(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
        }
        cursor.close();
        db.close();
        return obj;
    }

    public void addCustomStats(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("STATS_NAMES_NAME", name);
        cv.put("STATS_NAMES_START", "");
        cv.put("STATS_NAMES_END", "");
        db.insert("STATS_NAMES_TABLE", null, cv);
        // Take ID from new created row
        int id = 0;
        Cursor cursor = db.rawQuery("SELECT * FROM STATS_NAMES_TABLE", null);
        if (cursor.moveToLast())
            id = cursor.getInt(0);
        cursor.close();
        // Create table with user custom statistic
        db.execSQL(
                "CREATE TABLE STATS_VALUES_TABLE_"+id+" (STATS_VALUES_ID INTEGER PRIMARY KEY AUTOINCREMENT, STATS_VALUES_VALUE REAL, STATS_VALUES_DATE TEXT)"
        );
        db.close();
    }

    public void addVStats(int statsNamesId, Double value, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("STATS_VALUES_VALUE", value);
        cv.put("STATS_VALUES_DATE", date);
        db.insert("STATS_VALUES_TABLE_"+statsNamesId, null, cv);
    }

    public void addExStats(int exId, int result_s, String result_l, String time, String date, String weights) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("STATS_EXERCISE_RESULT_S", result_s);
        cv.put("STATS_EXERCISE_RESULT_L", result_l);
        cv.put("STATS_EXERCISE_TIME", time);
        cv.put("STATS_EXERCISE_DATE", date);
        cv.put("STATS_EXERCISE_WEIGHTS", weights);
        db.insert("STATS_EXERCISE_TABLE_"+exId, null, cv);
    }

    public void addExercise(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("EXERCISE_NAME", name);
        cv.put("EXERCISE_SECONDS", 120);
        cv.put("EXERCISE_FIRST", 10);
        cv.put("EXERCISE_REPS", 5);
        cv.put("EXERCISE_WEIGHT", 0.0);
        cv.put("EXERCISE_START", "");
        cv.put("EXERCISE_END", "");
        db.insert("EXERCISES_TABLE", null, cv);
        // Take ID from new created row
        int id = 0;
        Cursor cursor = db.rawQuery("SELECT * FROM EXERCISES_TABLE", null);
        if (cursor.moveToLast())
            id = cursor.getInt(0);
        cursor.close();
        // Create table with exercise statistics
        db.execSQL(
                "CREATE TABLE STATS_EXERCISE_TABLE_"+id+" (STATS_EXERCISE_ID INTEGER PRIMARY KEY AUTOINCREMENT, STATS_EXERCISE_RESULT_S INT, STATS_EXERCISE_RESULT_L TEXT, STATS_EXERCISE_TIME TEXT, STATS_EXERCISE_DATE TEXT, STATS_EXERCISE_WEIGHTS TEXT)"
        );
        db.close();
    }

    public void deleteStats(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM STATS_NAMES_TABLE WHERE STATS_NAMES_ID = "+id);
        db.execSQL("DROP TABLE STATS_VALUES_TABLE_"+id);
        db.close();
    }

    public void deleteExercise(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM EXERCISES_TABLE WHERE EXERCISE_ID = "+id);
        db.execSQL("DROP TABLE STATS_EXERCISE_TABLE_"+id);
        db.close();
    }

    public void updateStatsEntry(int objId, int statsNamesId, Double value) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE STATS_VALUES_TABLE_"+statsNamesId+" SET STATS_VALUES_VALUE = '"+value+"' WHERE STATS_VALUES_ID = "+objId);
        db.close();
    }

    public void updateStatsName(String newName, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE STATS_NAMES_TABLE SET STATS_NAMES_NAME = '"+newName+"' WHERE STATS_NAMES_ID = "+id);
        db.close();
    }

    public void updateExerciseSettings(String newName, int id, int seconds, int first, int reps, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE EXERCISES_TABLE SET EXERCISE_NAME = '"+newName+"', EXERCISE_SECONDS = '"+seconds+"', EXERCISE_FIRST = '"+first+"', EXERCISE_REPS = '"+reps+"', EXERCISE_WEIGHT = '"+weight+"' WHERE EXERCISE_ID = "+id);
        db.close();
    }

    public void saveDate(String date, int id, boolean exercise, boolean start) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (exercise)
            if (start)
                db.execSQL("UPDATE EXERCISES_TABLE SET EXERCISE_START = '"+date+"' WHERE EXERCISE_ID = "+id);
            else
                db.execSQL("UPDATE EXERCISES_TABLE SET EXERCISE_END = '"+date+"' WHERE EXERCISE_ID = "+id);
        else
            if (start)
                db.execSQL("UPDATE STATS_NAMES_TABLE SET STATS_NAMES_START = '"+date+"' WHERE STATS_NAMES_ID = "+id);
            else
                db.execSQL("UPDATE STATS_NAMES_TABLE SET STATS_NAMES_END = '"+date+"' WHERE STATS_NAMES_ID = "+id);
        db.close();
    }

    // COLORS
    public void setObjColor(int color, String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS COLORS_O (COLOR_ID TEXT PRIMARY KEY, COLOR_VALUE INT, USED INT)");
        Cursor cursor = db.rawQuery("SELECT * FROM COLORS_O WHERE COLOR_ID = '"+id+"'", null);
        if (cursor.moveToFirst()) {
            db.execSQL("UPDATE COLORS_O SET COLOR_VALUE = "+color+" WHERE COLOR_ID = '"+id+"'");
        } else {
            ContentValues cv = new ContentValues();
            cv.put("COLOR_ID", id);
            cv.put("COLOR_VALUE", color);
            cv.put("USED", 0);
            db.insert("COLORS_O", null, cv);
        }
        cursor.close();
        db.close();
    }

    public int[] getObjColor(String id) {
        int[] c = new int[] {0, 0};
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor1 = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='COLORS_O'", null);
        if (cursor1.moveToFirst()) {
            Cursor cursor = db.rawQuery("SELECT * FROM COLORS_O WHERE COLOR_ID = '"+id+"'", null);
            if (cursor.moveToFirst()) {
                c[0] = cursor.getInt(1);
                c[1] = cursor.getInt(2);
            }
            cursor.close();
        }
        cursor1.close();
        db.close();
        return c;
    }

    public LinkedHashMap<String, Integer> getAllObjColors() {
        LinkedHashMap<String, Integer> l = new LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor1 = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='COLORS_O'", null);
        if (cursor1.moveToFirst()) {
            Cursor cursor = db.rawQuery("SELECT * FROM COLORS_O", null);
            if (cursor.moveToFirst())
                do {
                    l.put(cursor.getString(0), cursor.getInt(1));
                } while (cursor.moveToNext());
            cursor.close();
        }
        cursor1.close();
        db.close();
        return l;
    }

    // EXPORT / IMPORT
    public String exportDatabase() {
        try {
            // Prepare data
            JSONArray jArray = new JSONArray();
            SQLiteDatabase dbImport = getWritableDatabase();
            for (String table: new String[]{"EXERCISES_TABLE", "STATS_NAMES_TABLE"}) {
                Cursor c = dbImport.rawQuery("SELECT * FROM "+table, null);
                if (c.moveToFirst()) {
                    do {
                        int id = c.getInt(0);
                        JSONObject jObj = new JSONObject();
                        if (table.equals("EXERCISES_TABLE")) {
                            jObj.put("type", "ex");
                            jObj.put("ex_id", id);
                            jObj.put("ex_name", c.getString(1));
                            jObj.put("ex_sec", c.getInt(2));
                            jObj.put("ex_first", c.getInt(3));
                            jObj.put("ex_reps", c.getInt(4));
                            jObj.put("ex_start", c.getString(5));
                            jObj.put("ex_end", c.getString(6));
                            jObj.put("ex_weight", c.getDouble(7));
                            jArray.put(jObj);
                            Cursor c1 = dbImport.rawQuery("SELECT * FROM STATS_EXERCISE_TABLE_"+id, null);
                            if (c1.moveToFirst()) {
                                do {
                                    JSONObject jObj1 = new JSONObject();
                                    jObj1.put("type", "ex_e");
                                    jObj1.put("st_e_id", c1.getInt(0));
                                    jObj1.put("st_e_res_s", c1.getInt(1));
                                    jObj1.put("st_e_res_l", c1.getString(2));
                                    jObj1.put("st_e_time", c1.getString(3));
                                    jObj1.put("st_e_date", c1.getString(4));
                                    jObj1.put("st_e_weights", c1.getString(5));
                                    jObj1.put("parent_id", id);
                                    jArray.put(jObj1);
                                } while (c1.moveToNext());
                            }
                            c1.close();
                        } else if (table.equals("STATS_NAMES_TABLE")) {
                            jObj.put("type", "st");
                            jObj.put("st_n_id", id);
                            jObj.put("st_n_name", c.getString(1));
                            jObj.put("st_n_start", c.getString(2));
                            jObj.put("st_n_end", c.getString(3));
                            jArray.put(jObj);
                            Cursor c1 = dbImport.rawQuery("SELECT * FROM STATS_VALUES_TABLE_"+id, null);
                            if (c1.moveToFirst()) {
                                do {
                                    JSONObject jObj1 = new JSONObject();
                                    jObj1.put("type", "st_e");
                                    jObj1.put("st_v_id", c1.getInt(0));
                                    jObj1.put("st_v_value", c1.getDouble(1));
                                    jObj1.put("st_v_date", c1.getString(2));
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
            dbImport.close();
            return jArray.toString();
        }  catch (Exception e) {
            e.printStackTrace();
            return context.getString(R.string.error);
        }
    }

    public boolean importDatabase(String data) {
        String oldData = exportDatabase();
        try {
            if (!(data.contains("[") && data.contains("]") && data.contains("{") && data.contains("}"))) {
                throw new Exception("Invalid data");
            }
            cleanDatabase();
            // Loop data
            while (data.length() > 1) {
                // Obj in data
                int start = data.indexOf("{");
                int end = data.indexOf("}");
                String obj = data.substring(start+1, end);
                ContentValues cv = new ContentValues();
                String type = "";
                // Loop parameters in obj
                while (obj.length() > 1) {
                    int endObj = obj.indexOf(",");
                    String entry;
                    if (endObj != -1)
                        entry = obj.substring(0, endObj);
                    else
                        entry = obj;
                    int x = entry.indexOf(":");
                    String key = entry.substring(0, x);
                    key = key.substring(1, key.length()-1);
                    String value = entry.substring(x+1);
                    Double valueDouble = null;
                    Integer valueInt = null;
                    if (value.startsWith("\"") && value.endsWith("\""))
                        value = value.substring(1, value.length()-1);
                    else if (key.equals("st_v_value"))
                        valueDouble = Double.parseDouble(value);
                    else
                        valueInt = Integer.parseInt(value);
                    // Put parameter to CV
                    if (key.equals("type")) {
                        type = value;
                    } else {
                        if (valueDouble != null) {
                            cv.put(key, valueDouble);
                        } else if (valueInt != null) {
                            cv.put(key, valueInt);
                        } else {
                            cv.put(key, value);
                        }
                    }
                    // Remove finished parameter from obj
                    if (endObj != -1)
                        obj = obj.substring(endObj+1);
                    else
                        obj = "";
                }
                // Insert obj to DB
                SQLiteDatabase db = getWritableDatabase();
                ContentValues cvDB = new ContentValues();
                int id;
                switch (type) {
                    case "ex":
                        id = (int) cv.get("ex_id");
                        cvDB.put("EXERCISE_ID", id);
                        cvDB.put("EXERCISE_NAME", (String) cv.get("ex_name"));
                        cvDB.put("EXERCISE_SECONDS", (int) cv.get("ex_sec"));
                        cvDB.put("EXERCISE_FIRST", (int) cv.get("ex_first"));
                        cvDB.put("EXERCISE_REPS", (int) cv.get("ex_reps"));
                        cvDB.put("EXERCISE_START", (String) cv.get("ex_start"));
                        cvDB.put("EXERCISE_END", (String) cv.get("ex_end"));
                        try {
                            cvDB.put("EXERCISE_WEIGHT", (Double) cv.get("ex_weight"));
                        } catch (Exception e) {
                            cvDB.put("EXERCISE_WEIGHT", Double.parseDouble(String.valueOf((int) cv.get("ex_weight"))));
                        }
                        db.insert("EXERCISES_TABLE", null, cvDB);
                        db.execSQL(
                                "CREATE TABLE STATS_EXERCISE_TABLE_"+id+" (STATS_EXERCISE_ID INTEGER PRIMARY KEY AUTOINCREMENT, STATS_EXERCISE_RESULT_S INT, STATS_EXERCISE_RESULT_L TEXT, STATS_EXERCISE_TIME TEXT, STATS_EXERCISE_DATE TEXT, STATS_EXERCISE_WEIGHTS TEXT)"
                        );
                        break;
                    case "st":
                        id = (int) cv.get("st_n_id");
                        cvDB.put("STATS_NAMES_ID", id);
                        cvDB.put("STATS_NAMES_NAME", (String) cv.get("st_n_name"));
                        cvDB.put("STATS_NAMES_START", (String) cv.get("st_n_start"));
                        cvDB.put("STATS_NAMES_END", (String) cv.get("st_n_end"));
                        db.insert("STATS_NAMES_TABLE", null, cvDB);
                        db.execSQL(
                                "CREATE TABLE STATS_VALUES_TABLE_"+id+" (STATS_VALUES_ID INTEGER PRIMARY KEY AUTOINCREMENT, STATS_VALUES_VALUE REAL, STATS_VALUES_DATE TEXT)"
                        );
                        break;
                    case "ex_e":
                        id = (int) cv.get("st_e_id");
                        cvDB.put("STATS_EXERCISE_ID", id);
                        cvDB.put("STATS_EXERCISE_RESULT_S", (int) cv.get("st_e_res_s"));
                        cvDB.put("STATS_EXERCISE_RESULT_L", (String) cv.get("st_e_res_l"));
                        cvDB.put("STATS_EXERCISE_TIME", (String) cv.get("st_e_time"));
                        cvDB.put("STATS_EXERCISE_DATE", (String) cv.get("st_e_date"));
                        cvDB.put("STATS_EXERCISE_WEIGHTS", (String) cv.get("st_e_weights"));
                        db.insert("STATS_EXERCISE_TABLE_"+(int) cv.get("parent_id"), null, cvDB);
                        break;
                    case "st_e":
                        id = (int) cv.get("st_v_id");
                        cvDB.put("STATS_VALUES_ID", id);
                        cvDB.put("STATS_VALUES_VALUE", (Double) cv.get("st_v_value"));
                        cvDB.put("STATS_VALUES_DATE", (String) cv.get("st_v_date"));
                        db.insert("STATS_VALUES_TABLE_"+ (int) cv.get("parent_id"), null, cvDB);
                        break;
                }
                db.close();
                // Remove finished obj from data
                data = data.substring(end+1);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            cleanDatabase();
            importDatabase(oldData);
            return false;
        }
    }

    public void cleanDatabase() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM EXERCISES_TABLE");
        db.execSQL("DELETE FROM STATS_NAMES_TABLE");
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (cursor.moveToFirst())
            do {
                String tableName = cursor.getString(0);
                for (String name: new String[] {"STATS_VALUES_TABLE_", "STATS_EXERCISE_TABLE_", "COLORS"}) {
                    if (tableName.contains(name))
                        db.execSQL("DROP TABLE "+tableName);
                }
            } while (cursor.moveToNext());
        cursor.close();
        db.close();
    }
}