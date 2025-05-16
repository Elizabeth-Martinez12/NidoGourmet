package com.example.comederomvil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Handler para actualizar la interfaz de usuario en el hilo principal
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Variables para los botones
    private TextView btnAves;
    private TextView btnAlimento;

    // sesion
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        // Inicializar el SessionManager
        this.sessionManager = new SessionManager(this);

        // Configurar los botones
        btnAves = findViewById(R.id.btn_aves);
        btnAlimento = findViewById(R.id.btn_alimento);

        btnAves.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AvesClass.class);
                startActivity(intent);
            }
        });

        btnAlimento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlimentoClass.class);
                startActivity(intent);
            }
        });


        WebSocketManager.getInstance().connect(this, sessionManager.getAuthToken());

        WebSocketManager.getInstance().send("Hola desde el MainActivity");
    }
}
