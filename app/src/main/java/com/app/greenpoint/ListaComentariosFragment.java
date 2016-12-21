package com.app.greenpoint;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.greenpoint.adapter.AdaptadorLista;
import com.app.greenpoint.model.Comentario;
import com.app.greenpoint.model.Contenedor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ListaComentariosFragment extends Fragment {

    private TextView vacio;
    private RecyclerView list;
    private ImageView enviar;
    private EditText texto;

    public ListaComentariosFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista_comentarios, null);

        vacio = (TextView) view.findViewById(R.id.vacio);
        vacio.setVisibility(View.GONE);
        texto = (EditText) view.findViewById(R.id.texto_comentario);
        enviar = (ImageView) view.findViewById(R.id.enviar);
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = texto.getText().toString();
                if (!txt.equals("")) {
                    SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.user_preference), Context.MODE_PRIVATE);
                    String claveApi = sp.getString(getString(R.string.user_preference_key), null);
                    Contenedor c = ((MainActivity) getActivity()).getContenedor();
                    String id = c.getId() + "";
                    String tipo = c.getTipo() + "";

                    PublicarComentario publicarComentario = new PublicarComentario();
                    publicarComentario.execute(claveApi, txt, tipo, id);
                }
            }
        });

        list = (RecyclerView) view.findViewById(R.id.lista_comentarios);

        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        ConsultaComentarios consultaComentarios = new ConsultaComentarios();
        consultaComentarios.execute();
        return view;
    }

    public class PublicarComentario extends AsyncTask<String, Comentario, Comentario> {
        private static final String JSON_ESTADO = "estado";
        private static final String JSON_IDCONTENEDOR = "idContenedor";
        private static final String JSON_FECHA = "fecha";
        private static final String JSON_TIPOCONTENEDOR = "tipo";
        private static final String JSON_TEXTOCOMENTARIO = "texto";

        public static final String JSON_ARRAYCOMENTARIO = "comentarios";
        public static final String JSON_IDCOMENTARIO = "idComentario";
        public static final String JSON_AUTOR = "nombreAutor";
        public static final String JSON_CUERPO = "cuerpo";

        private final String URL = "http://raspinico.ddns.net/ApiGreenpoint/v1/comentarios";

        private Comentario comentario;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Comentario doInBackground(String... params) {
            String claveApi = params[0];
            String texto = params[1];
            String tipo = params[2];
            String idContenedor = params[3];
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Authorization", claveApi);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentDateandTime = sdf.format(Calendar.getInstance().getTime());

                putDataToConnection(urlConnection, idContenedor, tipo, texto, currentDateandTime);

                if (urlConnection.getResponseCode() == 400) {
                    readReturnDataFromConnectionFail(urlConnection);
                }

                String data = readReturnDataFromConnection(urlConnection);
                comentario = getDataFromJson(data);

            } catch (IOException e) {

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return comentario;
        }

        private void putDataToConnection(HttpURLConnection connection, String idContenedor, String tipoContenedor, String texto, String fecha) {
            try {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                JSONObject p = new JSONObject();

                p.put(JSON_IDCONTENEDOR, idContenedor);
                p.put(JSON_TIPOCONTENEDOR, tipoContenedor);
                p.put(JSON_FECHA, fecha);
                p.put(JSON_TEXTOCOMENTARIO, texto);

                wr.writeBytes(p.toString());
                wr.flush();
                wr.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
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

        private String readReturnDataFromConnectionFail(HttpURLConnection connection) throws IOException {
            InputStream inputStream = connection.getErrorStream();
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

        private Comentario getDataFromJson(String data) {
            Comentario comentario;
            try {

                JSONObject jsonObject = new JSONObject(data);
                int estado = jsonObject.getInt(JSON_ESTADO);
                if (estado == 3) {
                    comentario = new Comentario();
                    jsonObject = jsonObject.getJSONArray(JSON_ARRAYCOMENTARIO).getJSONObject(0);
                    comentario.setIdComentario(Integer.parseInt(jsonObject.getString(JSON_IDCOMENTARIO)));
                    comentario.setFecha(jsonObject.getString(JSON_FECHA));
                    comentario.setNomUsuario(jsonObject.getString(JSON_AUTOR));
                    comentario.setTexto(jsonObject.getString(JSON_CUERPO));
                    SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.user_preference), Context.MODE_PRIVATE);
                    String encodedImg = sp.getString(getString(R.string.user_preference_image), "null");
                    comentario.setEncodedImg(encodedImg);

                    return comentario;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Comentario c) {
            super.onPostExecute(c);
            if (c != null) {
                ((AdaptadorLista) list.getAdapter()).addItem(c);
                vacio.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
                texto.setText("");
            }
        }
    }

    public class ConsultaComentarios extends AsyncTask<Void, Void, ArrayList<Comentario>> {

        public static final String JSON_ARRAYCOMENTARIO = "comentarios";
        public static final String JSON_IDCOMENTARIO = "idComentario";
        public static final String JSON_AUTOR = "nombreAutor";
        public static final String JSON_FECHA = "fecha";
        public static final String JSON_CUERPO = "cuerpo";
        private static final String JSON_IMG = "encodedImg";


        private String URL = "http://raspinico.ddns.net/ApiGreenpoint/v1/comentarios";

        @Override
        protected ArrayList<Comentario> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            final String ID_CONTENEDOR = "contenedor";
            final String TIPO_CONTENEDOR = "tipo";
            Uri uri = Uri.parse(URL).buildUpon()
                    .appendQueryParameter(ID_CONTENEDOR, ((MainActivity) getActivity()).getContenedor().getId() + "")
                    .appendQueryParameter(TIPO_CONTENEDOR, ((MainActivity) getActivity()).getContenedor().getTipo() + "")
                    .build();

            ArrayList<Comentario> comentarios = new ArrayList<>();
            try {
                java.net.URL url = new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                String data = readReturnDataFromConnection(urlConnection);

                comentarios = getDataFromJson(data);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return comentarios;
        }

        @Override
        protected void onPostExecute(ArrayList<Comentario> comentarios) {
            super.onPostExecute(comentarios);
            if (!comentarios.isEmpty()) {
                AdaptadorLista adaptadorLista = new AdaptadorLista(comentarios, getActivity().getApplicationContext());
                list.setAdapter(adaptadorLista);
                vacio.setVisibility(View.GONE);
            } else {
                AdaptadorLista adaptadorLista = new AdaptadorLista(null, getActivity().getApplicationContext());
                list.setAdapter(adaptadorLista);
                list.setVisibility(View.GONE);
                vacio.setVisibility(View.VISIBLE);
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
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                return null;
            }
            return buffer.toString();
        }

        private ArrayList<Comentario> getDataFromJson(String data) {
            ArrayList<Comentario> comentariosJSON = new ArrayList<Comentario>();
            Comentario comentario;
            try {
                JSONObject jsonObjectJson = new JSONObject(data);
                JSONArray jsonArrayJson = jsonObjectJson.getJSONArray(JSON_ARRAYCOMENTARIO);
                JSONObject temporal;
                for (int i = 0; i < jsonArrayJson.length(); i++) {
                    temporal = jsonArrayJson.getJSONObject(i);
                    comentario = new Comentario();
                    comentario.setIdComentario(temporal.getInt(JSON_IDCOMENTARIO));
                    comentario.setNomUsuario(temporal.getString(JSON_AUTOR));
                    comentario.setFecha(temporal.getString(JSON_FECHA));
                    comentario.setTexto(temporal.getString(JSON_CUERPO));
                    try {
                        comentario.setEncodedImg(temporal.getString(JSON_IMG));
                    } catch (JSONException e) {

                    }
                    comentariosJSON.add(comentario);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return comentariosJSON;
        }

    }
}
