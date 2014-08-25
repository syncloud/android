package org.syncloud.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DirectEndpoint;

import java.util.ArrayList;
import java.util.List;

public class Db extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "syncloud";
    public static final String DEVICE_TABLE = "device";
    public static final String NAME_COLUMN = "name";
    public static final String USER_DOMAIN = "user_domain";
    public static final String LOCAL_HOST_COLUMN = "local_host";
    public static final String LOCAL_PORT_COLUMN = "local_port";
    public static final String LOGIN_COLUMN = "login";
    public static final String PASSWORD_COLUMN = "password";
    public static final String SSHKEY_COLUMN = "key";
    public static final String DEVICE_ID_COLUMN = "device_id";

    private static final String DEVICE_TABLE_CREATE =
            "CREATE TABLE " + DEVICE_TABLE + " (" +
                    DEVICE_ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NAME_COLUMN + " TEXT, " +
                    USER_DOMAIN + " TEXT, " +
                    LOCAL_HOST_COLUMN + " TEXT, " +
                    LOCAL_PORT_COLUMN + " INTEGER," +
                    LOGIN_COLUMN + " TEXT, " +
                    PASSWORD_COLUMN + " TEXT, " +
                    SSHKEY_COLUMN + " TEXT " +
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
                                cursor.getInt(cursor.getColumnIndex(DEVICE_ID_COLUMN)),
                                cursor.getString(cursor.getColumnIndex(NAME_COLUMN)),
                                cursor.getString(cursor.getColumnIndex(USER_DOMAIN)),
                                new DirectEndpoint(
                                        cursor.getString(cursor.getColumnIndex(LOCAL_HOST_COLUMN)),
                                        cursor.getInt(cursor.getColumnIndex(LOCAL_PORT_COLUMN)),
                                        cursor.getString(cursor.getColumnIndex(LOGIN_COLUMN)),
                                        cursor.getString(cursor.getColumnIndex(PASSWORD_COLUMN)),
                                        cursor.getString(cursor.getColumnIndex(SSHKEY_COLUMN))))
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
                DEVICE_ID_COLUMN + "=?",
                new String[] { device.getId().toString() });

    }

    public void insert(Device device) {
        ContentValues values = toFields(device);
        getWritableDatabase().insert(DEVICE_TABLE, null, values);
    }

    public void update(Device device) {
        ContentValues values = toFields(device);
        values.put(NAME_COLUMN, device.getName());

        int update = getWritableDatabase().update(
                DEVICE_TABLE,
                values,
                DEVICE_ID_COLUMN + "=?",
                new String[]{device.getId().toString()});
        System.out.println(update);
    }

    private ContentValues toFields(Device device) {
        ContentValues values = new ContentValues();
        values.put(USER_DOMAIN, device.getUserDomain());
        values.put(LOCAL_HOST_COLUMN, device.getLocalEndpoint().getHost());
        values.put(LOCAL_PORT_COLUMN, device.getLocalEndpoint().getPort());
        values.put(LOGIN_COLUMN, device.getLocalEndpoint().getLogin());
        values.put(PASSWORD_COLUMN, device.getLocalEndpoint().getPassword());
        values.put(SSHKEY_COLUMN, device.getLocalEndpoint().getKey());
        return values;
    }
}