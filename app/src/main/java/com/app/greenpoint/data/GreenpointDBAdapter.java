package com.app.greenpoint.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.app.greenpoint.data.GreenpointDBHelper.ContenedoresTable;
import com.app.greenpoint.data.GreenpointDBHelper.FavoritosTable;
import com.app.greenpoint.model.Contenedor;
import com.app.greenpoint.model.Favorito;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class GreenpointDBAdapter {

    private Context context;
    private GreenpointDBHelper dbHelper;
    private SQLiteDatabase database;

    public GreenpointDBAdapter(Context context) {
        this.context = context;
        this.dbHelper = new GreenpointDBHelper(context);
        openDB();
    }

    public void openDB() {
        try {
            database = dbHelper.getWritableDatabase();
        } catch (SecurityException e) {
            database = dbHelper.getReadableDatabase();
        }
    }

    public long insertFavorito(int idFavorito, Contenedor c) {
        ContentValues values = new ContentValues();

        values.put(FavoritosTable.ID_FAVORITO, idFavorito);
        values.put(FavoritosTable.ID_CONTENEDOR, c.getId());
        values.put(FavoritosTable.TIPO_CONTNENEDOR, c.getTipo());
        values.put(FavoritosTable.DIR_CONTENEDOR, c.getDireccion());
        LatLng position = c.getLocation();
        values.put(FavoritosTable.LAT_CONTENEDOR, position.latitude);
        values.put(FavoritosTable.LON_CONTENEDOR, position.longitude);

        return database.insert(FavoritosTable.TABLE_NAME, null, values);
    }

    public int queryFavorito(Contenedor c) {
        int idFavorito = -1;
        String where = FavoritosTable.ID_CONTENEDOR+" =? and "+FavoritosTable.TIPO_CONTNENEDOR+" =?";
        String values[] = {c.getId()+"", c.getTipo()+""};
        Cursor cursor = database.query(FavoritosTable.TABLE_NAME, FavoritosTable.COLUMNS_RESULT, where, values, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int colIdFavorito = cursor.getColumnIndex(FavoritosTable.ID_FAVORITO);
            do {
                idFavorito = cursor.getInt(colIdFavorito);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return idFavorito;
    }

    public ArrayList<Favorito> recuperarFavoritos (){
        ArrayList<Favorito> favoritos = new ArrayList<Favorito>();
        Cursor cursor = database.query(FavoritosTable.TABLE_NAME,FavoritosTable.COLUMNS_RESULT,null,null,null,null,null);
        if (cursor != null && cursor.moveToFirst()){
            int colIdFavorito = cursor.getColumnIndex(FavoritosTable.ID_FAVORITO);
            int colIdContenedor = cursor.getColumnIndex(FavoritosTable.ID_CONTENEDOR);
            int colTipoContenedor = cursor.getColumnIndex(FavoritosTable.TIPO_CONTNENEDOR);
            int colDirContenedor = cursor.getColumnIndex(FavoritosTable.DIR_CONTENEDOR);
            int colLatContenedor = cursor.getColumnIndex(FavoritosTable.LAT_CONTENEDOR);
            int colLonContenedor = cursor.getColumnIndex(FavoritosTable.LON_CONTENEDOR);
            do{
                Favorito f = new Favorito();
                f.setIdFavorito(cursor.getInt(colIdFavorito));
                Contenedor c = new Contenedor();
                c.setId(cursor.getInt(colIdContenedor));
                c.setTipo(cursor.getInt(colTipoContenedor));
                c.setDireccion(cursor.getString(colDirContenedor));
                c.setLocation(new LatLng(cursor.getDouble(colLatContenedor),cursor.getDouble(colLonContenedor)));
                f.setIdFavorito(cursor.getInt(colIdFavorito));
                f.setContenedor(c);
                favoritos.add(f);
            }while (cursor.moveToNext());
        }
        cursor.close();

        return favoritos;
    }

    public int deleteFavorito(int id){
        String where = FavoritosTable.ID_FAVORITO+" =?";
        String[] values = {id+""};
        return database.delete(FavoritosTable.TABLE_NAME, where, values);
    }

    public int deleteAllFavoritos() {
        return database.delete(FavoritosTable.TABLE_NAME, null, null);
    }

    public void insertContenedor(Contenedor c) {
        ContentValues values = new ContentValues();

        values.put(ContenedoresTable.ID_CONTENEDOR, c.getId());
        values.put(ContenedoresTable.TIPO_CONTNENEDOR, c.getTipo());
        values.put(ContenedoresTable.DIR_CONTENEDOR, c.getDireccion());
        LatLng position = c.getLocation();
        values.put(ContenedoresTable.LAT_CONTENEDOR, position.latitude);
        values.put(ContenedoresTable.LON_CONTENEDOR, position.longitude);

        database.insert(ContenedoresTable.TABLE_NAME, null, values);
    }

    public void cleanContenedor() {
        database.delete(ContenedoresTable.TABLE_NAME,null,null);
    }

    public boolean contenedoresIsClean() {
        Cursor c=database.rawQuery("SELECT "+ContenedoresTable.ID_CONTENEDOR+" FROM "+ContenedoresTable.TABLE_NAME,null);
        if(c!=null && c.getCount()>0){
            c.close();
            return false;
        }else{
            c.close();
            return true;
        }

    }

    public ArrayList<Contenedor> returnContenedores() {
        Log.d("basededatos","leiendo contenedores");
        Cursor cursor= database.query(ContenedoresTable.TABLE_NAME,null,null,null,null,null,null);
        ArrayList<Contenedor> contenedores=new ArrayList<Contenedor>();
        Log.d("totalcontenedore",cursor.getCount()+"");
        Contenedor contenedor;

        int colIdContenedor = cursor.getColumnIndex(ContenedoresTable.ID_CONTENEDOR);
        int colTipoContenedor = cursor.getColumnIndex(ContenedoresTable.TIPO_CONTNENEDOR);
        int colDirContenedor = cursor.getColumnIndex(ContenedoresTable.DIR_CONTENEDOR);
        int colLatContenedor = cursor.getColumnIndex(ContenedoresTable.LAT_CONTENEDOR);
        int colLonContenedor = cursor.getColumnIndex(ContenedoresTable.LON_CONTENEDOR);
        if(cursor!=null && cursor.moveToFirst()) {
            do {
                contenedor = new Contenedor();
                contenedor.setId(cursor.getInt(colIdContenedor));
                contenedor.setDireccion(cursor.getString(colDirContenedor));
                contenedor.setTipo(cursor.getInt(colTipoContenedor));

                double latitud = cursor.getDouble(colLatContenedor);
                double longitude = cursor.getDouble(colLonContenedor);
                LatLng position = new LatLng(latitud, longitude);
                contenedor.setLocation(position);
                contenedores.add(contenedor);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return contenedores;
    }


}
