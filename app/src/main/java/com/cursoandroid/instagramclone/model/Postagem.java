package com.cursoandroid.instagramclone.model;

import com.cursoandroid.instagramclone.helper.ConfiguracaoFirebase;
import com.cursoandroid.instagramclone.helper.UsuarioFirebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Postagem implements Serializable {

    private String id;
    private String idUsuario;
    private String descricao;
    private String caminhoFoto;

    public Postagem(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenceFirebase();
        DatabaseReference postagemRef = firebaseRef.child("postagens");
        String idPostagem = postagemRef.push().getKey();
        setId(idPostagem);
    }

    public boolean salvar(DataSnapshot seguidoresSnapshot){

        Map objeto = new HashMap();
        Usuario usuarioloagdo = UsuarioFirebase.getDadosUsuarioLogado();
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenceFirebase();

        //Referência para postagem
        String combinacaoId = "/" + getIdUsuario() + "/" + getId() ;
        objeto.put("/postagens" + combinacaoId , this);

        //Referência para a postagem no feed
       for (DataSnapshot seguidores : seguidoresSnapshot.getChildren()){

           //Recuperar o seguidor
           String idSeguidor = seguidores.getKey();

           //Montar objeto para salvar
           HashMap<String,Object> dadosSeguidor = new HashMap<>();
           dadosSeguidor.put("fotoPostagem",getCaminhoFoto());
           dadosSeguidor.put("id",getId());
           dadosSeguidor.put("descricao",getDescricao());
           dadosSeguidor.put("nomeUsuario",usuarioloagdo.getNome());
           dadosSeguidor.put("fotoUsuario",usuarioloagdo.getCaminhoFoto());

           String idsAtualizacao = "/" + idSeguidor + "/" + getId() ;
           objeto.put("/feed" + idsAtualizacao,dadosSeguidor);


       }

        firebaseRef.updateChildren(objeto);


        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }
}
