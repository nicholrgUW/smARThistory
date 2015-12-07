package com.kell.android.smarthistory.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import com.kell.android.smarthistory.MainActivity;
import com.kell.android.smarthistory.model.ArtCard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robbie on 11/13/2015.
 */
public class CardInfoDB {
    private static final int DB_VERSION = 3;
    private static final String DB_NAME = "Card.db";
    private final SQLiteDatabase mSQLiteDatabase;

    public CardInfoDB(Context context) {
        CardInfoDBHelper userInfoDBHelper = new CardInfoDBHelper(
                context);
        mSQLiteDatabase = userInfoDBHelper.getWritableDatabase();
    }

    public byte[] getImage(String user, String list, String artist, String title) {
        String[] columns = {"image"};
        String[] wheres = {user, list, artist, title};
        String whereCol = "User = ? AND List = ? AND Artist = ? AND Title = ?";
        Cursor c = mSQLiteDatabase.query(
                "Card",                                 // The table to query
                columns,                                // The columns to return
                whereCol,                               // The columns for the WHERE clause
                wheres,                                 // The values for the WHERE clause
                null,                                   // don't group the rows
                null,                                   // don't filter by row groups
                null                                    // The sort order
        );
        c.moveToFirst();
        int count = c.getCount();
        if (count != 1) {
            c.close();
            return null;
        } else {
            byte[] image = c.getBlob(0);
            if (image != null) {
                c.close();
                return image;
            } else {
                Log.d(MainActivity.APP_TAG, "failed to load image " + count);
                c.close();
                return null;
            }
        }
    }

    public boolean updateImage(String user, String list, String artist, String title, Bitmap image) {
        String selectStr = "user=? AND list=? AND artist=? AND title=?";
        String[] selectArgs = new String[]{user, list, artist, title};
        ContentValues contentValues = new ContentValues();

        //from http://stackoverflow.com/questions/17839388/creating-a-scaled-bitmap-with-createscaledbitmap-in-android
        final int maxSize = 540;
        int outWidth;
        int outHeight;
        int inWidth = image.getWidth();
        int inHeight = image.getHeight();
        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        Bitmap resizedImage = Bitmap.createScaledBitmap(image, outWidth, outHeight, false);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resizedImage.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        byte[] imageAsBytes = outputStream.toByteArray();

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        contentValues.put("image", imageAsBytes);
        long rowId = mSQLiteDatabase.update("Card", contentValues, selectStr, selectArgs);
        return rowId != -1;

    }

    public boolean insertCard(String user, String list, String artist, String title, String year,
                              String facts, String url) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("user", user);
        contentValues.put("list", list);
        contentValues.put("artist", artist);
        contentValues.put("title", title);
        contentValues.put("year", year);
        contentValues.put("facts", facts);
        contentValues.put("url", url);
        contentValues.put("favorite", false);

        long rowId = mSQLiteDatabase.insert("Card", null, contentValues);
        return rowId != -1;
    }

    public void closeDB() {
        mSQLiteDatabase.close();
    }

    @SuppressWarnings("Convert2Diamond")
    public List<ArtCard> selectCards(String theUser, String theCardList) {

        String[] columns = {
                "user", "list", "artist", "title", "year", "facts", "image", "favorite"};

        String[] wheres = {theUser, theCardList};
        Cursor c = mSQLiteDatabase.query(
                "Card",                                 // The table to query
                columns,                                // The columns to return
                "User = ? AND CardList = ?",            // The columns for the WHERE clause
                wheres,                                 // The values for the WHERE clause
                null,                                   // don't group the rows
                null,                                   // don't filter by row groups
                null                                    // The sort order
        );
        c.moveToFirst();
        List<ArtCard> list = new ArrayList<>();
        for (int i = 0; i < c.getCount(); i++) {
            String artist = c.getString(0);
            String title = c.getString(1);
            String year = c.getString(2);
            String info = c.getString(3);
            String image = c.getString(4);
            ArtCard.ITEMS.add(new ArtCard.CardInfo(title, artist, year, info, image));
            c.moveToNext();
        }
        c.close();
        return list;
    }

    public void deleteCardByArtistTitle(String artist, String title) {
        String args[] = {artist, title};
        mSQLiteDatabase.delete("Card", "artist = ? and title = ?;", args);
    }

    public void deleteUserListCards(String user, String list) {
        String args[] = {user, list};
        mSQLiteDatabase.delete("Card", "user = ? and list = ?;", args);
    }

    class CardInfoDBHelper extends SQLiteOpenHelper {
        private static final String CREATE_CARD_SQL = "CREATE TABLE IF NOT EXISTS Card " +
                "(" +
                "user TEXT, list TEXT, artist TEXT, title TEXT, year TEXT, facts TEXT," +
                "url TEXT, image BLOB, favorite INTEGER, PRIMARY KEY(user, list, artist, title)" +
                ")";

        private static final String DROP_CARD_SQL =
                //update
                "DROP TABLE IF EXISTS Card";

        public CardInfoDBHelper(Context context) {
            super(context, CardInfoDB.DB_NAME, null, CardInfoDB.DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_CARD_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(DROP_CARD_SQL);
            onCreate(sqLiteDatabase);
        }
    }
}
