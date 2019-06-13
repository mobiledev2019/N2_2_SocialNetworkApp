package com.tad.asannet.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tad.asannet.models.Notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tad on 2/3/18.
 */

public class NotificationsHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "asannet_Notifications_db";
    public static final String NOTIFICATION_TABLE_NAME = "notifications";
    public static final String NOTIFICATION_COLUMN_ID = "id";
    public static final String NOTIFICATION_COLUMN_USERIMAGE = "user_image";
    public static final String NOTIFICATION_COLUMN_TITLE = "title";
    public static final String NOTIFICATION_COLUMN_BODY = "body";
    public static final String NOTIFICATION_COLUMN_TIMESTAMP = "timestamp";

    private HashMap hp;

    public NotificationsHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE notifications " + "(id integer PRIMARY KEY,user_image text,title text,body text,timestamp text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notifications");
        onCreate(db);
    }

    public void insertContact( String user_image, String title, String body,String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_image", user_image);
        contentValues.put("title", title);
        contentValues.put("body", body);
        contentValues.put("timestamp", timestamp);
        db.insert("notifications", null, contentValues);
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from notifications where id="+id+"", null );
    }

    public List<Notification> getAllNotifications(){

        List<Notification> notifications=new ArrayList<>();

        String selectQuery = "SELECT * FROM notifications ORDER BY "+NOTIFICATION_COLUMN_TIMESTAMP+" DESC";
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do{

                Notification notification=new Notification(cursor.getString(cursor.getColumnIndex(NOTIFICATION_COLUMN_USERIMAGE)),
                        cursor.getString(cursor.getColumnIndex(NOTIFICATION_COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(NOTIFICATION_COLUMN_BODY)),
                        cursor.getString(cursor.getColumnIndex(NOTIFICATION_COLUMN_TIMESTAMP)));
                notifications.add(notification);

            }while(cursor.moveToNext());
        }

        db.close();
        return notifications;
    }

    public int getCount(){
        String countQuery="SELECT * FROM notifications";
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery(countQuery,null);

        int count=cursor.getCount();
        db.close();
        return count;

    }

    public Integer deleteContact (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("notifications",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

}