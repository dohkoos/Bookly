package com.codemany.bookly;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class BookDao {
    private static BookDao dao;
    private SQLiteDatabase database;

    private BookDao(Context context) {
        database = context.openOrCreateDatabase("bookly", Context.MODE_PRIVATE, null);
        createTables();
    }

    public static void initBookDao(Context context) {
        if (dao == null) {
            dao = new BookDao(context);
        }
    }

    public static BookDao getInstance() {
        return dao;
    }

    private void createTables() {
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS books("
                + " id integer PRIMARY KEY AUTOINCREMENT,"
                + " title text,"
                + " author text,"
                + " isbn char(13),"
                + " summary text,"
                + " image_url varchar(255),"
                + " publisher text"
                + ");");
    }

    public void add(Book book) {
        SQLiteStatement statement = database.compileStatement(
                "INSERT INTO books(title, author, isbn, summary, image_url, publisher)"
                + " VALUES (?, ?, ?, ?, ?, ?)");
        statement.bindString(1, book.getTitle());
        statement.bindString(2, book.getAuthor());
        statement.bindString(3, book.getIsbn());
        statement.bindString(4, book.getSummary());
        statement.bindString(5, book.getImageUrl());
        statement.bindString(6, book.getPublisher());
        statement.execute();
        statement.close();
    }

    public Book get(String isbn) {
        Book book = null;

        Cursor cursor = database.rawQuery(
                "SELECT title, author, isbn, summary, image_url, publisher FROM books"
                + " WHERE isbn =" + isbn,
                new String[] {});

        if (cursor.moveToFirst()) {
            book = new Book();
            book.setTitle(cursor.getString(0));
            book.setAuthor(cursor.getString(1));
            book.setIsbn(cursor.getString(2));
            book.setSummary(cursor.getString(3));
            book.setImageUrl(cursor.getString(4));
            book.setPublisher(cursor.getString(5));
        }

        cursor.close();

        return book;
    }

    public JSONArray list() {
        JSONArray array = new JSONArray();

        Cursor cursor = database.rawQuery(
                "SELECT title, author, isbn, summary, image_url, publisher FROM books",
                new String[] {});

        while (cursor.moveToNext()) {
            JSONObject object = new JSONObject();
            try {
                object.put("title", cursor.getString(0));
                object.put("author", cursor.getString(1));
                object.put("isbn", cursor.getString(2));
                object.put("summary", cursor.getString(3));
                object.put("image_url", cursor.getString(4));
                object.put("publisher", cursor.getString(5));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            array.put(object);
        }

        cursor.close();

        return array;
    }

    public void delete(String isbn) {
        SQLiteStatement statement = database
                .compileStatement("DELETE FROM books WHERE isbn = ?");
        statement.bindString(1, isbn);
        statement.execute();
        statement.close();
    }

    public void deleteAll() {
        SQLiteStatement statement = database.compileStatement("DELETE FROM books");
        statement.execute();
        statement.close();
    }
}
