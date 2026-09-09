package com.example.mystudyplan

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Course(
    val id: Int,
    val name: String,
    val duration: String,
    val description: String,
    val tracks: String
)

class DBHandler(context: Context?) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT,"
                + DURATION_COL + " TEXT,"
                + DESCRIPTION_COL + " TEXT,"
                + TRACKS_COL + " TEXT)")
        db.execSQL(query)
    }

    fun addNewCourse(
        courseName: String?,
        courseDuration: String?,
        courseDescription: String?,
        courseTracks: String?
    ) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(NAME_COL, courseName)
        values.put(DURATION_COL, courseDuration)
        values.put(DESCRIPTION_COL, courseDescription)
        values.put(TRACKS_COL, courseTracks)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllCourses(): List<Course> {
        val courseList = mutableListOf<Course>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME ORDER BY $ID_COL DESC",
            null
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL))
                val duration = cursor.getString(cursor.getColumnIndexOrThrow(DURATION_COL))
                val description = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION_COL))
                val tracks = cursor.getString(cursor.getColumnIndexOrThrow(TRACKS_COL))
                courseList.add(Course(id, name, duration, description, tracks))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return courseList
    }

    fun deleteCourse(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$ID_COL = ?", arrayOf(id.toString()))
        db.close()
    }

    // UPDATE: overwrite an existing row by id. This is Option B.
    fun updateCourse(
        id: Int,
        courseName: String?,
        courseDuration: String?,
        courseDescription: String?,
        courseTracks: String?
    ) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(NAME_COL, courseName)
        values.put(DURATION_COL, courseDuration)
        values.put(DESCRIPTION_COL, courseDescription)
        values.put(TRACKS_COL, courseTracks)
        db.update(TABLE_NAME, values, "$ID_COL = ?", arrayOf(id.toString()))
        db.close()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        private const val DB_NAME = "coursedb"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "mycourses"
        private const val ID_COL = "id"
        private const val NAME_COL = "name"
        private const val DURATION_COL = "duration"
        private const val DESCRIPTION_COL = "description"
        private const val TRACKS_COL = "tracks"
    }
}