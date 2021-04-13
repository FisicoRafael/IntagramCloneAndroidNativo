package com.cursoandroid.instagramclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cursoandroid.instagramclone.R;
import com.cursoandroid.instagramclone.adapter.AdapterComentario;
import com.cursoandroid.instagramclone.helper.ConfiguracaoFirebase;
import com.cursoandroid.instagramclone.helper.UsuarioFirebase;
import com.cursoandroid.instagramclone.model.Comentario;
import com.cursoandroid.instagramclone.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ComentariosActivity extends AppCompatActivity {

    private Toolbar toolbarPrincipal;
    private EditText editComentario;
    private RecyclerView recyclerComentario;
    private String idPostagem;
    private Usuario usuario;
    private List<Comentario> listaComentario = new ArrayList<>();
    private AdapterComentario adapterComentario;

    private DatabaseReference firebaseRef;
    private DatabaseReference comentariosRef;
    private ValueEventListener valueEventListenerComentarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);
        toolbarPrincipal = findViewById(R.id.toolbarPrincipal);
        toolbarPrincipal.setTitle("Comentarios");
        setSupportActionBar(toolbarPrincipal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        //INicializar componentes
        inicializarComponentes();

        //Configurações iniciais
        usuario = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef = ConfiguracaoFirebase.getReferenceFirebase();

        //Recuperar dados da postagem
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            idPostagem = bundle.getString("idPostagem");
        }

        adapterComentario = new AdapterComentario(listaComentario,getApplicationContext());

        recyclerComentario.setHasFixedSize(true);
        recyclerComentario.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerComentario.setAdapter(adapterComentario);

    }

    private void inicializarComponentes() {
        editComentario = findViewById(R.id.editTextComentario);
        recyclerComentario = findViewById(R.id.recyclerComentarios);
    }

    private void recuperarComentarios(){
        comentariosRef = firebaseRef.child("comentarios")
                        .child(idPostagem);

        valueEventListenerComentarios = comentariosRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaComentario.clear();
                        for (DataSnapshot ds :snapshot.getChildren()){
                            listaComentario.add(ds.getValue(Comentario.class));
                        }
                        adapterComentario.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarComentarios();
    }

    @Override
    protected void onStop() {
        super.onStop();
        comentariosRef.removeEventListener(valueEventListenerComentarios);
    }

    public void salvarComentario(View view){

        String textoComentario = editComentario.getText().toString();
        if(textoComentario != null && !textoComentario.equals("")){

            Comentario comentario= new Comentario();
            comentario.setIdPostagem(idPostagem);
            comentario.setIdUsuario(usuario.getId());
            comentario.setNomeUsuario(usuario.getNome());
            comentario.setCaminhoFoto(usuario.getCaminhoFoto());
            comentario.setComentario(textoComentario);
            comentario.salvar();

        }else{
            Toast.makeText(ComentariosActivity.this,
                    "Insira um comentario antes de salvar.",
            Toast.LENGTH_LONG).show();

        }
        editComentario.setText("");


    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}