package chinmayd.notesapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import chinmayd.notesapp.DataModels.Notes;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "notesDB.db";

    private static final String TABLE_NAME = "notesDB_data";

    private static final String COL1 = "ID";

    private static final String COL2 = "TITLE";

    private static final String COL3 = "DATA";

    private static final String COL4 = "TIMESTAMP";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COL2 + " TEXT, " + COL3 + " TEXT, " + COL4 + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addData(String title, String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        Log.i(TAG, "DatabaseItems " + count);

        // Checks if there are any items in the database, if not then resets the AutoIncrement count to 0.
        if (count == 0) {
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_NAME + "'");
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, title);
        contentValues.put(COL3, data);

        long result = db.insert(TABLE_NAME, null, contentValues);

        db.close();

        return result;
    }

    public Notes getNote(long id) {
        // Get readable database, as we're getting notes.
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[]{COL1, COL2, COL3, COL4}, COL1 + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // Create new notes object.
        Notes note = new Notes(cursor.getInt(cursor.getColumnIndex(COL1)), cursor.getString(cursor.getColumnIndex(COL2)), cursor.getString(cursor.getColumnIndex(COL3)), cursor.getString(cursor.getColumnIndex(COL4)));

        // Close Database connection.
        cursor.close();

        return note;
    }

    public Cursor getListContents() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL4 + " DESC ", null);
        return data;
    }

    public Cursor getContents(int id) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE ID= " + id;
        cursor = db.rawQuery(sql, null);
        return cursor;
    }

    public boolean updateData(int id, String title, String data) {
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "SELECT " + COL1 + " FROM " + TABLE_NAME + " WHERE ID= " + id;
        Log.i(TAG, "SQL query: " + sql);
        cursor = db.rawQuery(sql, null);
        Log.i(TAG, "Cursor Count: " + cursor.getCount());

        // If record found then update, else return false
        if (cursor.getCount() > 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL1, id);
            contentValues.put(COL2, title);
            contentValues.put(COL3, data);
            String sId = Integer.toString(id);
            db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{sId});
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public int deleteData(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sId = Integer.toString(id);
        int data = db.delete(TABLE_NAME, "ID = ?", new String[]{sId});
        Log.i(TAG, "Number of data deleted: " + data);
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        Log.i(TAG, "Number of remaining data in the database: " + count);

        // Checks if there are any items in the database, if not then resets the autoincrement count to 0.
        if (count == 0) {
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_NAME + "'");
            Log.i(TAG, "AutoIncrement count has been reset!");
        }

        return data;
    }
}
