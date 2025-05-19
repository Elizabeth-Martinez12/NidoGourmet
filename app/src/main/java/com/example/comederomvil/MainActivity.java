package com.example.comederomvil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Handler para actualizar la interfaz de usuario en el hilo principal
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Variables para los botones
    private TextView btnAves;
    private TextView btnAlimento;

    // variables de nivel de alimento y bateria
    private TextView batteryLevel, foodLevel1, foodLevel2;

    // sesion
    private SessionManager sessionManager;

    private String SERVER_URL;
    private RequestQueue listaSolicitudes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        // Inicializar el SessionManager
        this.sessionManager = new SessionManager(this);
        this.listaSolicitudes = Volley.newRequestQueue(this);

        // Configurar los botones
        btnAves = findViewById(R.id.btn_aves);
        btnAlimento = findViewById(R.id.btn_alimento);

        // Configurar los botones
        batteryLevel = findViewById(R.id.battery_level);
        foodLevel1 = findViewById(R.id.food_level_1);
        foodLevel2 = findViewById(R.id.food_level_2);


        addListeners();

        SERVER_URL = getString(R.string.api_base_url);

        getLastData();


        WebSocketManager.getInstance().connect(this, sessionManager.getAuthToken());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Configurar el listener del mensaje, para recibir mensajes del WebSocket
        WebSocketManager.getInstance().setMessageListener(message -> runOnUiThread(() -> {
            try {
                // Parsear el mensaje JSON
                JSONObject json = new JSONObject(message);

                // Obtener el tipo de mensaje
                String tipo = json.getString("type");

                // si el tipo es update_status, actualizar el estado de los botones
                if (tipo.equals("update_status")) {

                    // obteniendo los datos del json
                    int battery = json.getInt("batteryLevel");
                    JSONObject floodgates = json.getJSONObject("floodgates");

                    JSONObject fg1 = floodgates.getJSONObject("1");
                    int foodLevelc1 = fg1.getInt("foodLevel");

                    JSONObject fg2 = floodgates.getJSONObject("2");
                    int foodLevelc2 = fg2.getInt("foodLevel");

                    // Actualizar la interfaz de usuario con los nuevos valores
                    batteryLevel.setText("Nivel de Batería: " + battery + "%");
                    foodLevel1.setText("Alimento restante en compuerta 1: " + foodLevelc1 + "%");
                    foodLevel2.setText("Alimento restante en compuerta 2: " + foodLevelc2 + "%");


                } else {
                    String contenido = json.optString("contenido", "Sin contenido");

                    Toast.makeText(MainActivity.this, "Tipo: " + tipo + "\nContenido: " + contenido, Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error al parsear JSON", Toast.LENGTH_SHORT).show();
                Log.e("WebSocket", "Error al parsear JSON: " + e.getMessage());
                Log.d("WebSocket", "Mensaje problemático: " + message);
            }
        }));
    }


    private void addListeners() {
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
    }


    // metodo para obtener la ultima data del servidor
    private void getLastData() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, SERVER_URL + "device/feeder/67f57821c597ab31781c074d", null,
                response -> {
                    try {
                        //
                        JSONObject data = response.getJSONObject("data");

                        // obteniendo los datos del json
                        int battery = data.getInt("batteryLevel");

                        JSONObject floodgates = data.getJSONObject("floodgates");

                        JSONObject fg1 = floodgates.getJSONObject("1");
                        int foodLevelc1 = fg1.getInt("foodLevel");

                        JSONObject fg2 = floodgates.getJSONObject("2");
                        int foodLevelc2 = fg2.getInt("foodLevel");

                        // Actualizar la interfaz de usuario con los nuevos valores
                        batteryLevel.setText("Nivel de Batería: " + battery + "%");
                        foodLevel1.setText("Alimento restante en compuerta 1: " + foodLevelc1 + "%");
                        foodLevel2.setText("Alimento restante en compuerta 2: " + foodLevelc2 + "%");

                        Log.d("data", data.toString());

                    } catch (JSONException e) {
                        Toast.makeText(this, "algo ha fallado", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "algo ha fallado" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("error", error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Añadir la cabecera para el JWT
                headers.put("x-token", sessionManager.getAuthToken());
                return headers;
            }

            ;
        };

        listaSolicitudes.add(request);
    }


//    @Override
//    protected void onStop() {
//        super.onStop();
//        WebSocketManager.getInstance().removeMessageListener();
//    }
}
