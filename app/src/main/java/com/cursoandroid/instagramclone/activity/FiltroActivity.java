package com.cursoandroid.instagramclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.cursoandroid.instagramclone.R;
import com.cursoandroid.instagramclone.adapter.AdapterMiniaturas;
import com.cursoandroid.instagramclone.helper.ConfiguracaoFirebase;
import com.cursoandroid.instagramclone.helper.RecyclerItemClickListener;
import com.cursoandroid.instagramclone.helper.UsuarioFirebase;
import com.cursoandroid.instagramclone.model.Postagem;
import com.cursoandroid.instagramclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FiltroActivity extends AppCompatActivity {
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private Toolbar toolbarPrincipal;
    private ImageView imageFotoEscolhida;
    private Bitmap imagem;
    private Bitmap imagemFiltro;
    private TextInputEditText textDescricaoFiltro;
    private List<ThumbnailItem> listaFiltros;
    private String idUsuarioLogado;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference usuariosRef;
    private Usuario usuarioLogado;
    private DatabaseReference firebaseRef;
    private AlertDialog dialog;
    private DataSnapshot seguidoresSnapshot;

    private RecyclerView recyclerFiltros;
    private AdapterMiniaturas adapterMiniaturas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro);
        toolbarPrincipal = findViewById(R.id.toolbarPrincipal);
        toolbarPrincipal.setTitle("Filtros");
        setSupportActionBar(toolbarPrincipal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        //Inicializar o componentes
        imageFotoEscolhida = findViewById(R.id.imageFotoEscolhida);
        listaFiltros = new ArrayList<>();
        recyclerFiltros = findViewById(R.id.recyclerFiltros);
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();
        textDescricaoFiltro = findViewById(R.id.textDescricaoFiltro);
        firebaseRef = ConfiguracaoFirebase.getReferenceFirebase();
        usuariosRef = firebaseRef.child("usuarios");


        recuperarDadosPostagem();


        //recuperar a imagem escolhida pelo usuario
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            byte[] dadosImagem = bundle.getByteArray("fotoEscolhida");
            imagem = BitmapFactory.decodeByteArray(dadosImagem, 0, dadosImagem.length);
            imageFotoEscolhida.setImageBitmap(imagem);
            imagemFiltro = imagem.copy(imagem.getConfig(), true);

            //Configura????o do recycler filtro
            adapterMiniaturas = new AdapterMiniaturas(listaFiltros, getApplicationContext());
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recyclerFiltros.setLayoutManager(layoutManager);
            recyclerFiltros.setAdapter(adapterMiniaturas);

            //Adicionar um evento de clique no RecyclerView
            recyclerFiltros.addOnItemTouchListener(
                    new RecyclerItemClickListener(
                            getApplicationContext(),
                            recyclerFiltros,
                            new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {

                                    ThumbnailItem item = listaFiltros.get(position);
                                    imagemFiltro = imagem.copy(imagem.getConfig(), true);
                                    Filter filter = item.filter;
                                    imageFotoEscolhida.setImageBitmap(filter.processFilter(imagemFiltro));
                                }

                                @Override
                                public void onLongItemClick(View view, int position) {

                                }

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                }
                            }
                    )
            );

            //Recuperar Filtros
            recuperarFiltros();


        }

    }

    private void abrirDialogCarregamento(String titulo) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(titulo);
        alert.setCancelable(false);
        alert.setView(R.layout.carregamento);

        dialog = alert.create();
        dialog.show();
    }


    private void recuperarDadosPostagem() {

        abrirDialogCarregamento("Carregando dados, aguarde!");
        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usuarioLogado = snapshot.getValue(Usuario.class);

                        //Recuperar seguidores
                        DatabaseReference seguidoresRef = firebaseRef.child("seguidores")
                                .child(idUsuarioLogado);
                        seguidoresRef.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        seguidoresSnapshot = snapshot;
                                        dialog.cancel();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );

    }


    private void recuperarFiltros() {

        //Limpar itens
        ThumbnailsManager.clearThumbs();
        listaFiltros.clear();

        //Configurar filtro normal
        ThumbnailItem item = new ThumbnailItem();
        item.image = imagem;
        item.filterName = "Normal";
        ThumbnailsManager.addThumb(item);

        //Lista todos os filtros
        List<Filter> filtros = FilterPack.getFilterPack(getApplicationContext());
        for (Filter filtro : filtros) {

            ThumbnailItem itemFiltro = new ThumbnailItem();
            itemFiltro.image = imagem;
            itemFiltro.filter = filtro;
            itemFiltro.filterName = filtro.getName();
            ThumbnailsManager.addThumb(itemFiltro);
        }
        listaFiltros.addAll(ThumbnailsManager.processThumbs(getApplicationContext()));
        adapterMiniaturas.notifyDataSetChanged();
    }


    @Override  //Cria o layout de menu
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filtro, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override  // Configura o menu
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.ic_salvar_postagem:
                publicarPostagem();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void publicarPostagem() {

        abrirDialogCarregamento("Salvando postagem");
        final Postagem postagem = new Postagem();
        postagem.setIdUsuario(idUsuarioLogado);
        postagem.setDescricao(textDescricaoFiltro.getText().toString());

        //Recuperar dados da imagem para o Firebase
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagemFiltro.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] dadosImagem = baos.toByteArray();

        //Salvar Imagem no Firebase
        StorageReference storageRef = ConfiguracaoFirebase.getStorageReference();
        final StorageReference imagemRef = storageRef
                .child("imagens")
                .child("postagens")
                .child(postagem.getId() + ".jpeg");

        UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FiltroActivity.this,
                        "Erro ao salvar postagem",
                        Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri url = task.getResult();
                        postagem.setCaminhoFoto(url.toString());

                        //Atualizar a quantidade de postagens
                        int quantidadePostagens = usuarioLogado.getPostagens() + 1;
                        usuarioLogado.setPostagens(quantidadePostagens);
                        usuarioLogado.atualizarQtPostagens();

                        if (postagem.salvar(seguidoresSnapshot)) {
                            Toast.makeText(FiltroActivity.this,
                                    "Sucesso ao salvar postagem.",
                                    Toast.LENGTH_LONG).show();
                            dialog.cancel();
                            finish();
                        }

                    }
                });

            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}