package com.dineyandroid.negocio.app.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.dineyandroid.negocio.app.R;
import com.dineyandroid.negocio.app.helper.ConfiguracaoFirebase;
import com.dineyandroid.negocio.app.helper.Permissoes;
import com.dineyandroid.negocio.app.model.Anuncio;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskedittext.MaskEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;


public class CadastrarAnunciosActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText campoTitulo, campoDescricao;
    private CurrencyEditText campoValor;
    private Spinner campoEstado, campoCategoria;
    private MaskEditText campoTelefone;
    private ImageView campoImagem1, campoImagem2, campoImagem3;
    private Anuncio anuncio;
    private StorageReference storage;
    private AlertDialog dialog;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private List<String> listaFotosRecuperadas = new ArrayList<>();
    private List<String> listaUrlFotosFirebase = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncios);

        //configurações iniciais
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        //Validar Permissões
        Permissoes.validarPermissoes(permissoes, this, 1);

        inicializarComponentes();
        carregarDadosSpinner();
    }

    public  void salvarAnuncio(){
        dialogSalvandoAnuncio();

        for (int i=0; i < listaFotosRecuperadas.size(); i++){
             String urlImagem = listaFotosRecuperadas.get(i);
             int tamanhoLista = listaFotosRecuperadas.size();
             salvarFotoStorage(urlImagem,tamanhoLista, i );
         }
    }

    private void dialogSalvandoAnuncio() {
        dialog = new SpotsDialog.Builder()
                 .setContext(this)
                 .setMessage("Salvar Anúncio")
                 .setCancelable(false)
                 .build();

        dialog.show();
    }

    private void salvarFotoStorage(String urlImagem, final int totalFotos, int contador){
       final StorageReference imagemAnuncio = storage.child("imagens")
                .child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("imagem"+contador);

        // fazendo upload da imagem
        UploadTask uploadTask = imagemAnuncio.putFile(Uri.parse(urlImagem));

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return imagemAnuncio.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUrl = task.getResult();
                    listaUrlFotosFirebase.add(downloadUrl.toString());
                    if (totalFotos == listaUrlFotosFirebase.size()) {
                        anuncio.setFotos(listaUrlFotosFirebase);
                        anuncio.salvar();

                        dialog.dismiss();
                        finish();
                    }
                }
            }
            });


    }
    private Anuncio configurarDadosAnuncio(){
        Anuncio anuncio = new Anuncio();
        anuncio.setEstado(campoEstado.getSelectedItem().toString());
        anuncio.setCategoria(campoCategoria.getSelectedItem().toString());
        anuncio.setTitulo(campoTitulo.getText().toString());
        anuncio.setValor(campoValor.getText().toString());
        anuncio.setTelefone(campoTelefone.getText().toString());
        anuncio.setDescricao(campoDescricao.getText().toString());

        return anuncio;
    }

    public  void validarAnuncio(View view){
         anuncio = configurarDadosAnuncio();
        String fone = campoTelefone.getRawText() != null? campoTelefone.getRawText().toString(): "";
        String valor = String.valueOf(campoValor.getRawValue()) != null ? String.valueOf(campoValor.getRawValue()): "";

        if (listaFotosRecuperadas.size() != 0){
            if (!anuncio.getEstado().isEmpty()){
                if (!anuncio.getCategoria().isEmpty()){
                    if (!anuncio.getTitulo().isEmpty()){
                        if (!valor.isEmpty() && !valor.equals("0")){
                            if (!anuncio.getTelefone().isEmpty() && fone.length() >=10){
                                if (!anuncio.getDescricao().isEmpty()){
                                    salvarAnuncio();
                                } else {
                                    exibirMsgErro("Preencha o campo Descrição!");
                                }

                            } else {
                                exibirMsgErro("Preencha o campo Telefone, digite ao menos 10 numeros!");
                            }

                        } else {
                            exibirMsgErro("Preencha o campo Valor!");
                        }

                    } else {
                        exibirMsgErro("Preencha o campo Titulo!");
                    }

                } else {
                    exibirMsgErro("Preencha o campo Categoria!");
                }

            } else {
                exibirMsgErro("Preencha o campo Estado!");
            }
        }else {
            exibirMsgErro("Selecione ao menos uma foto!");
        }

    }
    private void exibirMsgErro(String mensagem){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageCadastro1:
                escolherImagem(1);
                break;
            case R.id.imageCadastro2:
                escolherImagem(2);
                break;
            case R.id.imageCadastro3:
                escolherImagem(3);
                break;

        }
    }

    public  void escolherImagem(int requestCode){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){

            //recuperar imagens
            Uri imagemSelecionada = data.getData();
            String caminho = imagemSelecionada.toString();

            //configurar imagem no ImagemView
            if(requestCode == 1){
                campoImagem1.setImageURI(imagemSelecionada);
            }else if (requestCode == 2){
                campoImagem2.setImageURI(imagemSelecionada);
            }else if (requestCode == 3){
                campoImagem3.setImageURI(imagemSelecionada);
            }
            listaFotosRecuperadas.add(caminho);

        }
    }

    private  void carregarDadosSpinner(){
        //configuração spinner estados
        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                estados
        );
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoEstado.setAdapter(adapterEstado);

        //configuração spinner Categorias
        String[] categorias = getResources().getStringArray(R.array.categorias);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                categorias
        );
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoCategoria.setAdapter(adapterCategoria);

    }

    public void inicializarComponentes(){
        campoTitulo = findViewById(R.id.editTitulo);
        campoDescricao = findViewById(R.id.editDescricao);
        campoValor = findViewById(R.id.editValor);
        campoTelefone = findViewById(R.id.editTelefone);
        campoEstado = findViewById(R.id.spinnerEstados);
        campoCategoria = findViewById(R.id.spinnerCategoria);
        campoImagem1 = findViewById(R.id.imageCadastro1);
        campoImagem2 = findViewById(R.id.imageCadastro2);
        campoImagem3 = findViewById(R.id.imageCadastro3);
        campoImagem1.setOnClickListener(this);
        campoImagem2.setOnClickListener(this);
        campoImagem3.setOnClickListener(this);

        // configurar localidade para o Brasil pt-> Portugues BR-> Brasil
        Locale locale = new Locale("pt", "BR");
        campoValor.setLocale(locale);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permisaoResultado : grantResults){
            if (permisaoResultado== PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões negadas");
        builder.setMessage("Para utilizar o app, precisa-se das permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
