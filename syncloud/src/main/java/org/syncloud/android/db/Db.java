package org.syncloud.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import org.syncloud.ssh.model.Device;

import java.util.ArrayList;
import java.util.List;

public class Db extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "syncloud";
    public static final String DEVICE_TABLE = "device";
    public static final String NAME_COLUMN = "name";
    public static final String EXTERNAL_HOST_COLUMN = "external_host";
    public static final String EXTERNAL_PORT_COLUMN = "external_port";
    public static final String LOCAL_HOST_COLUMN = "local_host";
    public static final String LOCAL_PORT_COLUMN = "local_port";
    public static final String LOGIN_COLUMN = "login";
    public static final String PASSWORD_COLUMN = "password";
    public static final String SSHKEY_COLUMN = "key";

    private static final String DEVICE_TABLE_CREATE =
            "CREATE TABLE " + DEVICE_TABLE + " (" +
                    "device_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NAME_COLUMN + " TEXT, " +
                    EXTERNAL_HOST_COLUMN + " TEXT, " +
                    LOGIN_COLUMN + " TEXT, " +
                    PASSWORD_COLUMN + " TEXT, " +
                    SSHKEY_COLUMN + " TEXT, " +
                    EXTERNAL_PORT_COLUMN + " INTEGER" +
                    ");";
    private Context context;

    public Db(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DEVICE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        context.deleteDatabase(DATABASE_NAME);
        onCreate(sqLiteDatabase);
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
                                cursor.getString(cursor.getColumnIndex(EXTERNAL_HOST_COLUMN)),
                                cursor.getInt(cursor.getColumnIndex(EXTERNAL_PORT_COLUMN)),
                                cursor.getString(cursor.getColumnIndex(LOCAL_HOST_COLUMN)),
                                cursor.getInt(cursor.getColumnIndex(LOCAL_PORT_COLUMN)),
                                cursor.getString(cursor.getColumnIndex(LOGIN_COLUMN)),
                                cursor.getString(cursor.getColumnIndex(PASSWORD_COLUMN)),
                                cursor.getString(cursor.getColumnIndex(SSHKEY_COLUMN)),
                                cursor.getString(cursor.getColumnIndex(NAME_COLUMN)))
                );
            }
        } finally {
            cursor.close();
        }

        return devices;
    }

    public void remove(Device device) {

        getWritableDatabase().delete(
                DEVICE_TABLE,
                EXTERNAL_HOST_COLUMN + "=? AND " + EXTERNAL_PORT_COLUMN + "=?",
                new String[] { device.getExternalHost(), String.valueOf(device.getExternalPort()) });

    }

    public void insert(Device device) {
        ContentValues values = new ContentValues();
        values.put(EXTERNAL_HOST_COLUMN, device.getExternalHost());
        values.put(EXTERNAL_PORT_COLUMN, device.getExternalPort());
        values.put(LOCAL_HOST_COLUMN, device.getLocalHost());
        values.put(LOCAL_PORT_COLUMN, device.getLocalPort());
        values.put(LOGIN_COLUMN, "root");
        values.put(PASSWORD_COLUMN, "syncloud");
        values.put(SSHKEY_COLUMN, device.getKey());
        getWritableDatabase().insert(DEVICE_TABLE, null, values);
    }

    public void update(Device device) {
        ContentValues values = new ContentValues();
        values.put(EXTERNAL_HOST_COLUMN, device.getExternalHost());
        values.put(EXTERNAL_PORT_COLUMN, device.getExternalPort());
        values.put(SSHKEY_COLUMN, device.getKey());
        values.put(NAME_COLUMN, device.getName());

        int update = getWritableDatabase().update(
                DEVICE_TABLE,
                values,
                EXTERNAL_HOST_COLUMN + "=? AND " + EXTERNAL_PORT_COLUMN + "=?",
                new String[]{device.getExternalHost(), String.valueOf(device.getExternalPort())});
        System.out.println(update);
    }
}