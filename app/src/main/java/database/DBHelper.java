package database;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CaffeineTracker.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "CaffeineEntries";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TOTAL_CAFFEINE = "total_caffeine";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_TOTAL_CAFFEINE + " INTEGER)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // 날짜별 카페인 누적량 저장
    public void saveCaffeineAmount(String date, int totalCaffeine) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TOTAL_CAFFEINE, totalCaffeine);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // 날짜별 카페인 누적량 가져오기
    public int getCaffeineAmount(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_TOTAL_CAFFEINE}, COLUMN_DATE + "=?", new String[]{date}, null, null, null);
        int totalCaffeine = 0;
        if (cursor.moveToFirst()) {
            totalCaffeine = cursor.getInt(cursor.getColumnIndex(COLUMN_TOTAL_CAFFEINE));
        }
        cursor.close();
        db.close();
        return totalCaffeine;
    }
}
