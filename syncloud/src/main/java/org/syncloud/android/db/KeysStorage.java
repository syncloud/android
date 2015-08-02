package org.syncloud.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import org.apache.log4j.Logger;
import org.syncloud.android.core.platform.model.Credentials;
import org.syncloud.android.core.platform.model.Key;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class KeysStorage extends SQLiteOpenHelper {
    private static Logger logger = Logger.getLogger(KeysStorage.class);

    private static final int DATABASE_VERSION = 9;
    private static final String DATABASE_NAME = "syncloud";
    public static final String DEVICE_TABLE = "keys";
    public static final String MAC_ADDRESS_COLUMN = "mac_address";
    public static final String USER_COLUMN = "user";
    public static final String PASSWORD_COLUMN = "password";

    private static final String DEVICE_TABLE_CREATE =
            "CREATE TABLE " + DEVICE_TABLE + " (" +
                    MAC_ADDRESS_COLUMN + " TEXT PRIMARY KEY, " +
                    USER_COLUMN + " TEXT, " +
                    PASSWORD_COLUMN + " TEXT " +
                    ");";

    private Context context;

    public KeysStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DEVICE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        logger.info("upgrading db by dropping it");
        context.deleteDatabase(DATABASE_NAME);
        onCreate(sqLiteDatabase);
    }

    public List<Key> list() {

        List<Key> keys = newArrayList();

        final SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DEVICE_TABLE);
        Cursor cursor = qb.query(db, null, null, null, null, null, null);
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                Key key = new Key(
                        cursor.getString(cursor.getColumnIndex(MAC_ADDRESS_COLUMN)),
                        new Credentials(
                                cursor.getString(cursor.getColumnIndex(USER_COLUMN)),
                                cursor.getString(cursor.getColumnIndex(PASSWORD_COLUMN))
                        )
                );

                keys.add(key);
            }
        } finally {
            cursor.close();
        }

        return keys;
    }

    public void upsert(Key key) {
        logger.info("upserting key: " + key);

        Cursor cursor = getWritableDatabase().query(
                DEVICE_TABLE,
                null,
                MAC_ADDRESS_COLUMN + "=?",
                new String[] { key.macAddress },
                null,
                null,
                null);

        if (cursor.getCount() > 0)
            update(key);
        else
            insert(key);
    }

    public void insert(Key key) {
        logger.info("inserting key: " + key);
        ContentValues values = toFields(key, true);
        getWritableDatabase().insert(
                DEVICE_TABLE,
                null,
                values);
    }

    public void update(Key key) {
        logger.info("updating key: " + key);
        ContentValues values = toFields(key, false);

        int update = getWritableDatabase().update(
                DEVICE_TABLE,
                values,
                MAC_ADDRESS_COLUMN + "=?",
                new String[]{key.macAddress});
        System.out.println(update);
    }

    private ContentValues toFields(Key key, boolean addMacAddress) {
        ContentValues values = new ContentValues();
        if (addMacAddress)
            values.put(MAC_ADDRESS_COLUMN, key.macAddress);
        values.put(USER_COLUMN, key.credentials.login());
        values.put(PASSWORD_COLUMN, key.credentials.password());
        return values;
    }
}
