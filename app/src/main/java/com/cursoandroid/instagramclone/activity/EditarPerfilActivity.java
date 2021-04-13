package com.cursoandroid.instagramclone.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cursoandroid.instagramclone.R;
import com.cursoandroid.instagramclone.helper.ConfiguracaoFirebase;
import com.cursoandroid.instagramclone.helper.Permissoes;
import com.cursoandroid.instagramclone.helper.UsuarioFirebase;
import com.cursoandroid.instagramclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditarPerfilActivity extends AppCompatActivity {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_PHOTO = 200;
    private Toolbar toolbarPrincipal;
    private CircleImageView imagePerfil;
    private TextView campoAlterarFoto;
    private TextInputEditText campoNomePerfil, campoEmailPerfil;
    private Button buttonSalvarPerfil;
    private Usuario usuarioLogado;
    private StorageReference storageRef;
    private String idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);
        //Configurar Toolbar
        toolbarPrincipal = findViewById(R.id.toolbarPrincipal);
        toolbarPrincipal.setTitle("Editar Perfil");
        setSupportActionBar(toolbarPrincipal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        inicializarComponentes();

        //Validar permissões
        Permissoes.validarPermissoes(permissoesNecessarias, this, 1);

        //Configurações iniciais
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        storageRef = ConfiguracaoFirebase.getStorageReference();
        idUsuario = UsuarioFirebase.getIdUsuario();

        //recuperar dados do usuario
        FirebaseUser usuarioPerfil = UsuarioFirebase.getUsuarioAtual();
        campoNomePerfil.setText(usuarioPerfil.getDisplayName());
        campoEmailPerfil.setText(usuarioPerfil.getEmail());

        Uri url = usuarioPerfil.getPhotoUrl();
        if (url != null) {
            Glide.with(EditarPerfilActivity.this)
                    .load(url)
                    .into(imagePerfil);
        } else {
            imagePerfil.setImageResource(R.drawable.avatar);
        }

        //Salvar Alterações do nome
        buttonSalvarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeAtualizado = campoNomePerfil.getText().toString();

                //Atualizar nome perfil firebase
                UsuarioFirebase.atualizarNomeUsuario(nomeAtualizado);

                //Atualizar o nome no banco de dados
                usuarioLogado.setNome(nomeAtualizado);
                usuarioLogado.setNomePesquisa(nomeAtualizado.toUpperCase());
                usuarioLogado.atualizar();
                Toast.makeText(EditarPerfilActivity.this,
                        "Dados alterados com sucesso.",
                        Toast.LENGTH_LONG).show();
            }
        });

        //Alterar foto do usuario
        campoAlterarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, SELECAO_PHOTO);
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;
            try {
                //Selecao apenas da galeria de foto
                switch (requestCode) {
                    case SELECAO_PHOTO:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }

                //caso tenha escolhido a imagem, carregar a imagem
                if (imagem != null) {

                    //Configurar imagem na tela do Usuario
                    imagePerfil.setImageBitmap(imagem);

                    //Recuperar dados da imagem para o Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();


                    //salvar imagem no firebase
                    final StorageReference imagemRef = storageRef
                            .child("imagens")
                            .child("perfil")
                            .child(idUsuario + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditarPerfilActivity.this,
                                    "Erro ao fazer upload da Imagem.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();
                                    atualizarFotoUsuario(url);
                                }
                            });
                            Toast.makeText(EditarPerfilActivity.this,
                                    "Sucesso ao fazer upload da Imagem.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    private void atualizarFotoUsuario(Uri url) {
        //Atualizar a foto no perfil
        UsuarioFirebase.atualizarFotoUsuario(url);
        //Atualizar a foto no firebase
        usuarioLogado.setCaminhoFoto(url.toString());
        usuarioLogado.atualizar();
        Toast.makeText(EditarPerfilActivity.this,
                "Sua foto foi alterada.",
                Toast.LENGTH_LONG).show();
    }

    public void inicializarComponentes() {

        imagePerfil = findViewById(R.id.imageEditarPerfil);
        campoAlterarFoto = findViewById(R.id.textAlterarFoto);
        campoNomePerfil = findViewById(R.id.editNomePerfil);
        campoEmailPerfil = findViewById(R.id.editEmailPerfil);
        buttonSalvarPerfil = findViewById(R.id.buttonSalvarAlteracoesPerfil);
        campoEmailPerfil.setFocusable(false);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}