package com.example.comederomvil;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.slider.Slider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlimentoClass extends AppCompatActivity {

    // Handler para actualizar la interfaz de usuario en el hilo principal
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Variables para los botones
    private Button openCs, closeCs, openC1, closeC1, openC2, closeC2;
    private TextView foodLevel1, foodLevel2;
    private SessionManager sessionManager;
    private String SERVER_URL;
    private RequestQueue listaSolicitudes;
    private Slider sliderf1, sliderf2;
    private RecyclerView rvHorarios1, rvHorarios2;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_alimento);

        // Inicializar el SessionManager
        this.sessionManager = new SessionManager(this);

        this.listaSolicitudes = Volley.newRequestQueue(this);

        // Configurar los botones
        openCs = findViewById(R.id.bntOpenCs);
        closeCs = findViewById(R.id.btnCloseCs);
        openC1 = findViewById(R.id.bntOpenC1);
        closeC1 = findViewById(R.id.btnCloseC1);
        openC2 = findViewById(R.id.bntOpenC2);
        closeC2 = findViewById(R.id.btnCloseC2);

        foodLevel1 = findViewById(R.id.compuerta1food);
        foodLevel2 = findViewById(R.id.compuerta2food);

        sliderf1 = findViewById(R.id.compuerta1slider);
        sliderf2 = findViewById(R.id.compuerta2slider);

        // Configurar RecyclerViews para horarios
        rvHorarios1 = findViewById(R.id.rvHorarios1);
        rvHorarios2 = findViewById(R.id.rvHorarios2);

        rvHorarios1.setLayoutManager(new LinearLayoutManager(this));
        rvHorarios2.setLayoutManager(new LinearLayoutManager(this));

        addListeners();

        SERVER_URL = getString(R.string.api_base_url);

        getLastData();

    }

    @Override
    protected void onStart() {
        super.onStart();

        WebSocketManager.getInstance().setMessageListener(message -> runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(message);
                if (json.getString("type").equals("update_status")) {
                    // Actualizar niveles directamente
                    JSONObject floodgates = json.getJSONObject("floodgates");

                    JSONObject fg1 = floodgates.getJSONObject("1");
                    foodLevel1.setText("Compuerta 1: " + fg1.getInt("foodLevel") + "%");
                    sliderf1.setValue(fg1.getInt("foodLevel"));

                    JSONObject fg2 = floodgates.getJSONObject("2");
                    foodLevel2.setText("Compuerta 2: " + fg2.getInt("foodLevel") + "%");
                    sliderf2.setValue(fg2.getInt("foodLevel"));

                    // Procesar horarios
                    handleStatusUpdate(json);
                }
            } catch (JSONException e) {
                Log.e("AlimentoClass", "Error WS: " + e.getMessage());
                Toast.makeText(this, "Error en datos recibidos", Toast.LENGTH_SHORT).show();
            }
        }));
    }


    private void addListeners() {
        // evento para el boton de abrir compuertas
        openCs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manipularCompuertas("open_servos", 0, "Abriendo compuertas");
            }
        });

        // evento para el boton de cerrar compuertas
        closeCs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manipularCompuertas("close_servos", 0, "Cerrando compuertas");
            }
        });


        openC1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manipularCompuertas("open_servo", 1, "Abriendo compuerta 1");

            }
        });

        // evento para el boton de cerrar compuertas
        closeC1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manipularCompuertas("close_servo", 1, "Cerrando compuerta 1");
            }
        });

        openC2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manipularCompuertas("open_servo", 2, "Abriendo compuerta 2");

            }
        });

        // evento para el boton de cerrar compuertas
        closeC2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manipularCompuertas("close_servo", 2, "Cerrando compuerta 2");
            }
        });
    }

    // metodo para obtener la ultima data del servidor
    private void getLastData() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, SERVER_URL + "device/feeder/67f57821c597ab31781c074d", null,
                response -> {
                    try {
                        if (response.has("data")) {
                            handleStatusUpdate(response);
                        } else {
                            // Si no viene en "data", procesar directamente
                            handleStatusUpdate(response);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                        Log.e("AlimentoClass", "Error: " + e.getMessage());
                    }
                },
                error -> {
                    Toast.makeText(this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AlimentoClass", "Error de red: " + error.toString());
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-token", sessionManager.getAuthToken());
                return headers;
            }
        };

        listaSolicitudes.add(request);
    }


    private void manipularCompuertas(String type, int number, String toastMessage) {
        Toast.makeText(AlimentoClass.this, toastMessage, Toast.LENGTH_SHORT).show();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", type);
            jsonObject.put("servo_number", number);
            WebSocketManager.getInstance().send(jsonObject.toString());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(AlimentoClass.this, "Error al enviar datos", Toast.LENGTH_SHORT).show();
        }
    }


    private void handleStatusUpdate(JSONObject json) throws JSONException {
        try {
            // 1. Obtener el objeto de datos correcto
            JSONObject datos = json;
            if (json.has("data")) {
                datos = json.getJSONObject("data");
            }

            // 2. Verificar y obtener el ID del alimentador
            String feederId = "";
            if (datos.has("_id")) {
                feederId = datos.getString("_id");
            } else if (json.has("_id")) { // Caso para algunos mensajes WebSocket
                feederId = json.getString("_id");
            } else {
                // Usar un ID por defecto o manejar el error
                feederId = "default_id";
                Log.w("AlimentoClass", "No se encontró _id, usando valor por defecto");
            }

            // 3. Obtener las compuertas
            JSONObject floodgates = datos.getJSONObject("floodgates");

            // Procesar compuerta 1
            JSONObject fg1 = floodgates.getJSONObject("1");
            int foodLevelc1 = fg1.getInt("foodLevel");
            foodLevel1.setText("Compuerta 1: " + foodLevelc1 + "%");
            sliderf1.setValue(foodLevelc1);
            List<DiaHorario> horarios1 = parseHorarios(fg1, 1);
            rvHorarios1.setAdapter(new HorarioAdapter(horarios1, 1, feederId, this));

            // Procesar compuerta 2
            JSONObject fg2 = floodgates.getJSONObject("2");
            int foodLevelc2 = fg2.getInt("foodLevel");
            foodLevel2.setText("Compuerta 2: " + foodLevelc2 + "%");
            sliderf2.setValue(foodLevelc2);
            List<DiaHorario> horarios2 = parseHorarios(fg2, 2);
            rvHorarios2.setAdapter(new HorarioAdapter(horarios2, 2, feederId, this));

        } catch (JSONException e) {
            Log.e("AlimentoClass", "Error al procesar datos: " + e.getMessage());
            Toast.makeText(this, "Error al procesar datos del servidor", Toast.LENGTH_SHORT).show();
            throw e;
        }
    }

    private List<DiaHorario> parseHorarios(JSONObject compuerta, int numCompuerta) throws JSONException {
        List<DiaHorario> horarios = new ArrayList<>();

        Map<String, String> diasMap = new HashMap<>();
        diasMap.put("monday", "Lunes");
        diasMap.put("tuesday", "Martes");
        diasMap.put("wednesday", "Miércoles");
        diasMap.put("thursday", "Jueves");
        diasMap.put("friday", "Viernes");
        diasMap.put("saturday", "Sábado");
        diasMap.put("sunday", "Domingo");

        for (Map.Entry<String, String> entry : diasMap.entrySet()) {
            String diaIng = entry.getKey();
            String diaEsp = entry.getValue();

            if (compuerta.has(diaIng)) {
                JSONObject diaJson = compuerta.getJSONObject(diaIng);
                String startTime = diaJson.getString("startTime");
                String endTime = diaJson.getString("endTime");

                horarios.add(new DiaHorario(diaIng, diaEsp, startTime, endTime));
            }
        }

        return horarios;
    }
}
