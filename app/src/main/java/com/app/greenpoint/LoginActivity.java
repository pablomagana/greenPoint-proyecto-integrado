package com.app.greenpoint;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.app.greenpoint.data.GreenpointDBAdapter;


public class LoginActivity extends AppCompatActivity {
    private Context c;
    private static LoginFragment loginf;
    private static RegistroFragment registrof;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginf = new LoginFragment();
        registrof = new RegistroFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentloginactivity, loginf)
                .commit();

        c = getApplicationContext();
    }

    public void showViewRegistro() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_right, R.anim.fragment_slide_left)
                .replace(R.id.fragmentloginactivity, registrof)
                .commit();
    }

    public void showViewLogin() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_right, R.anim.fragment_slide_left)
                .replace(R.id.fragmentloginactivity, loginf)
                .commit();
    }

    public void showState(String s) {
        GreenpointDBAdapter dbAdapter = new GreenpointDBAdapter(getApplicationContext());
        dbAdapter.deleteAllFavoritos();
        Toast.makeText(LoginActivity.this, s, Toast.LENGTH_SHORT).show();
        finish();
    }
}
