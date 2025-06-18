package ru.crmkurgan.main.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.crmkurgan.main.Constants

class ObjectsDBSQLiteHelper(context: Context): SQLiteOpenHelper(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(Constants.CREATE_DATABASE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.let {
            it.execSQL(Constants.DROP_DATABASE)
            onCreate(it)
        }
    }
}