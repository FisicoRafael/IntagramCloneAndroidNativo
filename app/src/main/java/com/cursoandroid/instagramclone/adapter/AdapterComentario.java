package com.cursoandroid.instagramclone.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cursoandroid.instagramclone.R;
import com.cursoandroid.instagramclone.model.Comentario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterComentario extends RecyclerView.Adapter <AdapterComentario.MyViewHolder>{

    private List<Comentario> listaComentario;
    private Context context;

    public AdapterComentario(List<Comentario> listaComentario, Context context) {
        this.listaComentario = listaComentario;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comentarios,parent,false);
        return new AdapterComentario.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Comentario comentario = listaComentario.get(position);

        Uri uriNome = Uri.parse(comentario.getCaminhoFoto());
        Glide.with(context).load(uriNome).into(holder.fotoPerfil);
        holder.nomeUsuario.setText(comentario.getNomeUsuario());
        holder.comentarioUsuario.setText(comentario.getComentario());

    }

    @Override
    public int getItemCount() {
        return listaComentario.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView fotoPerfil;
        TextView nomeUsuario, comentarioUsuario;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            fotoPerfil = itemView.findViewById(R.id.imageUsuarioComentario);
            nomeUsuario = itemView.findViewById(R.id.textUsuarioNomeComentario);
            comentarioUsuario = itemView.findViewById(R.id.textUsuarioComentario);

        }
    }

}
