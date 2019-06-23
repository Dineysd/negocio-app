package com.dineyandroid.negocio.app.activity;

import android.app.Application;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.dineyandroid.negocio.app.R;
import com.dineyandroid.negocio.app.helper.ConfiguracaoFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class  MainActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private Switch tipoAcesso;

    private FirebaseAuth autentificacao;

    RelativeLayout animate;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            animate.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        animate = (RelativeLayout) findViewById(R.id.login_animate);
        handler.postDelayed(runnable, 2000);

        inicializarComponentes();

        autentificacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                if(!email.isEmpty()){
                    if (!senha.isEmpty()){
                        // Verificar estado do switch
                        verificarSwitch(email, senha);

                    } else {
                        Toast.makeText(MainActivity.this, "Preencha a senha!",
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Preencha E-mail!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verificarSwitch(String email, String senha) {
        if(tipoAcesso.isChecked()){//cadastro
            autentificacao.createUserWithEmailAndPassword(
                    email, senha
            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    cadastrar(task);
                }
            });
        }else{//Login
            autentificacao.signInWithEmailAndPassword(
                    email, senha
            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    logar(task);
                }
            });
        }
    }

    private void logar(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()){
            Toast.makeText(MainActivity.this,
                    "Logado com sucesso!",
                    Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(MainActivity.this,
                    "Erro ao fazer login: "+ task.getException(),
                    Toast.LENGTH_SHORT).show();

        }
    }

    private void cadastrar(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()){
            Toast.makeText(MainActivity.this,
                    "Cadastro realizado com sucesso!",
                    Toast.LENGTH_SHORT).show();
        } else {
            String erroExcecao = "";
            erroExcecao = erroExcecaoException(task);
            Toast.makeText(MainActivity.this, "Erro: "+ erroExcecao,
                    Toast.LENGTH_SHORT).show();

        }
    }

    private String erroExcecaoException(@NonNull Task<AuthResult> task) {
        String erroExcecao = "";
        try {
            throw task.getException();
        }catch (FirebaseAuthWeakPasswordException e){
            erroExcecao = "Digite uma senha mais forte!";
        }catch (FirebaseAuthInvalidCredentialsException e){
            erroExcecao = "Por favor, digite um e-mail valido!";
        }catch (FirebaseAuthUserCollisionException e){
            erroExcecao = "Esta conta já foi cadastrada!";
        }catch (Exception e){
            erroExcecao = "Ao cadastrar usuário: " + e.getMessage();
            e.printStackTrace();
        }
        return erroExcecao;
    }

    private  void inicializarComponentes(){
        campoEmail = (EditText) findViewById(R.id.editCadastroEmail);
        campoSenha = (EditText) findViewById(R.id.editCadastroSenha);
        botaoAcessar = (Button) findViewById(R.id.buttonAcesso);
        tipoAcesso = (Switch) findViewById(R.id.switchAcesso);
    }
}
