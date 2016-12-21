package com.app.greenpoint;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.app.greenpoint.data.GreenpointDBAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsActivity extends PreferenceActivity {

    private EditTextPreference radio, direccion;
    private ListPreference tipoMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.settings));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addPreferencesFromResource(R.xml.activity_settings);

        final SharedPreferences mPreferences = getSharedPreferences(getString(R.string.settings_preference), MODE_PRIVATE);

        radio = (EditTextPreference) findPreference(getString(R.string.radio_key));
        radio.setSummary(getString(R.string.desc_radio) + " " + mPreferences.getInt(getString(R.string.radio_key), 200) + " m");
        radio.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newRadio = newValue.toString();
                SharedPreferences.Editor editor = mPreferences.edit();
                int r = Integer.parseInt(newRadio);
                editor.putInt(getString(R.string.radio_key), r);
                editor.apply();
                radio.setSummary(getString(R.string.desc_radio) + " " + r + " m");
                return true;
            }
        });
        direccion = (EditTextPreference) findPreference(getString(R.string.direccion_key));
        direccion.setSummary(mPreferences.getString(getString(R.string.direccion_key), ""));
        direccion.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newDireccion = newValue.toString();
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString(getString(R.string.direccion_key), newDireccion);
                editor.apply();
                direccion.setSummary(newDireccion);

                ///////////////////////////////////
                ConverterCoordenates cc = new ConverterCoordenates();
                cc.execute(newDireccion);

                /////////////////////////////////////
                return true;
            }
        });
        tipoMapa = (ListPreference) findPreference(getString(R.string.map_key));
        int prefIndex = tipoMapa.findIndexOfValue(mPreferences.getInt(getString(R.string.map_key), 1) + "");
        tipoMapa.setSummary(tipoMapa.getEntries()[prefIndex]);
        tipoMapa.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newMap = newValue.toString();
                SharedPreferences.Editor editor = mPreferences.edit();
                int type = Integer.parseInt(newMap);
                editor.putInt(getString(R.string.map_key), type);
                editor.apply();
                int prefIndex = tipoMapa.findIndexOfValue(newMap);
                tipoMapa.setSummary(tipoMapa.getEntries()[prefIndex]);
                return true;
            }
        });
    }

    public class ConverterCoordenates extends AsyncTask<String, String, String> {
        private String URL = "https://maps.googleapis.com/maps/api/geocode/json";
        private String key = getString(R.string.coordenates_api);

        private String PARAM_ADDRESS = "address";
        private String PARAM_KEY = "key";

        @Override
        protected String doInBackground(String... params) {
            String[] coordenadas = new String[2];
            String strAdress = params[0] + " ,Valencia";
            HttpURLConnection urlConnection = null;
            Uri uri = Uri.parse(URL).buildUpon()
                    .appendQueryParameter(PARAM_ADDRESS, strAdress)
                    .appendQueryParameter(PARAM_KEY, key)
                    .build();
            try {
                URL url = new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                String data = readReturnDataFromConnection(urlConnection);
                String direcc = getDataFromJson(data);
                return direcc;
            } catch (Exception e) {

            }
            return "";
        }

        private String getDataFromJson(String data) {
            //Log.d("logdata",data);
            String directionstring = "null";
            try {
                SharedPreferences sp = getSharedPreferences(getString(R.string.settings_preference), MODE_PRIVATE);
                SharedPreferences.Editor spe = sp.edit();

                JSONObject jsonObject = new JSONObject(data);
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                jsonObject = jsonArray.getJSONObject(0);

                spe.putString(getString(R.string.direccion_key), jsonObject.getString("formatted_address"));
                directionstring = jsonObject.getString("formatted_address");

                jsonObject = jsonObject.getJSONObject("geometry");
                jsonObject = jsonObject.getJSONObject("location");

                String lat = jsonObject.getString("lat");

                String lng = jsonObject.getString("lng");

                spe.putString("lat_dir", lat);
                spe.putString("lng_dir", lng);
                spe.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return directionstring;
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
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                return null;
            }
            return buffer.toString();
        }


        @Override
        protected void onPostExecute(String direcc) {
            super.onPostExecute(direcc);
            direccion.setSummary(direcc);
            GreenpointDBAdapter bd = new GreenpointDBAdapter(getApplicationContext());
            bd.cleanContenedor();
        }
    }
}
