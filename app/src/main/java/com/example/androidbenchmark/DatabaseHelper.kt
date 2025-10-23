package com.example.androidbenchmark

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

object DbContract {
    object Entry : BaseColumns {
        const val TABLE_NAME = "test_entries"
        const val COLUMN_NAME_DATA = "data"
    }
}

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun clearTable(db: SQLiteDatabase) {
        db.execSQL(SQL_DELETE_ENTRIES)
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Benchmark.db"

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${DbContract.Entry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${DbContract.Entry.COLUMN_NAME_DATA} TEXT)"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${DbContract.Entry.TABLE_NAME}"
    }
}
