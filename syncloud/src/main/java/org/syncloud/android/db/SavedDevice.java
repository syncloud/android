package org.syncloud.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import org.syncloud.model.Device;

import java.util.ArrayList;
import java.util.List;

public class SavedDevice extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "syncloud";
    public static final String DEVICE_TABLE = "device";
    public static final String HOST_COLUMN = "host";
    public static final String PORT_COLUMN = "port";
    public static final String SSHKEY_COLUMN = "key";
    private static final String DICTIONARY_TABLE_CREATE =
            "CREATE TABLE " + DEVICE_TABLE + " (" +
                    HOST_COLUMN + " TEXT, " +
                    SSHKEY_COLUMN + " TEXT, " +
                    PORT_COLUMN + " INTEGER" +
                    ");";

    public SavedDevice(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DICTIONARY_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    public void insert(String hostname, int port, String key) {

        ContentValues values = new ContentValues();
        values.put(HOST_COLUMN, hostname);
        values.put(PORT_COLUMN, port);
        values.put(SSHKEY_COLUMN, key);
        getWritableDatabase().insert(DEVICE_TABLE, null, values);

    }

    public List<Device> list() {

        ArrayList<Device> devices = new ArrayList<Device>();

        final SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DEVICE_TABLE);
        Cursor cursor = qb.query(db, null, null, null, null, null, null);
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                devices.add(
                        new Device(
                                cursor.getString(cursor.getColumnIndex(HOST_COLUMN)),
                                cursor.getInt(cursor.getColumnIndex(PORT_COLUMN)),
                                cursor.getString(cursor.getColumnIndex(SSHKEY_COLUMN)))
                );
            }
        } finally {
            cursor.close();
        }

        return devices;
    }

    public void remove(String hostname) {

        getWritableDatabase().delete(DEVICE_TABLE, HOST_COLUMN + "=?", new String[] { hostname });

    }

}