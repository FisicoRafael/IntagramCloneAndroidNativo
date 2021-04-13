package com.cursoandroid.instagramclone.model;

import com.cursoandroid.instagramclone.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

public class PostagensCurtidas   {

    private Feed feed;
    private Usuario usuario;
    private int qtdCurtidas = 0;

    public PostagensCurtidas() {
    }

    public void salvar(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenceFirebase();

        //Dados usuario
        HashMap<String,Object> dadosUsuario = new HashMap<>();
        dadosUsuario.put("nomeUsuario",usuario.getNome());
        dadosUsuario.put("CaminhoFotoUsuario",usuario.getCaminhoFoto());

        DatabaseReference pCurtidasRef = firebaseRef.child("postagens-curtidas")
                            .child(feed.getId())
                            .child(usuario.getId());
        pCurtidasRef.setValue(dadosUsuario);

        //Atualizar a quantidades de curtidas
        atualizarQtCurtidas(1);


    }

    public void removerCurtida(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenceFirebase();

        DatabaseReference pCurtidasRef = firebaseRef.child("postagens-curtidas")
                .child(feed.getId())
                .child(usuario.getId());
        pCurtidasRef.removeValue();

        //Atualizar a quantidades de curtidas
        atualizarQtCurtidas(-1);

    }

    public void atualizarQtCurtidas(int valor){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenceFirebase();

        DatabaseReference pCurtidasRef = firebaseRef.child("postagens-curtidas")
                .child(feed.getId())
                .child("qtdCurtidas");
        setQtdCurtidas(getQtdCurtidas() + valor);
        pCurtidasRef.setValue(getQtdCurtidas());
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public int getQtdCurtidas() {
        return qtdCurtidas;
    }

    public void setQtdCurtidas(int qtdCurtidas) {
        this.qtdCurtidas = qtdCurtidas;
    }
}
