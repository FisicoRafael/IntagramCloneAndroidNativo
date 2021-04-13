package com.cursoandroid.instagramclone.fragment;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cursoandroid.instagramclone.R;
import com.cursoandroid.instagramclone.activity.FiltroActivity;
import com.cursoandroid.instagramclone.helper.Permissoes;

import java.io.ByteArrayOutputStream;


public class PostagemFragment extends Fragment {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private Button abrirGaleria, abrirCamera;
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    public PostagemFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_postagem, container, false);

        abrirGaleria = view.findViewById(R.id.buttonAbrirGaleria);
        abrirCamera = view.findViewById(R.id.buttonAbrirCamera);

        //Validar Permissões
        Permissoes.validarPermissoes(permissoesNecessarias,getActivity(),1);

        //Adiciona um evento de clique no botão abrir Camera
        abrirCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (i.resolveActivity(getActivity().getPackageManager()) != null){
                    startActivityForResult(i,SELECAO_CAMERA);
                }
            }
        });


        //Adiciona um evento de clique no botão abrir Galeria
        abrirGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (i.resolveActivity(getActivity().getPackageManager()) != null){
                    startActivityForResult(i,SELECAO_GALERIA);
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK){

            Bitmap imagem = null;

            try{
                //Validar tipo de seleção de imagem
                switch (requestCode) {
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),localImagemSelecionada);
                        break;
                }
                //Validar imagem selecionada
                if(imagem != null){
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Enviar Imegem para a aplicação de filtros
                    Intent intent = new Intent(getActivity(), FiltroActivity.class);
                    intent.putExtra("fotoEscolhida", dadosImagem);
                    startActivity(intent);
                }
                
                
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }
}