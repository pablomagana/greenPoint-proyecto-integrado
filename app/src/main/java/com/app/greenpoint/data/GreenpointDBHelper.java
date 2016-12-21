package com.app.greenpoint.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class GreenpointDBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "greenpointdb";
    public static final int DB_VERSION = 1;

    public GreenpointDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    public GreenpointDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FavoritosTable.CREATE_TABLE);
        db.execSQL(ContenedoresTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(FavoritosTable.DROP_TABLE);
        db.execSQL(ContenedoresTable.DROP_TABLE);
        onCreate(db);
    }

    public static final class FavoritosTable implements BaseColumns {

        public static final String TABLE_NAME = "favoritos";

        public static final String ID_FAVORITO = "id_favorito";
        public static final String ID_CONTENEDOR = "id_contenedor";
        public static final String TIPO_CONTNENEDOR = "tipo_contenedor";
        public static final String DIR_CONTENEDOR = "dir_contenedor";
        public static final String LAT_CONTENEDOR = "lat_contenedor";
        public static final String LON_CONTENEDOR = "lon_contenedor";

        public static final String[] COLUMNS_RESULT = {_ID, ID_FAVORITO, ID_CONTENEDOR, TIPO_CONTNENEDOR, DIR_CONTENEDOR, LAT_CONTENEDOR, LON_CONTENEDOR};

        public static final String CREATE_TABLE = "create table " + TABLE_NAME +
                "("+_ID+" integer primary key autoincrement, " +
                ID_FAVORITO + " integer not null, " +
                ID_CONTENEDOR + " integer not null, " +
                TIPO_CONTNENEDOR + " integer not null, " +
                DIR_CONTENEDOR + " varchar(140) not null, " +
                LAT_CONTENEDOR + " double not null, " +
                LON_CONTENEDOR + " double not null " +
                ");";
        public static final String DROP_TABLE = "drop table if exists " + TABLE_NAME + ";";

    }

    public static final class ContenedoresTable implements BaseColumns {

        public static final String TABLE_NAME = "contenedores";

        public static final String ID_CONTENEDOR = "id_contenedor";
        public static final String TIPO_CONTNENEDOR = "tipo_contenedor";
        public static final String DIR_CONTENEDOR = "dir_contenedor";
        public static final String LAT_CONTENEDOR = "lat_contenedor";
        public static final String LON_CONTENEDOR = "lon_contenedor";

        public static final String[] COLUMNS_RESULT = {_ID, ID_CONTENEDOR, TIPO_CONTNENEDOR, DIR_CONTENEDOR, LAT_CONTENEDOR, LON_CONTENEDOR};

        public static final String CREATE_TABLE = "create table " + TABLE_NAME +
                "("+_ID+" integer primary key autoincrement, " +
                ID_CONTENEDOR + " integer not null, " +
                TIPO_CONTNENEDOR + " integer not null, " +
                DIR_CONTENEDOR + " varchar(140) not null, " +
                LAT_CONTENEDOR + " double not null, " +
                LON_CONTENEDOR + " double not null " +
                ");";

        public static final String DROP_TABLE = "drop table if exists " + TABLE_NAME + ";";

    }
}
