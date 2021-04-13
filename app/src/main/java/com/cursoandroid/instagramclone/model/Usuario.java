package com.cursoandroid.instagramclone.model;

import com.cursoandroid.instagramclone.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Usuario implements Serializable {

    private String nome;
    private String nomePesquisa;
    private String id;
    private String email;
    private String senha;
    private String caminhoFoto;
    private int seguindo = 0;
    private int seguidores = 0;
    private int postagens = 0;

    public Usuario() {
    }

    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenceFirebase();
        DatabaseReference usuariosRef = firebaseRef.child("usuarios").child(getId());

        //SetValue escre os dados novamente e sobreescrete
        usuariosRef.setValue(this);
    }

    public void atualizarQtPostagens(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenceFirebase();
        DatabaseReference usuariosRef = firebaseRef.child("usuarios").child(getId());

        //updateChildren atualizada os dados
        HashMap<String, Object> dados = new HashMap<>();
        dados.put("postagens",getPostagens());
        usuariosRef.updateChildren(dados);
    }

    public void atualizar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenceFirebase();

        Map objeto = new HashMap();
        objeto.put("/usuarios/"+ getId() + "/nome",getNome() );
        objeto.put("/usuarios/"+ getId() + "/nomePesquisa",getNomePesquisa() );
        objeto.put("/usuarios/"+ getId() + "/caminhoFoto",getCaminhoFoto() );

        firebaseRef.updateChildren(objeto);

    }

    public Map<String, Object> converterParaMap(){

        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("email", getEmail());
        usuarioMap.put("nome", getNome());
        usuarioMap.put("id", getId());
        usuarioMap.put("caminhoFoto", getCaminhoFoto());
        usuarioMap.put("nomePesquisa",getNomePesquisa());
        usuarioMap.put("seguindo",getSeguindo());
        usuarioMap.put("seguidores",getSeguidores());
        usuarioMap.put("postagens",getPostagens());

        return usuarioMap;
    }

    public int getSeguindo() {
        return seguindo;
    }

    public void setSeguindo(int seguindo) {
        this.seguindo = seguindo;
    }

    public int getSeguidores() {
        return seguidores;
    }

    public void setSeguidores(int seguidores) {
        this.seguidores = seguidores;
    }

    public int getPostagens() {
        return postagens;
    }

    public void setPostagens(int postagens) {
        this.postagens = postagens;
    }

    public String getNomePesquisa() {
        return nomePesquisa;
    }

    public void setNomePesquisa(String nomePesquisa) {
        this.nomePesquisa = nomePesquisa;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
