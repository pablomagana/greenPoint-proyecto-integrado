package com.app.greenpoint;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.greenpoint.model.Usuario;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

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
import java.util.Arrays;

public class LoginFragment extends Fragment {

    private int RC_SIGN_IN = 1;

    private EditText userName, userPass;
    private Button loginButton;
    private TextView registerButton;
    private SignInButton googleButton;
    private GoogleSignInOptions googleSignInOptions;
    private GoogleApiClient googleApiClient;
    private LoginButton facebookButton;
    private CallbackManager callbackManager;

    public LoginFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!FacebookSdk.isInitialized())
            FacebookSdk.sdkInitialize(context);

        callbackManager = CallbackManager.Factory.create();

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .build();

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .enableAutoManage(getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                        }
                    })
                    .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                    .build();
        }
        googleApiClient.connect();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        googleApiClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(getActivity().getApplicationContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        userName = (EditText) view.findViewById(R.id.userName);
        userPass = (EditText) view.findViewById(R.id.userPass);
        loginButton = (Button) view.findViewById(R.id.login);
        registerButton = (TextView) view.findViewById(R.id.register);
        googleButton = (SignInButton) view.findViewById(R.id.sign_in_google);
        facebookButton = (LoginButton) view.findViewById(R.id.sign_in_facebook);

        facebookButton.setFragment(this);
        facebookButton.setReadPermissions(Arrays.asList("email"));

        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    String name = object.getString("name");
                                    String email = object.getString("email");
                                    String id = object.getString("id");
                                    LoginAT login = new LoginAT(LoginAT.FACEBOOK);
                                    login.execute(name, email, id);
                                    try {
                                        String urlImagen = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                        DescargaImagen descargaImagen = new DescargaImagen();
                                        descargaImagen.execute(urlImagen);
                                    } catch (JSONException e) {

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "name,id,email,picture.type(normal)");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

//        if (!Conexiones.getInstance(getActivity()).conectadoMovil() && !Conexiones.getInstance(getActivity()).conectadoWifi()) {
//            Toast.makeText(getActivity(), "Asegurate de que tienes acceso a internet para iniciar sesion", Toast.LENGTH_LONG).show();
//        }

        googleButton.setSize(SignInButton.SIZE_STANDARD);
        googleButton.setScopes(googleSignInOptions.getScopeArray());
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userName.getText().toString().equals("") && !userPass.getText().toString().equals("")) {
                    String mail = userName.getText().toString();
                    String pass = userPass.getText().toString();
                    LoginAT login = new LoginAT(LoginAT.LOGIN);
                    login.execute(mail, pass, "true");
                } else {
                    Toast.makeText(getActivity(), getString(R.string.campos_necesarios), Toast.LENGTH_LONG).show();
                }

            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity) getActivity()).showViewRegistro();
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                String name = acct.getDisplayName();
                String email = acct.getEmail();
                String id = acct.getId();
                LoginAT login = new LoginAT(LoginAT.GOOGLE);
                login.execute(name, email, id);

                DescargaImagen descargaImagen = new DescargaImagen();
                descargaImagen.execute(acct.getPhotoUrl().toString());
            }
        } else {
            // error login google
        }
    }

    private class DescargaImagen extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            String urlImg = params[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(urlImg);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());
                if (bitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageBytes = baos.toByteArray();

                    String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.user_preference), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(getString(R.string.user_preference_image), encodedImage);
                    editor.apply();

                    String claveApi = sharedPreferences.getString(getString(R.string.user_preference_key), "");

                    String[] array = new String[]{claveApi, encodedImage};
                    return array;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] aString) {
            super.onPostExecute(aString);
            if (aString != null) {
                UploadImagen uploadImagen = new UploadImagen();
                uploadImagen.execute(aString[0], aString[1]);
            }
        }
    }

    private class UploadImagen extends AsyncTask<String, Void, Boolean> {

        private static final String JSON_IMAGEN = "imagen";
        private final String URL = "http://raspinico.ddns.net/ApiGreenpoint/v1/usuarios/imagen";

        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            Boolean b = false;
            try {
                java.net.URL url = new URL(URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Authorization", params[0]);

                putDataToConnection(urlConnection, params);
                urlConnection.connect();

                int responsecode = urlConnection.getResponseCode();

                if (responsecode == 200) {
                    String data = readReturnDataFromConnection(urlConnection);
                    b = getDataFromJson(data);
                    return b;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return b;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            LoginActivity parent = ((LoginActivity) getActivity());
            if (aBoolean) {
                String s = parent.getString(R.string.login_ok);
                parent.showState(s);
            } else {
                Toast.makeText(parent.getApplicationContext(), parent.getString(R.string.login_err), Toast.LENGTH_LONG).show();
            }

        }

        private void putDataToConnection(HttpURLConnection connection, String... params) {
            try {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                JSONObject p = new JSONObject();

                p.put(JSON_IMAGEN, params[1]);

                wr.writeBytes(p.toString());
                wr.flush();
                wr.close();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        private Boolean getDataFromJson(String data) {
            Boolean b = false;
            try {
                JSONObject json = new JSONObject(data);
                b = Boolean.valueOf(json.getString(JSON_IMAGEN));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return b;
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
    }

    private class LoginAT extends AsyncTask<String, Void, Boolean> {

        private final String JSON_CORREO = "correo";
        private final String JSON_CONTRASENA = "contrasena";
        private final String JSON_ESTADO = "estado";
        private final String JSON_USUARIO = "usuario";
        private final String JSON_CLAVEAPI = "claveApi";
        private final String JSON_NOMBRE = "nombre";
        private final String JSON_GOOGLE = "google";
        private final String JSON_IMAGEN = "imagen";
        private final String JSON_FACEBOOK = "facebook";


        public static final int LOGIN = 0;
        public static final int GOOGLE = 1;
        public static final int FACEBOOK = 2;

        private int tipo = -1;

        public LoginAT(int tipo) {
            this.tipo = tipo;
        }

        private String URL_LOGIN = "http://raspinico.ddns.net/ApiGreenpoint/v1/usuarios/login";
        private String URL_GOOGLE = "http://raspinico.ddns.net/ApiGreenpoint/v1/usuarios/google";
        private String URL_FACEBOOK = "http://raspinico.ddns.net/ApiGreenpoint/v1/usuarios/facebook";


        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            Boolean b = false;
            if (params[2] != null)
                b = Boolean.parseBoolean(params[2]);
            try {
                URL url = new URL(buildUri().toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                putDataToConnection(urlConnection, params);
                urlConnection.connect();

                int responsecode = urlConnection.getResponseCode();

                if (responsecode == 200) {
                    String data = readReturnDataFromConnection(urlConnection);
                    Usuario u = getDataFromJson(data);
                    if (tipo == GOOGLE || tipo == FACEBOOK) {
                        u.setNombre(params[0]);
                        u.setCorreo(params[1]);
                    }
                    SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.user_preference), Context.MODE_PRIVATE);
                    SharedPreferences.Editor spe = sp.edit();
                    spe.putString(getString(R.string.user_preference_name), u.getNombre());
                    spe.putString(getString(R.string.user_preference_mail), u.getCorreo());
                    spe.putString(getString(R.string.user_preference_key), u.getClaveApi());
                    if (tipo != GOOGLE && tipo != FACEBOOK) {
                        spe.putString(getString(R.string.user_preference_image), u.getImagen());
                    }
                    spe.putBoolean(getString(R.string.user_preference_login), true);
                    spe.apply();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return b;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (tipo != GOOGLE && tipo != FACEBOOK) {
                LoginActivity parent = ((LoginActivity) getActivity());
                if (aBoolean) {
                    String s = parent.getString(R.string.login_ok);
                    parent.showState(s);
                } else {
                    Toast.makeText(parent.getApplicationContext(), parent.getString(R.string.login_err), Toast.LENGTH_LONG).show();
                }
            }
        }

        private Uri buildUri() {
            Uri uri = null;
            switch (tipo) {
                case LOGIN:
                    uri = Uri.parse(URL_LOGIN);
                    break;
                case GOOGLE:
                    uri = Uri.parse(URL_GOOGLE);
                    break;
                case FACEBOOK:
                    uri = Uri.parse(URL_FACEBOOK);
                    break;
            }
            return uri;
        }

        private Usuario getDataFromJson(String data) {
            Usuario usuario = new Usuario();
            try {
                JSONObject json = new JSONObject(data);
                if (json.getInt(JSON_ESTADO) == 1) {
                    switch (tipo) {
                        case LOGIN:
                            JSONObject usuarioJSON = json.getJSONObject(JSON_USUARIO);

                            usuario.setNombre(usuarioJSON.getString(JSON_NOMBRE));
                            usuario.setCorreo(usuarioJSON.getString(JSON_CORREO));
                            usuario.setClaveApi(usuarioJSON.getString(JSON_CLAVEAPI));
                            usuario.setImagen(usuarioJSON.getString(JSON_IMAGEN));
                            break;
                        case GOOGLE:
                        case FACEBOOK:
                            usuario.setClaveApi(json.getString(JSON_CLAVEAPI));
                            break;
                    }

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

                switch (tipo) {
                    case LOGIN:
                        p.put(JSON_CORREO, params[0]);
                        p.put(JSON_CONTRASENA, params[1]);
                        break;
                    case GOOGLE:
                        p.put(JSON_NOMBRE, params[0]);
                        p.put(JSON_CORREO, params[1]);
                        p.put(JSON_GOOGLE, params[2]);
                        break;
                    case FACEBOOK:
                        p.put(JSON_NOMBRE, params[0]);
                        p.put(JSON_CORREO, params[1]);
                        p.put(JSON_FACEBOOK, params[2]);
                        break;
                }

                wr.writeBytes(p.toString());
                wr.flush();
                wr.close();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
