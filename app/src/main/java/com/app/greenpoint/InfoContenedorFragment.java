package com.app.greenpoint;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.greenpoint.adapter.ContenedorPagerAdapter;
import com.app.greenpoint.data.GreenpointDBAdapter;
import com.app.greenpoint.model.Contenedor;
import com.app.greenpoint.model.Favorito;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class InfoContenedorFragment extends Fragment {

    private Contenedor contenedor;
    private ImageView icon, favorito, reciclar;
    private TextView tipo;
    private TextView direccion;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private View rootView;

    private boolean detalles;

    private GreenpointDBAdapter greenpointDBAdapter;

    public InfoContenedorFragment() {
        this.detalles = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_info_contenedor, container, false);
        greenpointDBAdapter = new GreenpointDBAdapter(getActivity().getApplicationContext());

        icon = (ImageView) rootView.findViewById(R.id.icono);
        tipo = (TextView) rootView.findViewById(R.id.tipo);
        direccion = (TextView) rootView.findViewById(R.id.direccion);
        favorito = (ImageView) rootView.findViewById(R.id.favorito);
        reciclar = (ImageView) rootView.findViewById(R.id.reciclar);
        viewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);


        TypedArray iconos = getResources().obtainTypedArray(R.array.tipo_iconos);
        String[] nombres = getResources().getStringArray(R.array.tipo_contenedores);

        contenedor = ((MainActivity) getActivity()).getContenedor();
        if (isContenedorFavorito(contenedor) > 0)
            favorito.setColorFilter(getResources().getColor(R.color.verde));
        else
            favorito.setColorFilter(getResources().getColor(R.color.grisIcono));


        icon.setImageResource(iconos.getResourceId(contenedor.getTipo(), R.mipmap.ic_launcher));
        tipo.setText(nombres[contenedor.getTipo()]);
        direccion.setText(contenedor.getDireccion());

        favorito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.user_preference), Context.MODE_PRIVATE);
                String claveApi = sp.getString(getString(R.string.user_preference_key), null);
                if (claveApi != null) {
                    if (isContenedorFavorito(contenedor) > 0) {
                        EliminarFavoritoTask eliminarFavoritoTask = new EliminarFavoritoTask(contenedor);
                        eliminarFavoritoTask.execute(claveApi);
                    } else {
                        CrearFavoritoTask crearFavoritoTask = new CrearFavoritoTask(contenedor);
                        crearFavoritoTask.execute(claveApi);
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.necesita_registro), Toast.LENGTH_LONG).show();
                }
            }
        });

        if (isClosed(contenedor.getLocation()) == true) {
            reciclar.setVisibility(View.VISIBLE);
        } else {
            reciclar.setVisibility(View.GONE);
        }
        reciclar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.user_preference), Context.MODE_PRIVATE);
                String claveApi = sp.getString(getString(R.string.user_preference_key), null);
                if (claveApi != null) {
                    ReciclarTask reciclarTask = new ReciclarTask(contenedor.getTipo());
                    reciclarTask.execute(claveApi);

                } else {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.necesita_registro), Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }

    private boolean isClosed(LatLng location)//detectar si el usuario esta a menos de x metros del contenedor
    {
        float distance;

        Location loc = new Location("");
        loc.setLongitude(location.longitude);
        loc.setLatitude(location.latitude);

        distance = loc.distanceTo(((MainActivity) getActivity()).getMapFragment().getmLastLocation());

        if (distance > 10) {
            return false;
        } else {
            return true;
        }
    }

    private int isContenedorFavorito(Contenedor c) {
        return greenpointDBAdapter.queryFavorito(c);
    }

    private long añadirFavorito(Contenedor c, int idFavorito) {
        return greenpointDBAdapter.insertFavorito(idFavorito, c);
    }

    private long eliminarFavorito(int idFavorito) {
        return greenpointDBAdapter.deleteFavorito(idFavorito);
    }

    public void showDetalles() {
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.detalles);
        layout.setVisibility(View.VISIBLE);
        Fragment[] fragments = {new ListaComentariosFragment(), new AlertaFragment()};
        ContenedorPagerAdapter adapter = new ContenedorPagerAdapter(getChildFragmentManager(), getContext(), fragments);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_comment);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_alert);
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public void hideDetalles() {
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.detalles);
        layout.setVisibility(View.GONE);
    }

    public boolean isDetallesHide() {
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.detalles);
        switch (layout.getVisibility()) {
            case View.GONE:
            case View.INVISIBLE:
                return true;
            case View.VISIBLE:
                return false;
        }
        return false;
    }

    private class ReciclarTask extends AsyncTask<String, Void, Integer> {

        private static final String JSON_ESTADO = "estado";
        private static final String JSON_TIPOCONTENEDOR = "tipo";

        private final String URL = "http://raspinico.ddns.net/ApiGreenpoint/v1/reciclaje";

        private int tipoBasura;

        public ReciclarTask(int tipoBasura) {
            this.tipoBasura = tipoBasura;
        }

        @Override
        protected Integer doInBackground(String... params) {
            String claveApi = params[0];
            HttpURLConnection urlConnection = null;
            Integer estado = -1;
            try {
                URL url = new URL(URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Authorization", claveApi);
                urlConnection.setDoOutput(true);
                putDataToConnection(urlConnection);
                urlConnection.connect();

                String dataPost = readReturnDataFromConnection(urlConnection);
                estado = getDataFromJson(dataPost);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return estado;
        }

        @Override
        protected void onPostExecute(Integer estado) {
            String[] fMolonas={getString(R.string.molona1),getString(R.string.molona2),getString(R.string.molon3),getString(R.string.molona4),getString(R.string.molona5),getString(R.string.molona6)};
            int ordenFrase=(int) (Math.random()*fMolonas.length);
            String frase=fMolonas[ordenFrase];
            super.onPostExecute(estado);
            if (estado == 3) {
                Toast.makeText(getActivity(), frase, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "error al reciclar", Toast.LENGTH_SHORT).show();
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

        private int getDataFromJson(String data) {
            int estado = 2;//2 mal //3 exito
            try {
                JSONObject jsonObject = new JSONObject(data);
                estado = jsonObject.getInt(JSON_ESTADO);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return estado;
        }

        private void putDataToConnection(HttpURLConnection connection) {
            try {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                JSONObject p = new JSONObject();
                p.put(JSON_TIPOCONTENEDOR, this.tipoBasura);
                wr.writeBytes(p.toString());
                wr.flush();
                wr.close();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private class CrearFavoritoTask extends AsyncTask<String, Void, Favorito> {

        private static final String JSON_ESTADO = "estado";
        private static final String JSON_IDCONTENEDOR = "idContenedor";
        private static final String JSON_TIPOCONTENEDOR = "tipo";
        private static final String JSON_IDFAVORITO = "idFavorito";
        private static final String JSON_DIRECCION = "direccion";
        private static final String JSON_LAT = "lat";
        private static final String JSON_LON = "lon";

        private final String URL = "http://raspinico.ddns.net/ApiGreenpoint/v1/favoritos";

        private Contenedor contenedor;

        public CrearFavoritoTask(Contenedor contenedor) {
            this.contenedor = contenedor;
        }

        @Override
        protected Favorito doInBackground(String... params) {
            String claveApi = params[0];
            HttpURLConnection urlConnection = null;
            Favorito favorito = null;
            try {
                URL url = new URL(URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Authorization", claveApi);
                urlConnection.setDoOutput(true);
                putDataToConnection(urlConnection);
                urlConnection.connect();

                String dataPost = readReturnDataFromConnection(urlConnection);
                favorito = getDataFromJson(dataPost);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return favorito;
        }

        @Override
        protected void onPostExecute(Favorito favorito) {
            super.onPostExecute(favorito);
            long result = -1;
            String msg = "";
            if (favorito != null) {
                InfoContenedorFragment.this.favorito.setColorFilter(getResources().getColor(R.color.verde));
                msg = getString(R.string.place_contenedor) + this.contenedor.getDireccion() + getString(R.string.add_marcador);
                result = añadirFavorito(this.contenedor, favorito.getIdFavorito());
            }
            if (result > 0)
                Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
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

        private Favorito getDataFromJson(String data) {
            Favorito favorito = null;
            try {
                JSONObject jsonObject = new JSONObject(data);
                int estado = jsonObject.getInt(JSON_ESTADO);
                if (estado == 4) {
                    favorito = new Favorito();
                    favorito.setIdFavorito(jsonObject.getInt(JSON_IDFAVORITO));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return favorito;
        }

        private void putDataToConnection(HttpURLConnection connection) {
            try {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                JSONObject p = new JSONObject();

                p.put(JSON_IDCONTENEDOR, this.contenedor.getId());
                p.put(JSON_TIPOCONTENEDOR, this.contenedor.getTipo());
                p.put(JSON_DIRECCION, this.contenedor.getDireccion());
                p.put(JSON_LAT, this.contenedor.getLocation().latitude);
                p.put(JSON_LON, this.contenedor.getLocation().longitude);

                wr.writeBytes(p.toString());
                wr.flush();
                wr.close();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class EliminarFavoritoTask extends AsyncTask<String, Void, Favorito> {

        private static final String JSON_ESTADO = "estado";
        private static final String JSON_IDFAVORITO = "idFavorito";
        private static final String HEADER_IDCONTENEDOR = "idContenedor";
        private static final String HEADER_TIPOCONTENEDOR = "tipoContenedor";

        private final String URL = "http://raspinico.ddns.net/ApiGreenpoint/v1/favoritos";

        private Contenedor contenedor;

        public EliminarFavoritoTask(Contenedor contenedor) {
            this.contenedor = contenedor;
        }

        @Override
        protected Favorito doInBackground(String... params) {
            String claveApi = params[0];
            HttpURLConnection urlConnection = null;
            Favorito favorito = null;
            try {
                URL url = new URL(URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("DELETE");
                urlConnection.setRequestProperty("Authorization", claveApi);
                urlConnection.setRequestProperty(HEADER_IDCONTENEDOR, this.contenedor.getId() + "");
                urlConnection.setRequestProperty(HEADER_TIPOCONTENEDOR, this.contenedor.getTipo() + "");
                urlConnection.connect();
                String data = readReturnDataFromConnection(urlConnection);
                favorito = getDataFromJson(data);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return favorito;
        }

        @Override
        protected void onPostExecute(Favorito favorito) {
            super.onPostExecute(favorito);
            long result = -1;
            String msg = "";
            if (favorito != null) {
                InfoContenedorFragment.this.favorito.setColorFilter(getResources().getColor(R.color.grisIcono));
                msg = getString(R.string.place_contenedor) + this.contenedor.getDireccion() + getString(R.string.delete_marcador);
                result = eliminarFavorito(favorito.getIdFavorito());
            }
            if (result > 0)
                Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
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

        private Favorito getDataFromJson(String data) {
            Favorito favorito = null;
            try {
                JSONObject jsonObject = new JSONObject(data);
                int estado = jsonObject.getInt(JSON_ESTADO);
                if (estado == 1) {
                    favorito = new Favorito();
                    favorito.setIdFavorito(jsonObject.getInt(JSON_IDFAVORITO));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return favorito;
        }
    }
}
