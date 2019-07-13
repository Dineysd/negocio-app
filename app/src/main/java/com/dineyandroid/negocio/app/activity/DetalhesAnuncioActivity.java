package com.dineyandroid.negocio.app.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dineyandroid.negocio.app.R;
import com.dineyandroid.negocio.app.model.Anuncio;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

public class DetalhesAnuncioActivity extends AppCompatActivity {

    private CarouselView carouselView;
    private TextView titulo;
    private TextView valor;
    private TextView estado;
    private TextView descricao;
    private Anuncio anuncioSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_anuncio);
        // configurar toolbar
        getSupportActionBar().setTitle("Detalhes");

        inicializarComponentes();

        //recuperar Anuncios para exibir detalhes
        anuncioSelecionado = (Anuncio) getIntent().getSerializableExtra("anuncioSelecionado");

        adicionandoCarouselDeImagens();
    }

    private void adicionandoCarouselDeImagens() {
        if(anuncioSelecionado != null){
            populandoComponentes();

            ImageListener imageListener = new ImageListener() {
                @Override
                public void setImageForPosition(int position, ImageView imageView) {
                    String urlString = anuncioSelecionado.getFotos().get(position);
                    Picasso.get().load(urlString).into(imageView);
                }
            };

            carouselView.setPageCount(anuncioSelecionado.getFotos().size());
            carouselView.setImageListener(imageListener);
        }
    }

    private void populandoComponentes() {
        titulo.setText(anuncioSelecionado.getTitulo());
        descricao.setText(anuncioSelecionado.getDescricao());
        estado.setText(anuncioSelecionado.getEstado());
        valor.setText(anuncioSelecionado.getValor());
    }

    public void visualizarTelefone(View view){
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", anuncioSelecionado.getTelefone(), null));
        startActivity( intent );

    }

    private void inicializarComponentes(){
        carouselView = findViewById(R.id.carouselViewDetalhe);
        titulo = findViewById(R.id.textTituloDetalhe);
        valor = findViewById(R.id.textValorDetalhe);
        estado = findViewById(R.id.textEstadoDetalhe);
        descricao = findViewById(R.id.textDescricaoDetalhe);
    }
}
