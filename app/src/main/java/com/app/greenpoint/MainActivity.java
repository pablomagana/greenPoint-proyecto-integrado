package com.app.greenpoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.app.greenpoint.adapter.ContenedorPagerAdapter;
import com.app.greenpoint.data.GreenpointDBAdapter;
import com.app.greenpoint.model.Contenedor;
import com.app.greenpoint.model.Favorito;
import com.app.greenpoint.model.MarkerContenedor;
import com.google.android.gms.maps.model.LatLng;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int LOCATION_SETTING_CODE = 1;
    public static final int CAMERA_CODE = 100;

    private Toolbar toolbar;
    private MapFragment mapFragment;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private int tipo = -1;
    private Contenedor contenedor;
    private ArrayList<MarkerContenedor> contenedores = new ArrayList<MarkerContenedor>();

    private NosotrosFragment nosotros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(toggle);
        boolean datosMovil = Conexiones.getInstance(getApplicationContext()).conectadoMovil();
        boolean wifi = Conexiones.getInstance(getApplicationContext()).conectadoWifi();

        toggle.syncState();

        mapFragment = new MapFragment();

        showMapFragment();

        if (!datosMovil && !wifi) {
            showInternetDisabled();
        }

        if (!isGPSEnabled()) {
            showGPSDisabledAlertToUser();
        }

        nosotros=new NosotrosFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean datosMovil = Conexiones.getInstance(getApplicationContext()).conectadoMovil();
        boolean wifi = Conexiones.getInstance(getApplicationContext()).conectadoWifi();

        SharedPreferences sp = getSharedPreferences(getString(R.string.user_preference), MODE_PRIVATE);

        Boolean login = sp.getBoolean(getString(R.string.user_preference_login), false);
        if (login) {
            if (wifi || datosMovil) {
                GreenpointDBAdapter dbAdapter = new GreenpointDBAdapter(getApplicationContext());
                dbAdapter.deleteAllFavoritos();
                ConsultaFavoritos consultaFavoritos = new ConsultaFavoritos();
                String apiKey = sp.getString(getString(R.string.user_preference_key), null);
                consultaFavoritos.execute(apiKey);
            }
        }
        updateUi(sp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOCATION_SETTING_CODE:
                if (!isGPSEnabled())
                    showGPSDisabledAlertToUser();
                break;
            case CAMERA_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    ContenedorPagerAdapter adapter = (ContenedorPagerAdapter) mapFragment.getInfoContenedorFragment().getViewPager().getAdapter();
                    AlertaFragment alertaFragment = (AlertaFragment) adapter.getItem(1);
                    alertaFragment.loadImagePhoto(bitmap);
                }
        }
    }

    public void startCameraApp() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_CODE);
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.gps_disabled))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.accept),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(callGPSSettingIntent, MainActivity.LOCATION_SETTING_CODE);
                            }
                        });
        alertDialogBuilder.setNegativeButton(getString(R.string.decline),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void showInternetDisabled() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.internet_disabled))
                .setPositiveButton(getString(R.string.accept), null);
        alertDialogBuilder.setNegativeButton(getString(R.string.decline),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nosotros:
                getSupportFragmentManager().beginTransaction().add(nosotros,"nosotrosfragment").commit();
                break;
            case R.id.nav_papelera:
                getSupportFragmentManager().beginTransaction().remove(nosotros).commit();
                tipo = 0;
                mapFragment.setTipo(tipo);
                break;
            case R.id.nav_organico:
                getSupportFragmentManager().beginTransaction().remove(nosotros).commit();
                tipo = 1;
                mapFragment.setTipo(tipo);
                break;
            case R.id.nav_carton:
                getSupportFragmentManager().beginTransaction().remove(nosotros).commit();
                tipo = 2;
                mapFragment.setTipo(tipo);
                break;
            case R.id.nav_plastico:
                getSupportFragmentManager().beginTransaction().remove(nosotros).commit();
                tipo = 3;
                mapFragment.setTipo(tipo);
                break;
            case R.id.nav_vidrio:
                getSupportFragmentManager().beginTransaction().remove(nosotros).commit();
                tipo = 4;
                mapFragment.setTipo(tipo);
                break;
            case R.id.nav_aceite:
                getSupportFragmentManager().beginTransaction().remove(nosotros).commit();
                tipo = 5;
                mapFragment.setTipo(tipo);
                break;
            case R.id.nav_pilas:
                getSupportFragmentManager().beginTransaction().remove(nosotros).commit();
                tipo = 6;
                mapFragment.setTipo(tipo);
                break;
            case R.id.nav_my_closed:
                getSupportFragmentManager().beginTransaction().remove(nosotros).commit();
                SharedPreferences sp = getSharedPreferences(getString(R.string.settings_preference), MODE_PRIVATE);
                if (!sp.getString("lat_dir", "null").equals("null")) {
                    tipo = 8;
                    mapFragment.setTipo(tipo);
                    mapFragment.ubicarMiCasa();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.faltadireccion), Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.nav_profile_menu:
                getSupportFragmentManager().beginTransaction().remove(nosotros).commit();
                SharedPreferences sp2 = getSharedPreferences(getString(R.string.user_preference), MODE_PRIVATE);
                Boolean login = sp2.getBoolean(getString(R.string.user_preference_login), false);
                Intent intent;
                if (login) {
                    intent = new Intent(getApplicationContext(), ProfileActivity.class);
                } else {
                    intent = new Intent(getApplicationContext(), LoginActivity.class);
                }
                startActivity(intent);
                break;
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().remove(nosotros).commit();
                Intent sett = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(sett);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateUi(SharedPreferences sp) {
        if (!sp.getBoolean(getString(R.string.user_preference_login), false)) {
            GreenpointDBAdapter dbAdapter = new GreenpointDBAdapter(getApplicationContext());
            dbAdapter.deleteAllFavoritos();
        }

        TextView nombrePerfil = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nombreperfilmenu);
        nombrePerfil.setText(sp.getString(getString(R.string.user_preference_name), getString(R.string.guest_user)));

        TextView emailPerfil = (TextView) navigationView.getHeaderView(0).findViewById(R.id.correoperfilmenu);
        emailPerfil.setText(sp.getString(getString(R.string.user_preference_mail), ""));

        CircularImageView iconoPerfil = (CircularImageView) navigationView.getHeaderView(0).findViewById(R.id.imgperfilmenu);
        String imgEncoded = sp.getString(getString(R.string.user_preference_image), "");
        if (!imgEncoded.equals("") && !imgEncoded.equals("null")) {
            byte[] data = Base64.decode(imgEncoded, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            iconoPerfil.setImageBitmap(bitmap);
        } else {
            iconoPerfil.setImageResource(R.drawable.logo2);
        }
    }

    public void showMapFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_main, mapFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (!drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.layout_main);
            if (f.equals(mapFragment)) {
                if (!mapFragment.isDetallesHide()) {
                    mapFragment.hideDetalles();
                } else {
                    if (mapFragment.isInfoShowed()) {
                        mapFragment.hideInfoContenedor();
                    } else super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
            try{//ocultar NosotrosFragment
                getSupportFragmentManager().beginTransaction().remove(nosotros).commit();
            }catch (Exception e){

            }
        } else {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public MapFragment getMapFragment() {
        return mapFragment;
    }

    public ArrayList<MarkerContenedor> getContenedores() {
        return contenedores;
    }

    public void setContenedores(ArrayList<MarkerContenedor> contenedores) {
        this.contenedores = contenedores;
    }

    public Contenedor getContenedor() {
        return contenedor;
    }

    public void setContenedor(Contenedor contenedor) {
        this.contenedor = contenedor;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    private class ConsultaFavoritos extends AsyncTask<String, Void, ArrayList<Favorito>> {

        private final String JSON_ESTADO = "estado";
        private final String JSON_FAVORITOS = "favoritos";
        private final String JSON_IDCONTENEDOR = "idContenedor";
        private final String JSON_TIPOCONTENEDOR = "tipo";
        private final String JSON_DIRECCION = "direccion";
        private final String JSON_LATITUD = "lat";
        private final String JSON_LONGITUD = "lon";
        private final String JOSN_IDFAVORITO = "idFavorito";

        private final String URL = "http://raspinico.ddns.net/ApiGreenpoint/v1/favoritos";

        @Override
        protected ArrayList<Favorito> doInBackground(String... params) {
            String claveApi = params[0];
            HttpURLConnection urlConnection = null;
            ArrayList<Favorito> favoritos = new ArrayList<Favorito>();
            try {
                URL url = new URL(URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", claveApi);
                urlConnection.connect();
                String data = readReturnDataFromConnection(urlConnection);
                favoritos = getDataFromJson(data);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return favoritos;
        }

        @Override
        protected void onPostExecute(ArrayList<Favorito> favoritos) {
            super.onPostExecute(favoritos);
            if (!favoritos.isEmpty()) {
                GreenpointDBAdapter dbAdapter = new GreenpointDBAdapter(getApplicationContext());
                for (Favorito f : favoritos) {
                    dbAdapter.insertFavorito(f.getIdFavorito(), f.getContenedor());
                }
            }
        }

        private String readReturnDataFromConnection(HttpURLConnection connection) throws IOException {
            InputStream inputStream = connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = null;
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            if (buffer.length() == 0) {
                return null;
            }
            return buffer.toString();
        }

        private ArrayList<Favorito> getDataFromJson(String data) {
            ArrayList<Favorito> favoritos = new ArrayList<Favorito>();
            try {
                JSONObject jsonObject = new JSONObject(data);
                int estado = jsonObject.getInt(JSON_ESTADO);
                if (estado == 1) {
                    JSONArray arrayFavorito = jsonObject.getJSONArray(JSON_FAVORITOS);
                    for (int i = 0; i < arrayFavorito.length(); i++) {
                        JSONObject jsonFavorito = arrayFavorito.getJSONObject(i);
                        Contenedor c = new Contenedor();
                        c.setId(jsonFavorito.getInt(JSON_IDCONTENEDOR));
                        c.setTipo(jsonFavorito.getInt(JSON_TIPOCONTENEDOR));
                        c.setDireccion(jsonFavorito.getString(JSON_DIRECCION));
                        double lat = jsonFavorito.getDouble(JSON_LATITUD);
                        double lon = jsonFavorito.getDouble(JSON_LONGITUD);
                        c.setLocation(new LatLng(lat, lon));
                        Favorito f = new Favorito();
                        f.setIdFavorito(jsonFavorito.getInt(JOSN_IDFAVORITO));
                        f.setContenedor(c);
                        favoritos.add(f);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return favoritos;
        }
    }
}


