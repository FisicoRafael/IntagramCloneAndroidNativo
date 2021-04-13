package com.cursoandroid.instagramclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cursoandroid.instagramclone.R;
import com.cursoandroid.instagramclone.adapter.AdapterGrid;
import com.cursoandroid.instagramclone.helper.ConfiguracaoFirebase;
import com.cursoandroid.instagramclone.helper.UsuarioFirebase;
import com.cursoandroid.instagramclone.model.Postagem;
import com.cursoandroid.instagramclone.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilAmigoActivity extends AppCompatActivity {

    private Toolbar toolbarPrincipal;
    private Usuario usuarioSelecionado;
    private Usuario usuarioLogado;
    private Button buttonAcaoPerfil;
    private CircleImageView fotoUsuarioSelecionado;
    private GridView gridViewPerfil;

    private DatabaseReference firebaseRef;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioAmigoRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference seguidoresRef;
    private DatabaseReference postagemUsuarioRef;
    private ValueEventListener valueEventListenerPerfilAmigo;
    private TextView campoPostagens, campoSeguidores, campoSeguindo;
    private AdapterGrid adapterGrid;

    private String idUsuarioLogdo;
    private List<Postagem> listaPostagens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_amigo);
        toolbarPrincipal = findViewById(R.id.toolbarPrincipal);
        toolbarPrincipal.setTitle("Perfil");
        setSupportActionBar(toolbarPrincipal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        inicializarComponentes();
        configuracoesIniciais();

        //Recuperar usuario selecionado
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            usuarioSelecionado = (Usuario) bundle.getSerializable("usuarioselecionado");
            //Configurar o nome do usuario amigo na Toolbar
            getSupportActionBar().setTitle(usuarioSelecionado.getNome());
            postagemUsuarioRef = ConfiguracaoFirebase.getReferenceFirebase()
                    .child("postagens")
                    .child(usuarioSelecionado.getId());
            //Configura a foto do usuario amigo
            if (usuarioSelecionado.getCaminhoFoto() != null) {
                Uri uri = Uri.parse(usuarioSelecionado.getCaminhoFoto());
                Glide.with(PerfilAmigoActivity.this).load(uri).into(fotoUsuarioSelecionado);
            }
        }
        inicializarImageLoader();
        carregagarFotosPostagens();

        //Abre a foto clicada
        gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Postagem postagem = listaPostagens.get(position);
                Intent intent = new Intent(getApplicationContext(),VisualizarPostagem.class);
                intent.putExtra("postagem",postagem);
                intent.putExtra("usuario",usuarioSelecionado);
                startActivity(intent);
            }
        });
    }

    public void inicializarImageLoader(){

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);

    }

    public void carregagarFotosPostagens(){

        //Recupera as fotos postadas pelo usuario
        listaPostagens = new ArrayList<>();
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
                            listaPostagens.add(postagem);
                            urlsFotos.add(postagem.getCaminhoFoto());
                        }
                      //  campoPostagens.setText(String.valueOf(urlsFotos.size()));

                        //Configurar o adapter
                        adapterGrid = new AdapterGrid(getApplicationContext(),R.layout.grid_postagem,urlsFotos);
                        gridViewPerfil.setAdapter(adapterGrid);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );


    }

    private void recuperarDadosUsuarioLogado() {

        usuarioLogadoRef = usuariosRef.child(idUsuarioLogdo);
        usuarioLogadoRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usuarioLogado = snapshot.getValue(Usuario.class);
                        //Verificar se o usuario já esta seguindo o amigo selecionado
                        verificaSegueUsuarioAmigo();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );

    }

    private void inicializarComponentes() {
        //Inicializar os componentes
        buttonAcaoPerfil = findViewById(R.id.buttonAcaorPerfil);
        buttonAcaoPerfil.setText("Carregando");
        fotoUsuarioSelecionado = findViewById(R.id.imageEditarPerfil);
        campoPostagens = findViewById(R.id.textPerfilPublicacoes);
        campoSeguidores = findViewById(R.id.textPerfilSeguidores);
        campoSeguindo = findViewById(R.id.textPerfilSeguindo);
        gridViewPerfil = findViewById(R.id.gridViewPerfil);
    }

    private void configuracoesIniciais() {
        //Configurações Iniciais
        firebaseRef = ConfiguracaoFirebase.getReferenceFirebase();
        usuariosRef = firebaseRef.child("usuarios");
        seguidoresRef = firebaseRef.child("seguidores");
        idUsuarioLogdo = UsuarioFirebase.getIdUsuario();
    }

    private void verificaSegueUsuarioAmigo() {

        DatabaseReference seguidorRef = seguidoresRef
                .child(usuarioSelecionado.getId())
                .child(idUsuarioLogdo);

        seguidoresRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Caso já esteja seguindo
                            Log.i("dadosUsuario", "Seguindo");
                            habilitarBotaoseguir(true);
                        } else {
                            // Caso não esteja seguindo
                            Log.i("dadosUsuario", "não seguindo");
                            habilitarBotaoseguir(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );

    }

    private void habilitarBotaoseguir(boolean segueUsuario) {
        if (segueUsuario) {
            buttonAcaoPerfil.setText("Seguindo");
        } else {
            buttonAcaoPerfil.setText("Seguir");
        }
        buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarSeguidor(usuarioLogado, usuarioSelecionado);
            }
        });
    }

    private void salvarSeguidor(Usuario uLogado, Usuario uAmigo) {

        HashMap<String, Object> dadosLogado = new HashMap<>();
        dadosLogado.put("nome", uLogado.getNome());
        dadosLogado.put("caminhoFoto", uLogado.getCaminhoFoto());
        DatabaseReference seguidorRef = seguidoresRef
                .child(uAmigo.getId())
                .child(uLogado.getId());
        seguidorRef.setValue(dadosLogado);

        //Alterar texto do botão
        buttonAcaoPerfil.setText("Seguindo");
        buttonAcaoPerfil.setOnClickListener(null);

        //Incrementar seguindo no usuario logado
        int seguindo = uLogado.getSeguindo() + 1;
        HashMap<String, Object> dadosSeguindo = new HashMap<>();
        dadosSeguindo.put("seguindo", seguindo);
        DatabaseReference usuarioSeguindo = usuariosRef
                .child(uLogado.getId());
        usuarioSeguindo.updateChildren(dadosSeguindo);

        //Incrementar seguidores no amigo
        int seguidores = uAmigo.getSeguidores() + 1;
        HashMap<String, Object> dadosSeguidores = new HashMap<>();
        dadosSeguidores.put("seguidores", seguidores);
        DatabaseReference usuarioSeguidores = usuariosRef
                .child(uAmigo.getId());
        usuarioSeguidores.updateChildren(dadosSeguidores);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarDadosPerfilAmigo();
        recuperarDadosUsuarioLogado();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioAmigoRef.removeEventListener(valueEventListenerPerfilAmigo);
    }

    private void recuperarDadosPerfilAmigo() {

        usuarioAmigoRef = usuariosRef.child(usuarioSelecionado.getId());
        valueEventListenerPerfilAmigo = usuarioAmigoRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Usuario usuario = snapshot.getValue(Usuario.class);
                        String postagens = String.valueOf(usuario.getPostagens());
                        String seguindo = String.valueOf(usuario.getSeguindo());
                        String seguidores = String.valueOf(usuario.getSeguidores());

                        campoPostagens.setText(postagens);
                        campoSeguindo.setText(seguindo);
                        campoSeguidores.setText(seguidores);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );


    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}