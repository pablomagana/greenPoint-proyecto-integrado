package com.app.greenpoint;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.greenpoint.model.Route;


public class RouteFragment extends Fragment {

    private TextView duracion, distancia;
    private Route ruta;

    public RouteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route, container, false);

        duracion = (TextView) view.findViewById(R.id.time);
        distancia = (TextView) view.findViewById(R.id.dist);

        distancia.setText(ruta.getDistance());
        duracion.setText(ruta.getTime());

        return view;
    }

    public void setRuta(Route ruta) {
        this.ruta = ruta;
    }
}
