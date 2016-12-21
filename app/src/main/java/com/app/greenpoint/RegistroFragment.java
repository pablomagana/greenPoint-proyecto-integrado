package com.app.greenpoint;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.greenpoint.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class RegistroFragment extends Fragment {
    private TextView tengoRegistro;
    private EditText nombre, contrasena, correo;
    private Button registrar;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro, container, false);

        tengoRegistro = (TextView) view.findViewById(R.id.tengoregistro);
        tengoRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity) getActivity()).showViewLogin();
            }
        });
        correo = (EditText) view.findViewById(R.id.correoet);
        nombre = (EditText) view.findViewById(R.id.nombreet);
        contrasena = (EditText) view.findViewById(R.id.contrasenaet);

        registrar = (Button) view.findViewById(R.id.registrarbtn);
        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!nombre.getText().toString().equals("") && !correo.getText().equals("") && !contrasena.getText().equals("")) {
                    String name = nombre.getText().toString();
                    String pass = contrasena.getText().toString();
                    String mail = correo.getText().toString();
                    RegisterAT register = new RegisterAT();
                    register.execute(name, mail, pass);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.campos_necesarios), Toast.LENGTH_LONG).show();
                }
            }
        });


        return view;
    }

    public class RegisterAT extends AsyncTask<String, Void, Boolean> {

        private final String JSON_NOMBRE = "nombre";
        private final String JSON_CORREO = "correo";
        private final String JSON_CONTRASENA = "contrasena";
        private final String JSON_CLAVEAPI = "claveApi";
        private final String JSON_ESTADO = "estado";

        private final String URL = "http://raspinico.ddns.net/ApiGreenpoint/v1/usuarios/registro";

        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                //inserción de parametros
                urlConnection.setDoOutput(true);
                putDataToConnection(urlConnection, params);

                int responsecode = urlConnection.getResponseCode();

                if (responsecode == 200) {
                    String data = readReturnDataFromConnection(urlConnection);
                    Usuario u = getDataFromJson(data, params);
                    SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.user_preference), Context.MODE_PRIVATE);
                    SharedPreferences.Editor spe = sp.edit();
                    spe.putString(getString(R.string.user_preference_name), u.getNombre());
                    spe.putString(getString(R.string.user_preference_mail), u.getCorreo());
                    spe.putString(getString(R.string.user_preference_key), u.getClaveApi());
                    spe.putBoolean(getString(R.string.user_preference_login), true);
                    spe.apply();
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            LoginActivity parent = ((LoginActivity) getActivity());
            if (aBoolean) {
                String s = parent.getString(R.string.register_ok);
                parent.showState(s);
            }
            else {
                Toast.makeText(parent.getApplicationContext(), parent.getString(R.string.register_err), Toast.LENGTH_LONG).show();
            }
        }

        private Usuario getDataFromJson(String data, String... params) {
            Usuario usuario = new Usuario(params[0], params[1], null);
            try {
                JSONObject json = new JSONObject(data);
                if (json.getInt(JSON_ESTADO) == 1) {
                    usuario.setClaveApi(json.getString(JSON_CLAVEAPI));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return usuario;
        }

        private String readReturnDataFromConnection(HttpURLConnection connection) throws IOException {
            InputStream inputStream = connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = null;
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

        private void putDataToConnection(HttpURLConnection connection, String... params) {
            try {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                JSONObject p = new JSONObject();
                    p.put(JSON_NOMBRE, params[0]);
                    p.put(JSON_CORREO, params[1]);
                    p.put(JSON_CONTRASENA, params[2]);

                wr.writeBytes(p.toString());
                wr.flush();
                wr.close();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

    }

}
