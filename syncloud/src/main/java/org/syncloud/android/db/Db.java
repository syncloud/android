package org.syncloud.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import org.apache.log4j.Logger;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.ssh.model.Identification;

import java.util.ArrayList;
import java.util.List;

public class Db extends SQLiteOpenHelper {

    private static Logger logger = Logger.getLogger(Db.class);

    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "syncloud";
    public static final String DEVICE_TABLE = "device";
    public static final String USER_DOMAIN = "user_domain";
    public static final String LOCAL_HOST_COLUMN = "local_host";
    public static final String LOCAL_PORT_COLUMN = "local_port";
    public static final String LOGIN_COLUMN = "login";
    public static final String PASSWORD_COLUMN = "password";
    public static final String SSHKEY_COLUMN = "key";
    public static final String MAC_ADDRESS_COLUMN = "mac_address";
    public static final String TITLE_COLUMN = "title";
    public static final String NAME_COLUMN = "name";

    private static final String DEVICE_TABLE_CREATE =
            "CREATE TABLE " + DEVICE_TABLE + " (" +
                    MAC_ADDRESS_COLUMN + " TEXT PRIMARY KEY, " +
                    TITLE_COLUMN + " TEXT, " +
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
                Credentials credentials = new Credentials(
                        cursor.getString(cursor.getColumnIndex(LOGIN_COLUMN)),
                        cursor.getString(cursor.getColumnIndex(PASSWORD_COLUMN)),
                        cursor.getString(cursor.getColumnIndex(SSHKEY_COLUMN)));

                Endpoint endpoint = new Endpoint(
                        cursor.getString(cursor.getColumnIndex(LOCAL_HOST_COLUMN)),
                        cursor.getInt(cursor.getColumnIndex(LOCAL_PORT_COLUMN)));

                Identification id = new Identification(
                        cursor.getString(cursor.getColumnIndex(MAC_ADDRESS_COLUMN)),
                        cursor.getString(cursor.getColumnIndex(NAME_COLUMN)),
                        cursor.getString(cursor.getColumnIndex(TITLE_COLUMN))
                );

                Device device = new Device(
                        cursor.getString(cursor.getColumnIndex(MAC_ADDRESS_COLUMN)),
                        id,
                        cursor.getString(cursor.getColumnIndex(USER_DOMAIN)),
                        endpoint,
                        credentials);

                devices.add(device);
            }
        } finally {
            cursor.close();
        }

        return devices;
    }

    public void remove(Device device) {

        getWritableDatabase().delete(
                DEVICE_TABLE,
                MAC_ADDRESS_COLUMN + "=?",
                new String[] { device.macAddress() });

    }

    public void upsert(Device device) {
        logger.info("upserting device: " + device);

        Cursor cursor = getWritableDatabase().query(
                DEVICE_TABLE,
                null,
                MAC_ADDRESS_COLUMN + "=?",
                new String[] { device.macAddress() },
                null,
                null,
                null);

        if (cursor.getCount() > 0)
            update(device);
        else
            insert(device);
    }

    public void insert(Device device) {
        logger.info("inserting device: " + device);
        ContentValues values = toFields(device, true);
        getWritableDatabase().insert(
                DEVICE_TABLE,
                null,
                values);
    }

    public void update(Device device) {
        logger.info("updating device: " + device);
        ContentValues values = toFields(device, false);

        int update = getWritableDatabase().update(
                DEVICE_TABLE,
                values,
                MAC_ADDRESS_COLUMN + "=?",
                new String[]{device.macAddress()});
        System.out.println(update);
    }

    private ContentValues toFields(Device device, boolean addMacAddress) {
        ContentValues values = new ContentValues();
        if (addMacAddress)
            values.put(MAC_ADDRESS_COLUMN, device.macAddress());
        values.put(NAME_COLUMN, device.id().name);
        values.put(TITLE_COLUMN, device.id().title);
        values.put(USER_DOMAIN, device.userDomain());
        values.put(LOCAL_HOST_COLUMN, device.localEndpoint().host());
        values.put(LOCAL_PORT_COLUMN, device.localEndpoint().port());
        values.put(LOGIN_COLUMN, device.credentials().login());
        values.put(PASSWORD_COLUMN, device.credentials().password());
        values.put(SSHKEY_COLUMN, device.credentials().key());
        return values;
    }

}