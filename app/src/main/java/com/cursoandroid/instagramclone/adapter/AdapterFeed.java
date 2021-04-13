package com.cursoandroid.instagramclone.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cursoandroid.instagramclone.R;
import com.cursoandroid.instagramclone.activity.ComentariosActivity;
import com.cursoandroid.instagramclone.helper.ConfiguracaoFirebase;
import com.cursoandroid.instagramclone.helper.UsuarioFirebase;
import com.cursoandroid.instagramclone.model.Feed;
import com.cursoandroid.instagramclone.model.PostagensCurtidas;
import com.cursoandroid.instagramclone.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFeed extends RecyclerView.Adapter<AdapterFeed.MyViewHolder> {

    private List<Feed> listaFeed;
    private Context context;
    private DatabaseReference curtidasRef = ConfiguracaoFirebase.getReferenceFirebase()
            .child("postagens-curtidas");
    private DatabaseReference curtidaRefFeed;

    public AdapterFeed(List<Feed> listaFeed, Context context) {
        this.listaFeed = listaFeed;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_feed,parent,false);
        return new AdapterFeed.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        final Feed feed = listaFeed.get(position);
        final Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //Carregando dados do feed
        if (feed.getFotoPostagem() != null){
            Uri uriFotoPostagem = Uri.parse(feed.getFotoPostagem());
            Glide.with(context).load(uriFotoPostagem).into(holder.fotoPostagem);
        }
        if (feed.getFotoUsuario() != null){
            Uri uriFotoUsuario = Uri.parse(feed.getFotoUsuario());
            Glide.with(context).load(uriFotoUsuario).into(holder.fotoPerfil);
        }
        holder.descricao.setText(feed.getDescricao());
        holder.nome.setText(feed.getNomeUsuario());

        //Adicionar evento de clique nos comentarios
        holder.visualizarComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ComentariosActivity.class);
                intent.putExtra("idPostagem",feed.getId());
                context.startActivity(intent);
            }
        });

        curtidaRefFeed = curtidasRef.child(feed.getId());

        curtidaRefFeed.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int qtdCurtidas = 0;
                        if (snapshot.hasChild("qtdCurtidas") ){
                            PostagensCurtidas postagensCurtidas = snapshot.getValue(PostagensCurtidas.class);
                            qtdCurtidas = postagensCurtidas.getQtdCurtidas();
                        }
                        //Verificar se já foi clicado
                        if (snapshot.hasChild(usuarioLogado.getId())){
                            holder.likeButton.setLiked(true);
                        }else {
                            holder.likeButton.setLiked(false);
                        }

                        //Montar um obejto postagem curdtidas
                        final PostagensCurtidas curtida = new PostagensCurtidas();
                        curtida.setFeed(feed);
                        curtida.setUsuario(usuarioLogado);
                        curtida.setQtdCurtidas(qtdCurtidas);

                        //Adicionar evento para curtir uma foto
                        holder.likeButton.setOnLikeListener(new OnLikeListener() {
                            @Override
                            public void liked(LikeButton likeButton) {
                                curtida.salvar();
                                holder.qtdsCurtidas.setText(curtida.getQtdCurtidas() + " curtidas");
                            }

                            @Override
                            public void unLiked(LikeButton likeButton) {
                                curtida.removerCurtida();
                                holder.qtdsCurtidas.setText(curtida.getQtdCurtidas() + " curtidas");
                            }
                        });
                        holder.qtdsCurtidas.setText(curtida.getQtdCurtidas() + " curtidas");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );

    }

    @Override
    public int getItemCount() {
        return listaFeed.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView fotoPerfil;
        TextView nome, descricao, qtdsCurtidas;
        ImageView fotoPostagem,visualizarComentario;
        LikeButton likeButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            fotoPerfil = itemView.findViewById(R.id.imagePerfilPostagem);
            nome = itemView.findViewById(R.id.textPerfilPostagem);
            descricao = itemView.findViewById(R.id.textDescricaoPostagem);
            fotoPostagem = itemView.findViewById(R.id.imagePostagemSelecionada);
            qtdsCurtidas = itemView.findViewById(R.id.textQtCurtidasPostagem);
            visualizarComentario = itemView.findViewById(R.id.imageComentarioFeed);
            likeButton = itemView.findViewById(R.id.likeButtonFeed);

        }
    }

}
