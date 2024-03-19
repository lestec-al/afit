package com.yurhel.alex.afit.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.yurhel.alex.afit.R;

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
        db.execSQL("CREATE TABLE exercises ("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "name TEXT, "+
                "rest INT, "+
                "reps INT, "+
                "sets INT, "+
                "start TEXT, "+
                "ended TEXT, "+
                "weight REAL, "+
                "color INT)"
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
    public void onUpgrade(SQLiteDatabase db, int oldDB, int newDB) {}


    // EDIT ACTIVITY. WORKOUT WITH WEIGHT
    public void setWeights(String id, Boolean isWeightShow) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS weights ( id TEXT, isWeightShow INT)");

        ContentValues cv = new ContentValues();
        cv.put("id", id);
        cv.put("isWeightShow", (isWeightShow) ? 1 : 0);

        Cursor c = db.rawQuery("SELECT * FROM weights WHERE id = '"+id+"'", null);
        if (c.moveToFirst()) {
            db.update("weights", cv, "id = '"+id+"'", null);
        } else {
            db.insert("weights", null, cv);
        }
        c.close();
        db.close();
    }

    public boolean getIsWeightShowForMainStat(String id) {
        boolean l = false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c1 = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='weights'", null);
        if (c1.moveToFirst()) {
            Cursor c = db.rawQuery("SELECT * FROM weights WHERE id = '"+id+"'", null);
            if (c.moveToFirst()) l = c.getInt(1) == 1;
            c.close();
        }
        c1.close();
        db.close();
        return l;
    }


    // MAIN OBJs POSITIONS
    public void setPositions(LinkedHashMap<String, Integer> pos) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS positions ( id TEXT, pos INT)");
        db.execSQL("DELETE FROM positions");
        for (String id : pos.keySet()) {
            ContentValues cv = new ContentValues();
            cv.put("id", id);
            cv.put("pos", pos.get(id));
            db.insert("positions", null, cv);
        }
        db.close();
    }

    public LinkedHashMap<String, Integer> getPositions() {
        LinkedHashMap<String, Integer> l = new LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c1 = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='positions'", null);
        if (c1.moveToFirst()) {
            Cursor c = db.rawQuery("SELECT * FROM positions", null);
            if (c.moveToFirst()) {
                do {
                    l.put(c.getString(0), c.getInt(1));
                } while (c.moveToNext());
            }
            c.close();
        }
        c1.close();
        db.close();
        return l;
    }


    // ALL STATS ACTIVITY
    public void setAllDataGraphDates(String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS allStats ( "+
                "id INTEGER PRIMARY KEY, "+
                "start TEXT, "+
                "ended TEXT)"
        );
        Cursor c = db.rawQuery("SELECT * FROM allStats", null);
        ContentValues cv = new ContentValues();
        cv.put("start", startDate);
        cv.put("ended", endDate);
        if (c.moveToFirst())
            db.update("allStats", cv, "id = 1", null);
        else
            db.insert("allStats", null, cv);
        c.close();
        db.close();
    }

    public String[] getAllDataGraphDates() {
        String startDate = "";
        String endDate = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c1 = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='allStats'", null);
        if (c1.moveToFirst()) {
            Cursor c = db.rawQuery("SELECT * FROM allStats", null);
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

    public void setOrDelAllDataSelected(int statsId, int isObjExercise, boolean isActionDelete) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS allStatsSelected ( "+
                "id INTEGER PRIMARY KEY, "+
                "statsId INT, "+
                "isExercise INT)"
        );
        if (isActionDelete) {
            db.execSQL("DELETE FROM allStatsSelected WHERE statsId = "+statsId+" AND isExercise = "+isObjExercise);
        } else {
            ContentValues cv = new ContentValues();
            cv.put("statsId", statsId);
            cv.put("isExercise", isObjExercise);
            db.insert("allStatsSelected", null, cv);
        }
        db.close();
    }

    public ArrayList<int[]> getAllDataSelected() {
        ArrayList<int[]> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c1 = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='allStatsSelected'", null);
        if (c1.moveToFirst()) {
            Cursor c = db.rawQuery("SELECT * FROM allStatsSelected", null);
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

    public boolean isEntriesTableExist(Integer tableId, boolean isExercise) {
        String tableName = ((isExercise)? "exercise_": "stats_")+tableId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"+tableName+"'", null);
        boolean result = c.moveToFirst();
        c.close();
        db.close();
        return result;
    }


    // GET OBJs
    public ArrayList<Obj> getAll() {
        ArrayList<Obj> l = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM exercises", null);
        if (c.moveToFirst())
            do {
                Cursor c1 = db.rawQuery("SELECT * FROM exercise_"+c.getInt(0), null);
                if (c1.moveToFirst()) {
                    do {
                        Obj o = new Obj(
                                c1.getInt(0),
                                c1.getInt(1),
                                c1.getString(2),
                                c1.getString(3),
                                c1.getString(4),
                                c1.getString(5)
                        );
                        o.parentId = c.getInt(0);
                        o.name = c.getString(1);
                        o.color = c.getInt(8);
                        l.add(o);
                    } while (c1.moveToNext());
                }
                c1.close();
            } while (c.moveToNext());
        c.close();
        c = db.rawQuery("SELECT * FROM stats", null);
        if (c.moveToFirst())
            do {
                Cursor c1 = db.rawQuery("SELECT * FROM stats_"+c.getInt(0), null);
                if (c1.moveToFirst()) {
                    do {
                        Obj o = new Obj(
                                c1.getInt(0),
                                c1.getDouble(1),
                                c1.getString(2),
                                c1.getString(3)
                        );
                        o.parentId = c.getInt(0);
                        o.name = c.getString(1);
                        o.color = c.getInt(4);
                        l.add(o);
                    } while (c1.moveToNext());
                }
                c1.close();
            } while (c.moveToNext());
        c.close();
        db.close();
        return l;
    }

    public ArrayList<Obj> getTableEntries(Integer tableId, boolean isExercise) {
        ArrayList<Obj> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlStr;
        if (tableId == null)
            sqlStr = "SELECT * FROM "+((isExercise) ? "exercises": "stats");
        else
            sqlStr = "SELECT * FROM "+((isExercise) ? "exercise_": "stats_")+tableId;
        Cursor cursor = db.rawQuery(sqlStr, null);
        if (cursor.moveToFirst()) {
            Obj obj;
            do {
                if (tableId == null)
                    if (isExercise)
                        obj = new Obj(
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
                        obj = new Obj(
                                cursor.getInt(0),
                                cursor.getString(1),
                                cursor.getString(2),
                                cursor.getString(3),
                                cursor.getInt(4)
                        );
                else
                    if (isExercise)
                        obj = new Obj(
                                cursor.getInt(0),
                                cursor.getInt(1),
                                cursor.getString(2),
                                cursor.getString(3),
                                cursor.getString(4),
                                cursor.getString(5)
                        );
                    else
                        obj = new Obj(
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

    public Obj getOneMainObj(int id, boolean isExercise) {
        SQLiteDatabase db = this.getReadableDatabase();
        Obj obj;
        Cursor cursor;
        if (isExercise) {
            cursor = db.rawQuery("SELECT * FROM exercises WHERE id = "+id, null);
            cursor.moveToFirst();
            obj = new Obj(
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
            cursor = db.rawQuery("SELECT * FROM stats WHERE id = "+id, null);
            cursor.moveToFirst();
            obj = new Obj(
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

    public Obj getLastExerciseEntry(int mainObjId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM exercise_"+mainObjId, null);
        Obj obj = null;
        if (cursor.moveToLast()) {
            obj = new Obj(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
            );
        }
        cursor.close();
        db.close();
        return obj;
    }

    // ADD OBJs
    public void addStats(String name, int color) {
        SQLiteDatabase db = this.getWritableDatabase();
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
        db.close();
    }

    public void addStatsEntry(int statsNamesId, Double value, String date, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("value", value);
        cv.put("date", date);
        cv.put("note", note);
        db.insert("stats_"+statsNamesId, null, cv);
        db.close();
    }

    public void addExercise(String name, int rest, int sets, double weight, int color) {
        SQLiteDatabase db = this.getWritableDatabase();
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
        db.close();
    }

    public void addExerciseEntry(int exId, int result_s, String result_l, String time, String date, String weights) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("resultShort", result_s);
        cv.put("resultLong", result_l);
        cv.put("time", time);
        cv.put("date", date);
        cv.put("weights", weights);
        db.insert("exercise_"+exId, null, cv);
        db.close();
    }


    // DEL OBJs
    public void deleteObj(int id, boolean isExercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (isExercise) {
            db.execSQL("DELETE FROM exercises WHERE id = "+id);
            db.execSQL("DROP TABLE exercise_"+id);
        } else {
            db.execSQL("DELETE FROM stats WHERE id = "+id);
            db.execSQL("DROP TABLE stats_"+id);
        }
        db.close();
    }

    public void deleteSmallObj(int tableId, int entryId, boolean isExercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+((isExercise) ? "exercise_": "stats_")+tableId+" WHERE id = "+entryId);
        db.close();
    }


    // UPDATE OBJs
    public void updateStats(String name, int id, int color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("color", color);
        db.update("stats", cv, "id = "+id, null);
        db.close();
    }

    public void updateStatsEntry(int id, int statsNamesId, Double value, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("value", value);
        cv.put("note", note);
        db.update("stats_"+statsNamesId, cv, "id = "+id, null);
        db.close();
    }

    public void updateExercise(String name, int id, int rest, int reps, int sets, double weight, int color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("rest", rest);
        cv.put("reps", reps);
        cv.put("sets", sets);
        cv.put("weight", weight);
        cv.put("color", color);
        db.update("exercises", cv, "id = "+id, null);
        db.close();
    }

    public void setDate(String date, int id, boolean isExercise, boolean start) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(
                "UPDATE "+((isExercise) ? "exercises": "stats")+" SET "+((start) ? "start": "ended")+" = '"+date+"' WHERE id = "+id
        );
        db.close();
    }


    // EXPORT / IMPORT
    public JSONArray exportDB() {
        try {
            JSONArray jArray = new JSONArray();
            SQLiteDatabase dbImport = getWritableDatabase();
            for (String table: new String[]{"exercises", "stats"}) {
                Cursor c = dbImport.rawQuery("SELECT * FROM "+table, null);
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
                            Cursor c1 = dbImport.rawQuery("SELECT * FROM exercise_"+id, null);
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
                            Cursor c1 = dbImport.rawQuery("SELECT * FROM stats_"+id, null);
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
            dbImport.close();
            return jArray;
        }  catch (Exception e) {
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
                db.close();
            }
            return true;
        } catch (Exception e) {
            cleanDatabase();
            importDB(oldData.toString());
            return false;
        }
    }

    public void cleanDatabase() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM exercises");
        db.execSQL("DELETE FROM stats");
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (cursor.moveToFirst()) {
            do {
                String tableName = cursor.getString(0);
                // Clear positions
                if (tableName.equals("positions")) db.execSQL("DELETE FROM positions");
                // Clear entries tables
                for (String name : new String[]{"stats_", "exercise_"}) {
                    if (tableName.contains(name))
                        db.execSQL("DROP TABLE " + tableName);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }
}