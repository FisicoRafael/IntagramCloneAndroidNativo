package com.cursoandroid.instagramclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cursoandroid.instagramclone.R;
import com.cursoandroid.instagramclone.helper.ConfiguracaoFirebase;
import com.cursoandroid.instagramclone.helper.UsuarioFirebase;
import com.cursoandroid.instagramclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private EditText campoNome, campoEmail, campoSenha;
    private Button buttonCadastrar;
    private Usuario usuario = new Usuario();
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        inicializarComponentes();
        //Cadastrar Usuario

        progressBar.setVisibility(View.GONE);
        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nome = campoNome.getText().toString();
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                if (!nome.isEmpty()){
                    if (!email.isEmpty()){
                        if (!senha.isEmpty()){
                            usuario.setNome(nome);
                            usuario.setEmail(email);
                            usuario.setSenha(senha);
                            usuario.setNomePesquisa(nome.toUpperCase());
                            cadastrarUsuario(usuario);

                        }else {
                            Toast.makeText(CadastroActivity.this,
                                    "Preencha a senha.",
                                    Toast.LENGTH_SHORT).show();                }

                    }else {
                        Toast.makeText(CadastroActivity.this,
                                "Preencha o email.",
                                Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(CadastroActivity.this,
                            "Preencha o nome.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void inicializarComponentes(){
        campoNome       = findViewById(R.id.editCadastroNome);
        campoEmail       = findViewById(R.id.editCadastroEmail);
        campoSenha       = findViewById(R.id.editCadastroSenha);
        buttonCadastrar       = findViewById(R.id.buttonCadastro);
        progressBar     = findViewById(R.id.progressCadastro);

        campoNome.requestFocus();
    }

    public void cadastrarUsuario(final Usuario usuario){
        progressBar.setVisibility(View.VISIBLE);
        //Cadastrar Usuario
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),usuario.getSenha()
        ).addOnCompleteListener(CadastroActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    try {
                        progressBar.setVisibility(View.GONE);
                        //Salvar dados do usuario no firebase
                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId(idUsuario);
                        usuario.salvar();

                        //Salvar dados no profile do Firebase
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                        Toast.makeText(CadastroActivity.this,
                                "Sucesso ao Cadastrar.",
                                Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        finish();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this,
                            "Sucesso ao Cadastrar.",
                            Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);

                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();

                }else {
                    progressBar.setVisibility(View.GONE);
                    String erroExcessao="";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e) {
                        erroExcessao = "Digite uma senha mais forte!";
                    }catch(FirebaseAuthInvalidCredentialsException e) {
                        erroExcessao = "Por favor, digite um email valido!";
                    } catch (FirebaseAuthUserCollisionException e){
                        erroExcessao = "Esta conta já foi cadastrada!";
                    } catch (Exception e) {
                        erroExcessao = "ao cadastrar usúario: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this,
                            "Erro: " + erroExcessao ,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}