package org.example.asteroides;

import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlmacenPuntuacionesSQLite extends SQLiteOpenHelper implements
	AlmacenPuntuaciones {
    // MŽtodos de SQLiteOpenHelper
    public AlmacenPuntuacionesSQLite(Context context) {
	super(context, "puntuaciones", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
	db.execSQL("CREATE TABLE puntuaciones ("
		+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ "puntos INTEGER, nombre TEXT, fecha LONG)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	// En caso de una nueva versi—n habr’a que actualizar las tablas
    }

    // MŽtodos de AlmacenPuntuaciones
    public void guardarPuntuacion(int puntos, String nombre, long fecha) {
	SQLiteDatabase db = getWritableDatabase();
	db.execSQL("INSERT INTO puntuaciones VALUES ( null, " + puntos + ", '"
		+ nombre + "', " + fecha + ")");
    }

    public Vector<String> listaPuntuaciones(int cantidad) {
	Vector<String> result = new Vector<String>();
	SQLiteDatabase db = getReadableDatabase();
	// Cursor cursor = db.rawQuery("SELECT puntos, nombre FROM "
	// + "puntuaciones ORDER BY puntos DESC LIMIT " + cantidad, null);
	String[] CAMPOS = { "puntos", "nombre" };
	Cursor cursor = db.query("puntuaciones", CAMPOS, null, null, null,
		null, "puntos", Integer.toString(cantidad));

	while (cursor.moveToNext()) {
	    result.add(cursor.getInt(0) + " " + cursor.getString(1));
	}
	cursor.close();
	return result;
    }
}