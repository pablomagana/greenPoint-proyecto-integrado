package com.app.greenpoint;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.app.greenpoint.model.Contenedor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AlertaFragment extends Fragment {

    private EditText texto;
    private Spinner spinner;
    private ImageView camara, foto, enviar, delete;
    private Bitmap bitmap;
    private View view;

    private boolean imagen;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_alert, container, false);

        texto = (EditText) view.findViewById(R.id.comentario);
        spinner = (Spinner) view.findViewById(R.id.spinner);
        camara = (ImageView) view.findViewById(R.id.camara);
        foto = (ImageView) view.findViewById(R.id.imagen);
        enviar = (ImageView) view.findViewById(R.id.enviar);
        delete = (ImageView) view.findViewById(R.id.delete);
        imagen = false;

        String[] tipos = getResources().getStringArray(R.array.tipo_incidencia);
        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_style, tipos);
        adapter.setDropDownViewResource(R.layout.item_style);
        spinner.setAdapter(adapter);

        camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).startCameraApp();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagen = false;
                foto.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
            }
        });
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.user_preference), Context.MODE_PRIVATE);
                String email = sp.getString(getString(R.string.user_preference_mail), null);

                String tipo = spinner.getSelectedItem().toString();
                String descripcion = texto.getText().toString();

                //Async task
                if (!descripcion.equals("")) {
                    EnviaAlerta alerta = new EnviaAlerta();
                    if (imagen) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        if (bitmap != null) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        }
                        byte[] imageBytes = baos.toByteArray();
                        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                        alerta.execute(email, tipo, descripcion, encodedImage);
                    } else
                        alerta.execute(email, tipo, descripcion);
                }
            }
        });

        return view;
    }

    public void loadImagePhoto(Bitmap bitmap) {
        this.bitmap = bitmap;
        foto.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        foto.setImageBitmap(bitmap);
        imagen = true;
    }

    private class EnviaAlerta extends AsyncTask<String, Void, Boolean> {

        private final String JSON_ESTADO = "estado";
        private final String JSON_TIPO = "tipo";
        private final String JSON_IDCONTENEDOR = "idContenedor";
        private final String JSON_TIPOCONTENEDOR = "tipoContenedor";
        private final String JSON_DIRECCION = "dirContenedor";
        private final String JSON_LATITUD = "lat";
        private final String JSON_LONGITUD = "lon";
        private final String JOSN_DESCRIPCION = "descripcion";
        private final String JSON_EMAIL = "email";
        private final String JSON_IMAGEN = "encodedImg";

        private final String URL = "http://matumizi.ddns.net/ApiGreenpoint/v1/alertas";

        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            boolean b = false;
            try {
                URL url = new URL(URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                putDataToConnection(urlConnection, params);

                String data = readReturnDataFromConnection(urlConnection);
                b = getDataFromJson(data);

            } catch (IOException e) {

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return b;
        }

        private void putDataToConnection(HttpURLConnection connection, String... params) {
            try {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                JSONObject p = new JSONObject();
                Contenedor c = ((MainActivity) getActivity()).getContenedor();

                p.put(JSON_TIPO, params[1]);
                p.put(JOSN_DESCRIPCION, params[2]);
                p.put(JSON_IDCONTENEDOR, c.getId());
                p.put(JSON_TIPOCONTENEDOR, c.getTipo());
                p.put(JSON_DIRECCION, c.getDireccion());
                p.put(JSON_LATITUD, c.getLocation().latitude);
                p.put(JSON_LONGITUD, c.getLocation().longitude);
                p.put(JSON_EMAIL, params[0]);
                if (imagen) {
                    p.put(JSON_IMAGEN,params[3] );
                }

                wr.writeBytes(p.toString());
                wr.flush();
                wr.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            if (bool) {
                Toast.makeText(getContext(), getString(R.string.alerta_ok), Toast.LENGTH_LONG).show();
                bitmap = null;
                foto.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
                texto.setText("");
                spinner.setSelection(0);
            } else {
                Toast.makeText(getContext(), getString(R.string.alerta_cancel), Toast.LENGTH_LONG).show();
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

        private boolean getDataFromJson(String data) {
            int estado=-1;
            try {
                JSONObject jsonObject = new JSONObject(data);
                estado =jsonObject.getInt(JSON_ESTADO);
            } catch (JSONException e) {e.printStackTrace();}
            if (estado == 3) {
                return true;
            }else{
                return false;
            }

        }
    }
}
