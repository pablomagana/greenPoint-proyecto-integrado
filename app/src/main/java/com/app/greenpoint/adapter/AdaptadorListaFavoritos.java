package com.app.greenpoint.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.greenpoint.R;
import com.app.greenpoint.data.GreenpointDBAdapter;
import com.app.greenpoint.model.Contenedor;
import com.app.greenpoint.model.Favorito;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AdaptadorListaFavoritos extends RecyclerView.Adapter<AdaptadorListaFavoritos.FavoritoViewHolder> {

    ArrayList<Favorito> favoritos;
    Context context;
    FavoritoViewHolder viewHolder;

    public AdaptadorListaFavoritos(ArrayList<Favorito> favoritos, Context context) {
        this.favoritos = favoritos;
        this.context = context;
        }

    @Override
    public int getItemCount() {
        return favoritos.size();
        }

    @Override
    public FavoritoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorito, parent, false);
        viewHolder = new FavoritoViewHolder(view);
        return viewHolder;
        }


    public void onBindViewHolder(FavoritoViewHolder holder, int position) {
        final Favorito favorito = favoritos.get(position);

        TypedArray iconos = context.getResources().obtainTypedArray(R.array.tipo_iconos);
        String[] nombres = context.getResources().getStringArray(R.array.tipo_contenedores);

        holder.tipo.setText(nombres[favorito.getContenedor().getTipo()]);
        holder.direccion.setText(favorito.getContenedor().getDireccion());

        holder.icono.setImageResource(iconos.getResourceId(favorito.getContenedor().getTipo(), R.mipmap.ic_launcher));

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //lanzar mapa
            }
        });
        }

    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        }

    public class FavoritoViewHolder extends RecyclerView.ViewHolder {


        private TextView tipo, direccion;
        private ImageView icono;
        private LinearLayout layout;

        public FavoritoViewHolder(View itemView) {
            super(itemView);

            icono = (ImageView) itemView.findViewById(R.id.icono);
            tipo = (TextView) itemView.findViewById(R.id.tipo);
            direccion = (TextView) itemView.findViewById(R.id.direccion);
            layout = (LinearLayout) itemView.findViewById(R.id.main_layout);

        }
    }
}
