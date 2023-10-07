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
        super(context, "afit.db", null, 2);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE EXERCISES_TABLE ("+
                "EXERCISE_ID INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "EXERCISE_NAME TEXT, "+
                "EXERCISE_SECONDS INT, "+
                "EXERCISE_FIRST INT, "+
                "EXERCISE_REPS INT, "+
                "EXERCISE_START TEXT, "+
                "EXERCISE_END TEXT, "+
                "EXERCISE_WEIGHT REAL, "+
                "EXERCISE_COLOR INT)"
        );
        db.execSQL("CREATE TABLE STATS_NAMES_TABLE ("+
                "STATS_NAMES_ID INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "STATS_NAMES_NAME TEXT, "+
                "STATS_NAMES_START TEXT, "+
                "STATS_NAMES_END TEXT, "+
                "STATS_NAMES_COLOR INT)"
        );
    }

    private String strCreateExEntryTable(int id) {
        return "CREATE TABLE STATS_EXERCISE_TABLE_"+id+" ("+
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "STATS_EXERCISE_RESULT_S INT, "+
                "STATS_EXERCISE_RESULT_L TEXT, "+
                "STATS_EXERCISE_TIME TEXT, "+
                "STATS_EXERCISE_DATE TEXT, "+
                "STATS_EXERCISE_WEIGHTS TEXT)";
    }
    private String strCreateStEntryTable(int id) {
        return "CREATE TABLE STATS_VALUES_TABLE_"+id+" ("+
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "STATS_VALUES_VALUE REAL, "+
                "STATS_VALUES_DATE TEXT, "+
                "STATS_VALUES_NOTES TEXT)";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldDB, int newDB) {}

    // ALL DATA GRAPH
    public void setAllDataGraphDates(String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS MANY_STATS ( "+
                "ID INTEGER PRIMARY KEY, "+
                "START_DATE TEXT, "+
                "END_DATE TEXT)"
        );
        Cursor c = db.rawQuery("SELECT * FROM MANY_STATS", null);
        ContentValues cv = new ContentValues();
        cv.put("START_DATE", startDate);
        cv.put("END_DATE", endDate);
        if (c.moveToFirst())
            db.update("MANY_STATS", cv, "ID = 1", null);
        else
            db.insert("MANY_STATS", null, cv);
        c.close();
        db.close();
    }

    public String[] getAllDataGraphDates() {
        String startDate = "";
        String endDate = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c1 = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='MANY_STATS'", null);
        if (c1.moveToFirst()) {
            Cursor c = db.rawQuery("SELECT * FROM MANY_STATS", null);
            if (c.moveToFirst()) {
                startDate = c.getString(1);
                endDate = c.getString(2);
            }
            c.close();
        }
        c1.close();
        db.close();
        return new String[] {startDate, endDate};
    }

    public void setOrDelAllDataSelected(int statsID, int isObjExercise, boolean isActionDelete) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS MANY_LIST_STATS ( "+
                "ID INTEGER PRIMARY KEY, "+
                "STATS_ID INT, "+
                "IS_EXERCISE INT)"
        );
        if (isActionDelete) {
            Cursor c = db.rawQuery(
                    "SELECT * FROM MANY_LIST_STATS WHERE STATS_ID = "+statsID+" AND IS_EXERCISE = "+isObjExercise, null
            );
            if (c.moveToFirst())
                db.execSQL("DELETE FROM MANY_LIST_STATS WHERE STATS_ID = "+statsID+" AND IS_EXERCISE = "+isObjExercise);
            c.close();
        } else {
            ContentValues cv = new ContentValues();
            cv.put("STATS_ID", statsID);
            cv.put("IS_EXERCISE", isObjExercise);
            db.insert("MANY_LIST_STATS", null, cv);
        }
        db.close();
    }

    public ArrayList<int[]> getAllDataSelected() {
        ArrayList<int[]> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c1 = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='MANY_LIST_STATS'", null);
        if (c1.moveToFirst()) {
            Cursor c = db.rawQuery("SELECT * FROM MANY_LIST_STATS", null);
            if (c.moveToFirst()) {
                do {
                    list.add(new int[] {c.getInt(1), c.getInt(2)});
                } while (c.moveToNext());
            }
            c.close();
        }
        c1.close();
        db.close();
        return list;
    }

    public boolean isEntriesTableExist(Integer tableID, boolean isExercise) {
        String tableName = (isExercise)? "STATS_EXERCISE_TABLE_"+tableID: "STATS_VALUES_TABLE_"+tableID;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"+tableName+"'", null);
        boolean result = c.moveToFirst();
        c.close();
        db.close();
        return result;
    }

    // GET STATS
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
                                new MyObject(
                                        c1.getInt(0),
                                        c1.getInt(1),
                                        c1.getString(2),
                                        c1.getString(3),
                                        c1.getString(4),
                                        c1.getString(5)
                                ),
                                new MyObject(
                                        c.getInt(0),
                                        c.getString(1),
                                        c.getInt(2),
                                        c.getInt(3),
                                        c.getInt(4),
                                        c.getString(5),
                                        c.getString(6),
                                        c.getDouble(7),
                                        c.getInt(8)
                                )
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
                                new MyObject(
                                        c1.getInt(0),
                                        c1.getDouble(1),
                                        c1.getString(2),
                                        c1.getString(3)
                                ),
                                new MyObject(
                                        c.getInt(0),
                                        c.getString(1),
                                        c.getString(2),
                                        c.getString(3),
                                        c.getInt(4)
                                )
                        );
                    } while (c1.moveToNext());
                }
                c1.close();
            } while (c.moveToNext());
        c.close();
        db.close();
        return l;
    }

    public ArrayList<MyObject> getTableEntries(Integer tableID, boolean isExercise) {
        ArrayList<MyObject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlStr;
        if (tableID == null)
            if (isExercise)
                sqlStr = "SELECT * FROM EXERCISES_TABLE";
            else
                sqlStr = "SELECT * FROM STATS_NAMES_TABLE";
        else
            if (isExercise)
                sqlStr = "SELECT * FROM STATS_EXERCISE_TABLE_"+tableID;
            else
                sqlStr = "SELECT * FROM STATS_VALUES_TABLE_"+tableID;
        Cursor cursor = db.rawQuery(sqlStr, null);
        if (cursor.moveToFirst()) {
            MyObject obj;
            do {
                if (tableID == null)
                    if (isExercise)
                        obj = new MyObject(
                                cursor.getInt(0),
                                cursor.getString(1),
                                cursor.getInt(2),
                                cursor.getInt(3),
                                cursor.getInt(4),
                                cursor.getString(5),
                                cursor.getString(6),
                                cursor.getDouble(7),
                                cursor.getInt(8)
                        );
                    else
                        obj = new MyObject(
                                cursor.getInt(0),
                                cursor.getString(1),
                                cursor.getString(2),
                                cursor.getString(3),
                                cursor.getInt(4)
                        );
                else
                    if (isExercise)
                        obj = new MyObject(
                                cursor.getInt(0),
                                cursor.getInt(1),
                                cursor.getString(2),
                                cursor.getString(3),
                                cursor.getString(4),
                                cursor.getString(5)
                        );
                    else
                        obj = new MyObject(
                                cursor.getInt(0),
                                cursor.getDouble(1),
                                cursor.getString(2),
                                cursor.getString(3)
                        );
                list.add(obj);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public MyObject getOneMainObj(int id, boolean isExercise) {
        SQLiteDatabase db = this.getReadableDatabase();
        MyObject obj;
        Cursor cursor;
        if (isExercise) {
            cursor = db.rawQuery("SELECT * FROM EXERCISES_TABLE WHERE EXERCISE_ID = "+id, null);
            cursor.moveToFirst();
            obj = new MyObject(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getInt(2),
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getDouble(7),
                    cursor.getInt(8)
            );
        } else {
            cursor = db.rawQuery("SELECT * FROM STATS_NAMES_TABLE WHERE STATS_NAMES_ID = "+id, null);
            cursor.moveToFirst();
            obj = new MyObject(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4)
            );
        }
        cursor.close();
        db.close();
        return obj;
    }

    // ADD STATS
    public void addStats(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("STATS_NAMES_NAME", name);
        cv.put("STATS_NAMES_START", "");
        cv.put("STATS_NAMES_END", "");
        cv.put("STATS_NAMES_COLOR", context.getColor(R.color.green_main));
        db.insert("STATS_NAMES_TABLE", null, cv);
        // Take ID from new created row
        int id = 0;
        Cursor cursor = db.rawQuery("SELECT * FROM STATS_NAMES_TABLE", null);
        if (cursor.moveToLast())
            id = cursor.getInt(0);
        cursor.close();
        // Create table with user custom statistic
        db.execSQL(strCreateStEntryTable(id));
        db.close();
    }

    public void addStatsEntry(int statsNamesId, Double value, String date, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("STATS_VALUES_VALUE", value);
        cv.put("STATS_VALUES_DATE", date);
        cv.put("STATS_VALUES_NOTES", notes);
        db.insert("STATS_VALUES_TABLE_"+statsNamesId, null, cv);
        db.close();
    }

    public void addExercise(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("EXERCISE_NAME", name);
        cv.put("EXERCISE_SECONDS", 120);
        cv.put("EXERCISE_FIRST", 5);
        cv.put("EXERCISE_REPS", 5);
        cv.put("EXERCISE_WEIGHT", 0.0);
        cv.put("EXERCISE_START", "");
        cv.put("EXERCISE_END", "");
        cv.put("EXERCISE_COLOR", context.getColor(R.color.green_main));
        db.insert("EXERCISES_TABLE", null, cv);
        // Take ID from new created row
        int id = 0;
        Cursor cursor = db.rawQuery("SELECT * FROM EXERCISES_TABLE", null);
        if (cursor.moveToLast())
            id = cursor.getInt(0);
        cursor.close();
        // Create table with exercise statistics
        db.execSQL(strCreateExEntryTable(id));
        db.close();
    }

    public void addExerciseEntry(int exId, int result_s, String result_l, String time, String date, String weights) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("STATS_EXERCISE_RESULT_S", result_s);
        cv.put("STATS_EXERCISE_RESULT_L", result_l);
        cv.put("STATS_EXERCISE_TIME", time);
        cv.put("STATS_EXERCISE_DATE", date);
        cv.put("STATS_EXERCISE_WEIGHTS", weights);
        db.insert("STATS_EXERCISE_TABLE_"+exId, null, cv);
        db.close();
    }

    // DEL
    public void deleteObj(int id, boolean isExercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (isExercise) {
            db.execSQL("DELETE FROM EXERCISES_TABLE WHERE EXERCISE_ID = "+id);
            db.execSQL("DROP TABLE STATS_EXERCISE_TABLE_"+id);
        } else {
            db.execSQL("DELETE FROM STATS_NAMES_TABLE WHERE STATS_NAMES_ID = "+id);
            db.execSQL("DROP TABLE STATS_VALUES_TABLE_"+id);
        }
        db.close();
    }

    public void deleteSmallObj(int tableID, int entryID, boolean isExercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (isExercise)
            db.execSQL("DELETE FROM STATS_EXERCISE_TABLE_"+tableID+" WHERE STATS_EXERCISE_ID = "+entryID);
        else
            db.execSQL("DELETE FROM STATS_VALUES_TABLE_"+tableID+" WHERE STATS_VALUES_ID = "+entryID);
        db.close();
    }

    // UPDATE
    public void updateStats(String name, int id, int color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("STATS_NAMES_NAME", name);
        cv.put("STATS_NAMES_COLOR", color);
        db.update("STATS_NAMES_TABLE", cv, "STATS_NAMES_ID = "+id, null);
        db.close();
    }

    public void updateStatsEntry(int id, int statsNamesId, Double value, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("STATS_VALUES_VALUE", value);
        cv.put("STATS_VALUES_NOTES", note);
        db.update("STATS_VALUES_TABLE_"+statsNamesId, cv, "STATS_VALUES_ID = "+id, null);
        db.close();
    }

    public void updateExercise(String name, int id, int seconds, int first, int sets, double weight, int color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("EXERCISE_NAME", name);
        cv.put("EXERCISE_SECONDS", seconds);
        cv.put("EXERCISE_FIRST", first);
        cv.put("EXERCISE_REPS", sets);
        cv.put("EXERCISE_WEIGHT", weight);
        cv.put("EXERCISE_COLOR", color);
        db.update("EXERCISES_TABLE", cv, "EXERCISE_ID = "+id, null);
        db.close();
    }

    public void setDate(String date, int id, boolean isExercise, boolean start) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (isExercise)
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

    // EXPORT / IMPORT
    public JSONArray exportDB() {
        try {
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
                            jObj.put("ex_color", c.getDouble(8));
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
                            jObj.put("st_n_color", c.getString(4));
                            jArray.put(jObj);
                            Cursor c1 = dbImport.rawQuery("SELECT * FROM STATS_VALUES_TABLE_"+id, null);
                            if (c1.moveToFirst()) {
                                do {
                                    JSONObject jObj1 = new JSONObject();
                                    jObj1.put("type", "st_e");
                                    jObj1.put("st_v_id", c1.getInt(0));
                                    jObj1.put("st_v_value", c1.getDouble(1));
                                    jObj1.put("st_v_date", c1.getString(2));
                                    jObj1.put("st_v_notes", c1.getString(3));
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
            return jArray;
        }  catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    public boolean importDB(String data) {
        JSONArray oldData = exportDB();
        try {
            cleanDatabase();
            // Loop data
            JSONArray jsonData = new JSONArray(data);
            for (int i = 0; i < jsonData.length(); i++) {
                JSONObject obj = jsonData.getJSONObject(i);
                SQLiteDatabase db = getWritableDatabase();
                ContentValues cvDB = new ContentValues();
                String type = obj.getString("type");
                int id;
                // Insert obj to DB
                switch (type) {
                    case "ex":
                        id = obj.getInt("ex_id");
                        cvDB.put("EXERCISE_ID", id);
                        cvDB.put("EXERCISE_NAME", obj.getString("ex_name"));
                        cvDB.put("EXERCISE_SECONDS", obj.getInt("ex_sec"));
                        cvDB.put("EXERCISE_FIRST", obj.getInt("ex_first"));
                        cvDB.put("EXERCISE_REPS", obj.getInt("ex_reps"));
                        cvDB.put("EXERCISE_START", obj.getString("ex_start"));
                        cvDB.put("EXERCISE_END", obj.getString("ex_end"));
                        cvDB.put("EXERCISE_WEIGHT", obj.getDouble("ex_weight"));
                        try {// Try import color
                            cvDB.put("EXERCISE_COLOR", obj.getInt("ex_color"));
                        } catch (Exception e) {
                            cvDB.put("EXERCISE_COLOR", context.getColor(R.color.green_main));
                        }
                        db.insert("EXERCISES_TABLE", null, cvDB);
                        db.execSQL(strCreateExEntryTable(id));
                        break;
                    case "st":
                        id = obj.getInt("st_n_id");
                        cvDB.put("STATS_NAMES_ID", id);
                        cvDB.put("STATS_NAMES_NAME", obj.getString("st_n_name"));
                        cvDB.put("STATS_NAMES_START", obj.getString("st_n_start"));
                        cvDB.put("STATS_NAMES_END", obj.getString("st_n_end"));
                        try {// Try import color
                            cvDB.put("STATS_NAMES_COLOR", obj.getInt("st_n_color"));
                        } catch (Exception e) {
                            cvDB.put("STATS_NAMES_COLOR", context.getColor(R.color.green_main));
                        }
                        db.insert("STATS_NAMES_TABLE", null, cvDB);
                        db.execSQL(strCreateStEntryTable(id));
                        break;
                    case "ex_e":
                        cvDB.put("STATS_EXERCISE_ID", obj.getInt("st_e_id"));
                        cvDB.put("STATS_EXERCISE_RESULT_S", obj.getInt("st_e_res_s"));
                        cvDB.put("STATS_EXERCISE_RESULT_L", obj.getString("st_e_res_l"));
                        cvDB.put("STATS_EXERCISE_TIME", obj.getString("st_e_time"));
                        cvDB.put("STATS_EXERCISE_DATE", obj.getString("st_e_date"));
                        cvDB.put("STATS_EXERCISE_WEIGHTS", obj.getString("st_e_weights"));
                        db.insert("STATS_EXERCISE_TABLE_"+obj.getInt("parent_id"), null, cvDB);
                        break;
                    case "st_e":
                        cvDB.put("STATS_VALUES_ID", obj.getInt("st_v_id"));
                        cvDB.put("STATS_VALUES_VALUE", obj.getDouble("st_v_value"));
                        cvDB.put("STATS_VALUES_DATE", obj.getString("st_v_date"));
                        try {// Try import note
                            cvDB.put("STATS_VALUES_NOTES", obj.getString("st_v_notes"));
                        } catch (Exception ignore) {
                            cvDB.put("STATS_VALUES_NOTES", "");
                        }
                        db.insert("STATS_VALUES_TABLE_"+ obj.getInt("parent_id"), null, cvDB);
                        break;
                }
                db.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            cleanDatabase();
            importDB(oldData.toString());
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
                for (String name: new String[] {"STATS_VALUES_TABLE_", "STATS_EXERCISE_TABLE_"}) {
                    if (tableName.contains(name))
                        db.execSQL("DROP TABLE "+tableName);
                }
            } while (cursor.moveToNext());
        cursor.close();
        db.close();
    }
}