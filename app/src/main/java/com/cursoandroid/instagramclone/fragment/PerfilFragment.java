package com.cursoandroid.instagramclone.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cursoandroid.instagramclone.R;
import com.cursoandroid.instagramclone.activity.EditarPerfilActivity;
import com.cursoandroid.instagramclone.adapter.AdapterGrid;
import com.cursoandroid.instagramclone.helper.ConfiguracaoFirebase;
import com.cursoandroid.instagramclone.helper.UsuarioFirebase;
import com.cursoandroid.instagramclone.model.Postagem;
import com.cursoandroid.instagramclone.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class PerfilFragment extends Fragment {

    private ProgressBar progressBar;
    private CircleImageView imagePerfil;
    private GridView gridViewPerfil;
    private TextView campoPublicacoes, campoSeguidores, campoSeguindo;
    private Button buttonAcaoPerfil;

    private DatabaseReference firebaseRef;
    private DatabaseReference usuarioAtualRef;
    private FirebaseUser usuarioAtual;
    private ValueEventListener valueEventListenerPerfilAtual;

    private DatabaseReference postagemUsuarioRef;
    private AdapterGrid adapterGrid;

    public PerfilFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        //Configurações dos componentes
        progressBar        = view.findViewById(R.id.progressBarPerfil);
        imagePerfil        = view.findViewById(R.id.imageEditarPerfil);
        gridViewPerfil        = view.findViewById(R.id.gridViewPerfil);
        campoPublicacoes        = view.findViewById(R.id.textPerfilPublicacoes);
        campoSeguidores        = view.findViewById(R.id.textPerfilSeguidores);
        campoSeguindo        = view.findViewById(R.id.textPerfilSeguindo);
        buttonAcaoPerfil = view.findViewById(R.id.buttonAcaorPerfil);
        progressBar.setVisibility(View.GONE);
        //Configurações Iniciais
        firebaseRef = ConfiguracaoFirebase.getReferenceFirebase()
                    .child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();
        usuarioAtualRef = firebaseRef.child(usuarioAtual.getUid());
        postagemUsuarioRef = ConfiguracaoFirebase.getReferenceFirebase()
                .child("postagens")
                .child(usuarioAtual.getUid());


        //Abrir a edição de perfil
        buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditarPerfilActivity.class);
                startActivity(intent);
            }
        });

        inicializarImageLoader();
        carregagarFotosPostagens();

        return view;
    }
    public void carregagarFotosPostagens(){

        //Recupera as fotos postadas pelo usuario
        postagemUsuarioRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //Configurar o tamanho do grid
                        int tamanhoGrid = getResources().getDisplayMetrics().widthPixels;
                        int tamanhoImagem = tamanhoGrid/3;
                        gridViewPerfil.setColumnWidth(tamanhoImagem);

                        List<String> urlsFotos = new ArrayList<>();
                        for (DataSnapshot sp : snapshot.getChildren()){
                            Postagem postagem = sp.getValue(Postagem.class);
                            urlsFotos.add(postagem.getCaminhoFoto());
                        }
                        //campoPublicacoes.setText(String.valueOf(urlsFotos.size()));

                        //Configurar o adapter
                        adapterGrid = new AdapterGrid(getActivity(),R.layout.grid_postagem,urlsFotos);
                        gridViewPerfil.setAdapter(adapterGrid);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );


    }

    public void inicializarImageLoader(){

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(getActivity())
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);

    }

    private void recuperarFotoUsuario(){
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();
        Uri uri = usuarioAtual.getPhotoUrl();
        if (uri != null){
            Glide.with(getActivity())
                    .load(uri)
                    .into(imagePerfil);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarDadosUsuarioLogado();
        recuperarFotoUsuario();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioAtualRef.removeEventListener(valueEventListenerPerfilAtual);
    }

    private void recuperarDadosUsuarioLogado() {

        valueEventListenerPerfilAtual = usuarioAtualRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Usuario usuario = snapshot.getValue(Usuario.class);
                        String seguindo = String.valueOf(usuario.getSeguindo());
                        String seguidores = String.valueOf(usuario.getSeguidores());
                        String postagens = String.valueOf(usuario.getPostagens());

                        campoSeguindo.setText(seguindo);
                        campoSeguidores.setText(seguidores);
                        campoPublicacoes.setText(postagens);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );



    }
}