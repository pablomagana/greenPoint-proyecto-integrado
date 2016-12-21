package com.app.greenpoint;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.app.greenpoint.adapter.AdaptadorTabs;
import com.app.greenpoint.model.Usuario;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfileActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private PasswordChange pc;
    private ProfileFragment profileFragment;
    private GraficsFragment graficsFragment;

    public PasswordChange getPc() {
        return pc;
    }

    public void setPc(PasswordChange pc) {
        this.pc = pc;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.perfil));

        toolbar.inflateMenu(R.menu.profile_menu);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        profileFragment = new ProfileFragment();
        graficsFragment = new GraficsFragment();

        int numTabs = 2;
        Fragment[] fragments = new Fragment[]{profileFragment, graficsFragment};
        AdaptadorTabs adaptadorTabs = new AdaptadorTabs(getSupportFragmentManager(), numTabs, fragments);

        ViewPager pager = (ViewPager) findViewById(R.id.pager_view);
        pager.setAdapter(adaptadorTabs);

        //slid tabs
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(pager);

        tabs.getTabAt(0).setIcon(R.drawable.ic_person_white_24dp);
        tabs.getTabAt(1).setIcon(R.drawable.ic_timeline_white_24dp);


        //PasswordChange
         pc=new PasswordChange();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_close_session:
                cerrarSesion();
                return true;
            case R.id.item_change_password:
                cambiarPassword();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void cambiarPassword() {
        android.support.v4.app.FragmentManager manager=getSupportFragmentManager();
        PasswordDialogo dialogoPassword = new PasswordDialogo();
        dialogoPassword.show(manager, "changepassword");
    }

    private void cerrarSesion() {
        SharedPreferences.Editor sp = getSharedPreferences(getString(R.string.user_preference), MODE_PRIVATE).edit();
        sp.remove(getString(R.string.user_preference_name));
        sp.remove(getString(R.string.user_preference_mail));
        sp.remove(getString(R.string.user_preference_key));
        sp.remove(getString(R.string.user_preference_image));
        sp.putBoolean(getString(R.string.user_preference_login), false);
        sp.apply();
        if (!FacebookSdk.isInitialized())
            FacebookSdk.sdkInitialize(getApplicationContext());
        LoginManager loginManager = LoginManager.getInstance();
        if (loginManager != null) {
            loginManager.logOut();
        }
        finish();
    }

    public File getMediaFileStorageDir() {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getString(R.string.app_name));

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        return mediaStorageDir;
    }

    public Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {

        File mediaStorageDir = getMediaFileStorageDir();

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "profilePic" + ".jpg");

        return mediaFile;
    }

    public static class PasswordDialogo extends DialogFragment implements Dialog.OnClickListener {
        private View v;
        private EditText oldPass;
        private EditText newPass;
        private EditText reNewPass;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            v = LayoutInflater.from(getActivity()).inflate(R.layout.password_dialog, null);
            return new AlertDialog.Builder(getActivity()).setTitle(R.string.cambiarpassword).setPositiveButton(android.R.string.ok, this).setNegativeButton(android.R.string.cancel, null).setView(v).create();
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            oldPass = (EditText) v.findViewById(R.id.oldPass);
            newPass = (EditText) v.findViewById(R.id.newPass);
            reNewPass = (EditText) v.findViewById(R.id.reNewPass);
            SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.user_preference), Context.MODE_PRIVATE);
            String claveApi = sp.getString(getString(R.string.user_preference_key), null);
            String newp = newPass.getText().toString();
            String oldp = oldPass.getText().toString();
            String renewp = reNewPass.getText().toString();

            if(newp.equals(renewp) && oldp!=""){
                ((ProfileActivity) getActivity()).getPc().execute(claveApi,newp,oldp);
            }else{
                Toast.makeText(getActivity(), getString(R.string.errorpassinputs), Toast.LENGTH_SHORT).show();
            }



        }

        public void show(android.support.v4.app.FragmentManager manager, String changepassword) {
            super.show(manager, changepassword);
        }
    }


    public class PasswordChange extends AsyncTask<String, Void, Boolean> {

        private final String JSON_CONTRASENA = "newpassword";
        private final String JSON_OLDCONTRASENA = "oldpassword";

        public static final int PASSWORD = 0;
        public int tipo=0;

        public PasswordChange() {
        }

        private String URL_PASSWORD = "http://raspinico.ddns.net/ApiGreenpoint/v1/usuarios/password";


        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            Boolean b=false;
            try {
                URL url = new URL(buildUri().toString());
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
            if(aBoolean){
                Toast.makeText(getApplicationContext(), getString(R.string.passchangesuccess), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), getString(R.string.errorchangepass), Toast.LENGTH_SHORT).show();
            }
        }

        private Uri buildUri() {
            Uri uri = null;
            switch (tipo) {
                case PASSWORD:
                    uri = Uri.parse(URL_PASSWORD);
                    break;
            }
            return uri;
        }

        private Boolean getDataFromJson(String data) {
            Boolean b=false;
            try {
                JSONObject json = new JSONObject(data);
                b=Boolean.valueOf(json.getString("contrasena"));
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

        private void putDataToConnection(HttpURLConnection connection, String... params) {
            try {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                JSONObject p = new JSONObject();

                switch (tipo) {

                    case PASSWORD:
                        p.put(JSON_CONTRASENA, params[1]);
                        p.put(JSON_OLDCONTRASENA,params[2]);
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
