package com.app.greenpoint;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.greenpoint.adapter.AdaptadorListaFavoritos;
import com.app.greenpoint.adapter.DividerItemDecoration;
import com.app.greenpoint.data.GreenpointDBAdapter;
import com.app.greenpoint.model.Favorito;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private static final int IMAGE_CODE = 200;

    private CircularImageView imagenPerfil;
    private RecyclerView listaFavoritos;
    private TextView nombre, email, marcador;

    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, null);

        marcador = (TextView) view.findViewById(R.id.vacio);
        listaFavoritos = (RecyclerView) view.findViewById(R.id.listView_favoritos);
        nombre = (TextView) view.findViewById(R.id.tv_nombreperfil);
        email = (TextView) view.findViewById(R.id.tv_emailperfil);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getActivity().getApplicationContext(), DividerItemDecoration.VERTICAL_LIST);
        listaFavoritos.addItemDecoration(itemDecoration);
        listaFavoritos.setHasFixedSize(true);
        listaFavoritos.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        GreenpointDBAdapter dbAdapter = new GreenpointDBAdapter(getActivity().getApplicationContext());
        ArrayList<Favorito> f = dbAdapter.recuperarFavoritos();
        if (!f.isEmpty()) {
            AdaptadorListaFavoritos al = new AdaptadorListaFavoritos(f, getActivity().getApplicationContext());
            listaFavoritos.setAdapter(al);
            marcador.setVisibility(View.GONE);
        } else {
            listaFavoritos.setVisibility(View.GONE);
            marcador.setVisibility(View.VISIBLE);
        }

        imagenPerfil = (CircularImageView) view.findViewById(R.id.circular);
        imagenPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPickImageIntent(getActivity().getApplicationContext());
                startActivityForResult(intent, IMAGE_CODE);
            }
        });

        SharedPreferences sp = getActivity().getApplicationContext().getSharedPreferences(getString(R.string.user_preference), Context.MODE_PRIVATE);

        String nom = sp.getString(getString(R.string.user_preference_name), getString(R.string.Nombre));
        nombre.setText(nom);
        String mail = sp.getString(getString(R.string.user_preference_mail), getString(R.string.Correoelectronico));
        email.setText(mail);
        String imgEncoded = sp.getString(getString(R.string.user_preference_image), "");
        if (!imgEncoded.equals("") && !imgEncoded.equals("null")) {
            byte[] data = Base64.decode(imgEncoded, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            imagenPerfil.setImageBitmap(bitmap);
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMAGE_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = decodeUri(uri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (bitmap != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageBytes = baos.toByteArray();

                        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.user_preference), Context.MODE_PRIVATE);
                        String claveApi = sharedPreferences.getString(getString(R.string.user_preference_key), "");

                        ImagenChange imagenChange = new ImagenChange();
                        imagenChange.execute(claveApi, encodedImage);

                        imagenPerfil.setImageBitmap(bitmap);
                    }
                }
        }
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 140;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage), null, o2);

    }

    public Intent getPickImageIntent(Context context) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = ((ProfileActivity) getActivity()).getOutputMediaFileUri();
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intentList = addIntentsToList(context, intentList, pickIntent);
        intentList = addIntentsToList(context, intentList, takePhotoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    context.getString(R.string.pick_image_intent_text));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    private List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    private class ImagenChange extends AsyncTask<String, Void, String> {

        private static final String JSON_IMAGEN = "imagen";
        private final String URL = "http://raspinico.ddns.net/ApiGreenpoint/v1/usuarios/imagen";

        @Override
        protected String doInBackground(String... params) {
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
                    if (b)
                        return params[1];
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String aString) {
            super.onPostExecute(aString);
            if (aString != null) {
                // guardar imagen
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.user_preference), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString(getString(R.string.user_preference_image), aString);

                editor.apply();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.image_ko), Toast.LENGTH_LONG).show();
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
}
