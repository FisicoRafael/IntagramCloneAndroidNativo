package com.cursoandroid.instagramclone.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SearchView;

import com.cursoandroid.instagramclone.R;
import com.cursoandroid.instagramclone.activity.PerfilAmigoActivity;
import com.cursoandroid.instagramclone.adapter.AdapterPesquisa;
import com.cursoandroid.instagramclone.helper.ConfiguracaoFirebase;
import com.cursoandroid.instagramclone.helper.RecyclerItemClickListener;
import com.cursoandroid.instagramclone.helper.UsuarioFirebase;
import com.cursoandroid.instagramclone.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class PesquisaFragment extends Fragment {

    private SearchView searchViewPesquisa;
    private RecyclerView recyclerView;

    private List<Usuario> listaUsuarios;
    private DatabaseReference usuarioRef;
    private AdapterPesquisa adapterPesquisa;

    private String idUsuarioLogdo;


    public PesquisaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pesquisa, container, false);

        searchViewPesquisa  = view.findViewById(R.id.searchViewPesquisa);
        recyclerView = view.findViewById(R.id.recyclerPesquisa);
        idUsuarioLogdo = UsuarioFirebase.getIdUsuario();

        listaUsuarios = new ArrayList<>();
        usuarioRef = ConfiguracaoFirebase.getReferenceFirebase()
                .child("usuarios");
        //Configurar reciclerView
        adapterPesquisa = new AdapterPesquisa(listaUsuarios,getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapterPesquisa);

        //Configurar evento de clique
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(),
                recyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Usuario usuarioSelecionado = listaUsuarios.get(position);
                        Intent intent = new Intent(getActivity(), PerfilAmigoActivity.class);
                        intent.putExtra("usuarioselecionado",usuarioSelecionado);
                        startActivity(intent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

        //Configurar searview
        searchViewPesquisa.setQueryHint("Buscar Usuarios");
        searchViewPesquisa.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String textoDigitado = newText.toUpperCase();
                pesquisarUsuarios(textoDigitado);
                return true;
            }
        });
        return view;
    }

    private void pesquisarUsuarios(String texto){

        //Limpar a lista
        listaUsuarios.clear();

        //Pesquisar usuario caso tenha texto na pesquisa
        if (texto.length() > 0){
            Query query = usuarioRef.orderByChild("nomePesquisa")
                    .startAt(texto)
                    .endAt(texto+"\uf8ff");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    listaUsuarios.clear();
                    for (DataSnapshot sn : snapshot.getChildren()){

                        //Verificar se é ususario logado e remove da lista
                        Usuario usuario = sn.getValue(Usuario.class);

                        if (idUsuarioLogdo.equals(usuario.getId()))
                            continue;

                        listaUsuarios.add(usuario);
                    }

                    adapterPesquisa.notifyDataSetChanged();


                    int total = listaUsuarios.size();
                    Log.i("totalUsuarios","Total: "+total);



                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }


}