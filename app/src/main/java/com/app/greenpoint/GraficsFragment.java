package com.app.greenpoint;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.app.greenpoint.charts.ColumnChart;
import com.app.greenpoint.charts.ComboChart;
import com.app.greenpoint.charts.PieChart;
import com.app.greenpoint.model.Anual;
import com.app.greenpoint.model.Contenedor;
import com.app.greenpoint.model.Datos;
import com.app.greenpoint.model.Favorito;
import com.app.greenpoint.model.Mensual;
import com.app.greenpoint.model.Reciclaje;
import com.app.greenpoint.model.Semanal;
import com.google.android.gms.fitness.request.DailyTotalRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class GraficsFragment extends Fragment {

    private PieChart pieChart;
    private ColumnChart columnChart;
    private ComboChart comboChart;

    public GraficsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grafics, container, false);

        SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.user_preference), Context.MODE_PRIVATE);
        String claveApi = sp.getString(getString(R.string.user_preference_key), "");

        pieChart = (PieChart) view.findViewById(R.id.pie_chart);
        columnChart = (ColumnChart) view.findViewById(R.id.column_chart);
        comboChart = (ComboChart) view.findViewById(R.id.combo_chart);

        DescargaEstadisticas descargaEstadisticas = new DescargaEstadisticas();
        descargaEstadisticas.execute(claveApi);

        return view;
    }

    private class DescargaEstadisticas extends AsyncTask<String, Void, Datos[]> {

        private final String URL = "http://raspinico.ddns.net/ApiGreenpoint/v1/reciclaje";

        private final String JSON_DATOS="datos";
        private final String JSON_ANUAL="anual";
        private final String JSON_MENSUAL="mensual";
        private final String JSON_SEMANAL="semanal";
        private final String JSON_MES="mes";
        private final String JSON_TIPO="tipo";
        private final String JSON_CANTIDAD="cantidad";


        @Override
        protected Datos[] doInBackground(String... params) {
            String claveApi = params[0];
            Datos[] datos = null;
            HttpURLConnection urlConnection = null;
            try {
                java.net.URL url = new URL(URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", claveApi);
                urlConnection.connect();
                String data = readReturnDataFromConnection(urlConnection);
                datos = getDataFromJson(data);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return datos;
        }

        @Override
        protected void onPostExecute(Datos[] datoses) {
            super.onPostExecute(datoses);
            if (datoses != null) {
                pieChart.create((Semanal) datoses[2]);
                columnChart.create((Mensual) datoses[1]);
                comboChart.create((Anual) datoses[0]);
            }
        }

        private Datos[] getDataFromJson(String data) {
            Datos[] datos = new Datos[3];
            Anual anual = new Anual();
            Mensual mensual = new Mensual();
            Semanal semanal = new Semanal();
            try {
                JSONObject jsonObject = new JSONObject(data);
                jsonObject = jsonObject.getJSONObject(JSON_DATOS);
                JSONObject jsonAnual = jsonObject.getJSONObject(JSON_ANUAL);
                for (int i = 1; i <= 12; i++) {
                    ArrayList<Reciclaje> mes = new ArrayList<>();
                    JSONArray jsonNumMes = jsonAnual.getJSONArray(i+"");
                    for (int j = 0;j < jsonNumMes.length(); j++) {
                        JSONObject datosMes = jsonNumMes.getJSONObject(j);
                        String tipo = datosMes.getString(JSON_TIPO);
                        String cantidad = datosMes.getString(JSON_CANTIDAD);
                        Reciclaje r = new Reciclaje(tipo, Double.parseDouble(cantidad));
                        mes.add(r);
                    }
                    anual.setDatosMes(i-1, mes);
                }
                JSONObject jsonMensual = jsonObject.getJSONObject(JSON_MENSUAL);
                String mes = jsonMensual.getString(JSON_MES);
                mensual.setNumMes(mes);
                JSONArray datosMes = jsonMensual.getJSONArray(JSON_DATOS);
                for (int i = 0; i < datosMes.length(); i++) {
                    JSONObject jsonDatos = datosMes.getJSONObject(i);
                    String tipo = jsonDatos.getString(JSON_TIPO);
                    String cantidad = jsonDatos.getString(JSON_CANTIDAD);
                    Reciclaje r = new Reciclaje(tipo, Double.parseDouble(cantidad));
                    mensual.getDatos().add(r);
                }
                JSONArray jsonSemanal = jsonObject.getJSONArray(JSON_SEMANAL);
                for (int i = 0; i < jsonSemanal.length(); i++) {
                    JSONObject jsonDatos = jsonSemanal.getJSONObject(i);
                    String tipo = jsonDatos.getString(JSON_TIPO);
                    String cantidad = jsonDatos.getString(JSON_CANTIDAD);
                    Reciclaje r = new Reciclaje(tipo, Double.parseDouble(cantidad));
                    semanal.getDatos().add(r);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            datos[0] = anual;
            datos[1] = mensual;
            datos[2] = semanal;
            return datos;
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
    }

}
