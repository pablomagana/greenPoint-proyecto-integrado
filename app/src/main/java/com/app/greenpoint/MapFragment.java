package com.app.greenpoint;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.greenpoint.data.GreenpointDBAdapter;
import com.app.greenpoint.model.Contenedor;
import com.app.greenpoint.model.MarkerContenedor;
import com.app.greenpoint.model.Route;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static long REFRESH_GPS = 10000;
    private static float ZOOM_LEVEL = 17.0f;
    public static float ZOOM_IN = 18.0f;

    private ContenedoresTask consulta;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private GoogleMap mMap;
    private FrameLayout frameLayoutInfo, frameLayoutRoute;
    private SupportMapFragment mapFragment;
    private FloatingActionButton fabRoute, fabLocation;
    private InfoContenedorFragment infoContenedorFragment;
    private RouteFragment routeFragment;
    private SharedPreferences preferences;
    private ProgressBar progressBar;
    private Polyline polyline;

    private GreenpointDBAdapter bd;

    private int tipo = 7;
    private boolean cercanos = false;
    private LatLng pointHome;//localizacion de la direccion del usuario

    public MapFragment() {

    }

    public Location getmLastLocation() {
        return mLastLocation;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        preferences = getActivity().getSharedPreferences(getString(R.string.settings_preference), Context.MODE_PRIVATE);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment = SupportMapFragment.newInstance();
        getFragmentManager().beginTransaction()
                .replace(R.id.map, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);

        frameLayoutInfo = (FrameLayout) view.findViewById(R.id.info_contenedor);
        frameLayoutRoute = (FrameLayout) view.findViewById(R.id.route_contenedor);
        fabLocation = (FloatingActionButton) view.findViewById(R.id.location_button);
        fabRoute = (FloatingActionButton) view.findViewById(R.id.route_button);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.toolbar_progress_bar);

        fabLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastLocation != null) {
                    LatLng user = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    moveCameraToLocation(user, ZOOM_LEVEL);
                }
            }
        });
        fabRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideRoute();
                LatLng location = ((MainActivity) getActivity()).getContenedor().getLocation();

                MakeRoute makeRoute = new MakeRoute();
                makeRoute.execute(location.latitude, location.longitude);
            }
        });
        frameLayoutInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoContenedorFragment.isDetallesHide()) {
                    infoContenedorFragment.showDetalles();
                    if (frameLayoutRoute.getVisibility() == View.VISIBLE)
                        frameLayoutRoute.setVisibility(View.GONE);
                    fabLocation.setVisibility(View.GONE);
                    fabRoute.setVisibility(View.GONE);
                }
            }
        });
        frameLayoutRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        bd=new GreenpointDBAdapter(getActivity().getApplicationContext());

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(false);
        int mapType = preferences.getInt(getString(R.string.map_key), 1);
        mMap.setMapType(mapType);

        final UiSettings mapSettings = mMap.getUiSettings();
        mapSettings.setMyLocationButtonEnabled(false);
        mapSettings.setMapToolbarEnabled(false);
        mapSettings.setCompassEnabled(true);
        mapSettings.setRotateGesturesEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (infoContenedorFragment != null) {
                    infoContenedorFragment = null;
                    hideRoute();
                    frameLayoutInfo.setVisibility(View.GONE);
                    fabRoute.setVisibility(View.GONE);
                }
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ArrayList<MarkerContenedor> mc = ((MainActivity) getActivity()).getContenedores();
                Contenedor c = null;
                for (MarkerContenedor markerContenedor : mc) {
                    if (markerContenedor.containsMarker(marker)) {
                        c = markerContenedor.getContenedor();
                        ((MainActivity) getActivity()).setContenedor(c);

                        showInfoMarkerSelected(c);
                    }
                }
                hideRoute();
                return true;
            }
        });

        ArrayList<MarkerContenedor> mc = ((MainActivity) getActivity()).getContenedores();
        if (!mc.isEmpty()) {
            for (MarkerContenedor markerContenedor : mc) {
                Marker m = createMarker(markerContenedor.getContenedor());
                markerContenedor.setMarker(m);
                Contenedor c = ((MainActivity) getActivity()).getContenedor();
                if (c != null && c.equals(markerContenedor.getContenedor())) {
                    showInfoMarkerSelected(c);
                }
            }
        }

        LatLng valencia = new LatLng(39.469779, -0.376426);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(valencia, 14.0f));

    }

    public void ubicarMiCasa() {
        if (tipo == 8) {
            MarkerOptions marker = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_home))
                    .position(pointHome);
            mMap.addMarker(marker);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        updateMyLastLocation();
        if (mLastLocation != null) {
            LatLng user = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            moveCameraToLocation(user, ZOOM_LEVEL);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null) {
            SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.settings_preference), Context.MODE_PRIVATE);
            int mapType = preferences.getInt(getString(R.string.map_key), 1);
            mMap.setMapType(mapType);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(REFRESH_GPS);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mGoogleApiClient.disconnect();
    }

    private void updateMyLastLocation() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void requestContenedoresNearLocation(Location location) {
        if (location != null) {
            consulta = new ContenedoresTask(location);
            consulta.execute();
        }
    }

    private void moveCameraToLocation(LatLng position, float zoom) {
        if (position != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position)
                    .zoom(zoom)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mLastLocation != null && tipo == 7 && !cercanos) {
            cercanos = true;
            setTipo(7);
        }
    }

    private void showInfoMarkerSelected(Contenedor c) {
        infoContenedorFragment = new InfoContenedorFragment();

        frameLayoutInfo.removeAllViews();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.info_contenedor, infoContenedorFragment)
                .commit();
        frameLayoutInfo.setVisibility(View.VISIBLE);
        fabRoute.setVisibility(View.VISIBLE);

        moveCameraToLocation(c.getLocation(), ZOOM_IN);
    }

    private void showInfoRoute(Route r) {
        routeFragment = new RouteFragment();
        routeFragment.setRuta(r);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.route_contenedor, routeFragment)
                .commit();
        frameLayoutRoute.setVisibility(View.VISIBLE);
    }

    public void setTipo(int tipo) {
        try{
            consulta.cancel(true);
        }catch (Exception e){

        }
        if (tipo != 7) {
            this.tipo = tipo;
        }
        if (this.tipo == 8) {
            SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.settings_preference), getActivity().MODE_PRIVATE);
            double latHome = Double.valueOf(sp.getString("lat_dir", "0.0"));
            double lngHome = Double.valueOf(sp.getString("lng_dir", "0.0"));
            pointHome = new LatLng(latHome, lngHome);
            moveCameraToLocation(pointHome, ZOOM_LEVEL);
            Location locationHome = new Location("");
            locationHome.setLatitude(latHome);
            locationHome.setLongitude(lngHome);
            if(bd.contenedoresIsClean())
                requestContenedoresNearLocation(locationHome);
            else {
                mMap.clear();
                progressBar.setVisibility(View.VISIBLE);
                ArrayList<Contenedor> contenedores=bd.returnContenedores();
                dibujarContenedores(contenedores);
                progressBar.setVisibility(View.GONE);
            }
        } else {
            updateMyLastLocation();
            if (mLastLocation != null) {
                requestContenedoresNearLocation(mLastLocation);
            }
        }
    }

    private Marker createMarker(Contenedor c) {
        TypedArray iconos = getResources().obtainTypedArray(R.array.tipo_marcadores);
        MarkerOptions marker = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(iconos.getResourceId(c.getTipo(), R.mipmap.ic_launcher)))
                .position(c.getLocation());
        return mMap.addMarker(marker);
    }

    public boolean isInfoShowed() {
        int visibility = frameLayoutInfo.getVisibility();
        switch (visibility) {
            case View.VISIBLE:
                return true;
            case View.INVISIBLE:
            case View.GONE:
                return false;
            default:
                return false;
        }
    }

    public void hideDetalles() {
        infoContenedorFragment.hideDetalles();
        fabLocation.setVisibility(View.VISIBLE);
        fabRoute.setVisibility(View.VISIBLE);
        if (polyline != null)
            frameLayoutRoute.setVisibility(View.VISIBLE);
    }

    private void hideRoute() {
        if (polyline != null) {
            polyline.remove();
            polyline = null;
            frameLayoutRoute.setVisibility(View.GONE);
        }
    }

    public void hideInfoContenedor() {
        infoContenedorFragment = null;
        hideRoute();
        frameLayoutInfo.setVisibility(View.GONE);
        fabRoute.setVisibility(View.GONE);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL), 1000, null);
    }

    public GoogleMap getmMap() {
        return mMap;
    }

    public InfoContenedorFragment getInfoContenedorFragment() {
        return infoContenedorFragment;
    }

    public boolean isDetallesHide() {
        if (infoContenedorFragment == null) return true;
        else return infoContenedorFragment.isDetallesHide();
    }

    private class ContenedoresTask extends AsyncTask<Void, Void, ArrayList<Contenedor>> {

        private final int PAPELERA = 0;
        private final int ORGANICO = 1;
        private final int CARTON = 2;
        private final int PLASTICO = 3;
        private final int VIDRIO = 4;
        private final int ACEITE = 5;
        private final int PILAS = 6;
        private final int CERCANOS = 7;
        private final int HOMECERCANOS = 8;

        private final String JSON_ARRAYCONTENEDORES = "contenedores";
        private final String JSON_IDCONTENEDOR = "id";
        private final String JSON_TIPOCONTENEDOR = "tipo";
        private final String JSON_DIRECCRIONCONTENEDOR = "direccion";
        private final String JSON_LATCONTENEDOR = "lat";
        private final String JSON_LONGCONTENEDOR = "log";

        private final String URL_PAPELERAS = "http://matumizi.ddns.net/GreenpointOpenData/v1/papeleras?";
        private final String URL_CONTENEDORES = "http://matumizi.ddns.net/GreenpointOpenData/v1/contenedores?";
        private final String URL_PILAS = "http://matumizi.ddns.net/GreenpointOpenData/v1/pilas?";
        private final String URL_ACEITE = "http://matumizi.ddns.net/GreenpointOpenData/v1/aceite?";
        private final String URL_CERCANOS = "http://matumizi.ddns.net/GreenpointOpenData/v1/cercanos?";

        private Location location;

        public ContenedoresTask(Location location) {
            this.location = location;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            mMap.clear();
            if (isInfoShowed()) {
                hideInfoContenedor();
            }
            if (tipo != 8) {
                LatLng user = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                moveCameraToLocation(user, ZOOM_LEVEL);
            } else {

            }
        }

        @Override
        protected ArrayList<Contenedor> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            Uri uri = buildUri();
            //creada la url se setea la variable tipo a -1
            if (tipo == 7) {
                tipo = -1;
            }
            ArrayList<Contenedor> contenedores = new ArrayList<Contenedor>();

            try {

                    URL url = new URL(uri.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                if(isCancelled()){
                    urlConnection.disconnect();
                    cancel(true);
                }
                    String data = readReturnDataFromConnection(urlConnection);
                    // leer JSON
                    contenedores = getDataFromJson(data);
                if(isCancelled()){
                    urlConnection.disconnect();
                    cancel(true);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return contenedores;
        }

        private String readReturnDataFromConnection(HttpURLConnection connection) throws IOException {
            if(isCancelled()){
                connection.disconnect();
                cancel(true);
            }
            InputStream inputStream = connection.getInputStream();
            if(isCancelled()){
                connection.disconnect();
                cancel(true);
            }
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
            if(isCancelled()){
                connection.disconnect();
                cancel(true);
            }
            return buffer.toString();
        }

        private Uri buildUri() {
            Uri url = null;
            int dist = preferences.getInt(getString(R.string.radio_key), 200);
            final String LAT_PARAM = "lat";
            final String LONG_PARAM = "long";
            final String TIPO_PARAM = "tipo";
            final String DIST_PARAM = "dist";
            switch (tipo) {
                case PAPELERA:
                    url = Uri.parse(URL_PAPELERAS).buildUpon()
                            .appendQueryParameter(LAT_PARAM, location.getLatitude() + "")
                            .appendQueryParameter(LONG_PARAM, location.getLongitude() + "")
                            .appendQueryParameter(DIST_PARAM, dist + "")
                            .build();
                    break;
                case ORGANICO:
                case CARTON:
                case PLASTICO:
                case VIDRIO:
                    url = Uri.parse(URL_CONTENEDORES).buildUpon()
                            .appendQueryParameter(TIPO_PARAM, tipo + "")
                            .appendQueryParameter(LAT_PARAM, location.getLatitude() + "")
                            .appendQueryParameter(LONG_PARAM, location.getLongitude() + "")
                            .appendQueryParameter(DIST_PARAM, dist + "")
                            .build();
                    break;
                case ACEITE:
                    url = Uri.parse(URL_ACEITE).buildUpon()
                            .appendQueryParameter(LAT_PARAM, location.getLatitude() + "")
                            .appendQueryParameter(LONG_PARAM, location.getLongitude() + "")
                            .appendQueryParameter(DIST_PARAM, dist + "")
                            .build();
                    break;
                case PILAS:
                    url = Uri.parse(URL_PILAS).buildUpon()
                            .appendQueryParameter(LAT_PARAM, location.getLatitude() + "")
                            .appendQueryParameter(LONG_PARAM, location.getLongitude() + "")
                            .appendQueryParameter(DIST_PARAM, dist + "")
                            .build();
                    break;
                case CERCANOS:
                    url = Uri.parse(URL_CERCANOS).buildUpon()
                            .appendQueryParameter(LAT_PARAM, location.getLatitude() + "")
                            .appendQueryParameter(LONG_PARAM, location.getLongitude() + "")
                            .build();
                    break;
                case HOMECERCANOS:
                    url = Uri.parse(URL_CERCANOS).buildUpon()
                            .appendQueryParameter(LAT_PARAM, location.getLatitude() + "")
                            .appendQueryParameter(LONG_PARAM, location.getLongitude() + "")
                            .appendQueryParameter(DIST_PARAM, dist + "")
                            .build();
                    break;
            }
            return url;
        }

        private ArrayList<Contenedor> getDataFromJson(String data) {
            ArrayList<Contenedor> contenedoresJSON = new ArrayList<Contenedor>();
            Contenedor contenedor;
            try {
                JSONObject jsonObjectJson = new JSONObject(data);
                JSONArray jsonArrayJson = jsonObjectJson.getJSONArray(JSON_ARRAYCONTENEDORES);
                JSONObject temporal;
                for (int i = 0; i < jsonArrayJson.length(); i++) {
                    temporal = jsonArrayJson.getJSONObject(i);
                    contenedor = new Contenedor();
                    contenedor.setId((int) temporal.get(JSON_IDCONTENEDOR));
                    contenedor.setTipo((int) temporal.get(JSON_TIPOCONTENEDOR));
                    contenedor.setDireccion((String) temporal.get(JSON_DIRECCRIONCONTENEDOR));
                    LatLng location = new LatLng((double) temporal.get(JSON_LATCONTENEDOR), (double) temporal.get(JSON_LONGCONTENEDOR));
                    contenedor.setLocation(location);
                    contenedoresJSON.add(contenedor);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return contenedoresJSON;
        }

        @Override
        protected void onPostExecute(ArrayList<Contenedor> arrayList) {
            super.onPostExecute(arrayList);
            if(isCancelled()){
                cancel(true);
            }
            mMap.clear();
            if (tipo == 8) {
                MarkerOptions marker = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_home))
                        .position(pointHome);
                mMap.addMarker(marker);

                //base de datos
                if(bd.contenedoresIsClean()){
                    for (Contenedor c:arrayList) {
                        bd.insertContenedor(c);
                    }
                }

            }
            if (isInfoShowed()) {
                infoContenedorFragment = null;
                frameLayoutInfo.setVisibility(View.GONE);
                fabRoute.setVisibility(View.GONE);
            }
            if (!arrayList.isEmpty()) {
                ((MainActivity) getActivity()).setContenedores(new ArrayList<MarkerContenedor>());
                for (Contenedor c : arrayList) {
                    Marker m = createMarker(c);
                    ((MainActivity) getActivity()).getContenedores().add(new MarkerContenedor(c, m));
                }
            } else
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.no_result), Toast.LENGTH_LONG)
                        .show();
            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled(ArrayList<Contenedor> arrayList) {
            super.onCancelled(arrayList);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void dibujarContenedores(ArrayList<Contenedor> arrayList) {
        mMap.clear();
            MarkerOptions marker = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_home))
                    .position(pointHome);
            mMap.addMarker(marker);
        if (isInfoShowed()) {
            infoContenedorFragment = null;
            frameLayoutInfo.setVisibility(View.GONE);
            fabRoute.setVisibility(View.GONE);
        }
        if (!arrayList.isEmpty()) {
            ((MainActivity) getActivity()).setContenedores(new ArrayList<MarkerContenedor>());
            for (Contenedor c : arrayList) {
                Marker m = createMarker(c);
                ((MainActivity) getActivity()).getContenedores().add(new MarkerContenedor(c, m));
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.no_result), Toast.LENGTH_LONG)
                    .show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private class MakeRoute extends AsyncTask<Double, Void, Route> {

        private final String URL = "https://maps.googleapis.com/maps/api/directions/json";

        private final String JSON_ROUTES = "routes";
        private final String JOSN_LEGS = "legs";
        private final String JSON_DISTANCE = "distance";
        private final String JOSN_TEXT = "text";
        private final String JSON_DURATION = "duration";
        private final String JSON_STEPS = "steps";
        private final String JSON_POLYLINE = "polyline";
        private final String JSON_POINTS = "points";

        public MakeRoute() {
        }

        @Override
        protected Route doInBackground(Double... params) {
            HttpURLConnection urlConnection = null;
            Uri uri = buildURI(params);

            URL url = null;
            Route route = null;
            try {
                url = new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                String data = readReturnDataFromConnection(urlConnection);

                route = getDataFromJson(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return route;
        }

        @Override
        protected void onPostExecute(Route route) {
            super.onPostExecute(route);
            if (route != null) {
                PolylineOptions polylineOptions = new PolylineOptions()
                        .geodesic(true)
                        .addAll(route.getArrayList())
                        .color(getActivity().getResources().getColor(R.color.verde2))
                        .width(22f);
                polyline = mMap.addPolyline(polylineOptions);
                showInfoRoute(route);
            }
        }

        private Route getDataFromJson(String data) {
            Route r = new Route();
            try {
                JSONObject jsonObject = new JSONObject(data);
                JSONArray jsonArray = jsonObject.getJSONArray(JSON_ROUTES);
                jsonObject = jsonArray.getJSONObject(0);
                jsonArray = jsonObject.getJSONArray(JOSN_LEGS);
                jsonObject = jsonArray.getJSONObject(0);
                String dist = jsonObject.getJSONObject(JSON_DISTANCE).getString(JOSN_TEXT);
                String time = jsonObject.getJSONObject(JSON_DURATION).getString(JOSN_TEXT);
                jsonArray = jsonObject.optJSONArray(JSON_STEPS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    jsonObject = jsonObject.getJSONObject(JSON_POLYLINE);
                    String point = jsonObject.getString(JSON_POINTS);
                    List<LatLng> locations = decodePoly(point);
                    r.getArrayList().addAll(locations);
                }
                r.setDistance(dist);
                r.setTime(time);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return r;
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

        private Uri buildURI(Double... params) {
            final String ORIGEN = "origin";
            final String DESTINO = "destination";
            final String SENSOR = "sensor";
            final String UNIDADES = "units";
            final String MODO = "mode";
            Uri url = Uri.parse(URL).buildUpon()
                    .appendQueryParameter(ORIGEN, mLastLocation.getLatitude() + "," + mLastLocation.getLongitude())
                    .appendQueryParameter(DESTINO, params[0] + "," + params[1])
                    .appendQueryParameter(SENSOR, "false")
                    .appendQueryParameter(UNIDADES, "metric")
                    .appendQueryParameter(MODO, "walking")
                    .build();
            return url;
        }

        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }
    }
}
