package com.dineyandroid.negocio.app.helper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfiguracaoFirebase {
    private static DatabaseReference referenceFirebase;
    private static FirebaseAuth firebaseAutenticacao;
    private static StorageReference storageReference;

    public static String getIdUsuario(){
        FirebaseAuth autenticacao = getFirebaseAutenticacao();
        return autenticacao.getCurrentUser().getUid();
    }


    public static DatabaseReference getReferenceFirebase() {
        if (referenceFirebase == null){
            referenceFirebase = FirebaseDatabase.getInstance().getReference();
        }
        return referenceFirebase;
    }

    public static FirebaseAuth getFirebaseAutenticacao() {
        if (firebaseAutenticacao == null){
            firebaseAutenticacao = FirebaseAuth.getInstance();
        }
        return firebaseAutenticacao;
    }

    public static StorageReference getFirebaseStorage() {
        if (storageReference == null){
            storageReference = FirebaseStorage.getInstance().getReference();
        }
        return storageReference;
    }
}
