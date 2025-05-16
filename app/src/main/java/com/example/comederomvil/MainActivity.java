package com.example.comederomvil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {

    // Handler para actualizar la interfaz de usuario en el hilo principal
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    // Variables para el WebSocket
    private WebSocket webSocket;
    private String SERVER_URL;

    // Variables para los botones
    private TextView btnAves;
    private TextView btnAlimento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        // Obtener la URL del WebSocket desde los recursos de cadena
        SERVER_URL = getString(R.string.websocket);

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


        startWebSocket();
    }


    private void startWebSocket() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder().url(SERVER_URL).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull okhttp3.Response response) {
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "Conectado al WebSocket", Toast.LENGTH_SHORT).show();

                    // Puedes enviar un JSON de prueba aquí
                    JSONObject json = new JSONObject();
                    try {
                        json.put("type", "user_connect");
                        json.put("mensaje", "Hola desde Android");
                        webSocket.send(json.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "Mensaje recibido: " + text, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                onMessage(webSocket, bytes.utf8());
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, okhttp3.Response response) {
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("WebSocket", "Error de conexión: " + t.getMessage());
                });
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                webSocket.close(1000, null);
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "Cerrando conexión", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

}
