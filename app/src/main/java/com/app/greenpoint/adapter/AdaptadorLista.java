package com.app.greenpoint.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.greenpoint.R;
import com.app.greenpoint.model.Comentario;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

public class AdaptadorLista extends RecyclerView.Adapter<AdaptadorLista.ComentarioViewHolder> {

    private ArrayList<Comentario> comentarios;
    private Context context;

    public AdaptadorLista(ArrayList<Comentario> comentarios, Context context) {
        this.context = context;
        if(comentarios==null){
            this.comentarios=new ArrayList<Comentario>();
        }else {
            this.comentarios = comentarios;
        }
    }

    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    @Override
    public ComentarioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);
        ComentarioViewHolder comentarioViewHolder = new ComentarioViewHolder(view);
        return comentarioViewHolder;
    }

    public void addItem(Comentario c){
        comentarios.add(0,c);
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(ComentarioViewHolder holder, int position) {
        Comentario comentario = comentarios.get(position);
        holder.nombre.setText(comentario.getNomUsuario());
        holder.fecha.setText(comentario.getFecha());
        holder.texto.setText(comentario.getTexto());
        if (!comentario.getEncodedImg().equals("") && !comentario.getEncodedImg().equals("null")) {
        byte[] data = Base64.decode(comentario.getEncodedImg(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        holder.icono.setImageBitmap(bitmap);}
        else
            holder.icono.setImageResource(R.mipmap.ic_launcher);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class ComentarioViewHolder extends RecyclerView.ViewHolder {

        private CircularImageView icono;
        private TextView nombre, fecha, texto;

        public ComentarioViewHolder(View itemView) {
            super(itemView);

            icono = (CircularImageView) itemView.findViewById(R.id.imagen);
            nombre = (TextView) itemView.findViewById(R.id.nombre_usuario);
            fecha = (TextView) itemView.findViewById(R.id.fecha);
            texto = (TextView) itemView.findViewById(R.id.cuerpo);
        }
    }
}
