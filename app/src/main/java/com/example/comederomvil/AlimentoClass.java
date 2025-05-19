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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
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


        ImageView btnAgregar1 = findViewById(R.id.btnAgregar1);
        ImageView btnAgregar2 = findViewById(R.id.btnAgregar2);
        ImageView btnAgregarTodas = findViewById(R.id.btnAgregarTodas);

        View.OnClickListener agregarHorarioListener = v -> mostrarDialogoAgregar();

        btnAgregar1.setOnClickListener(agregarHorarioListener);
        btnAgregar2.setOnClickListener(agregarHorarioListener);
        btnAgregarTodas.setOnClickListener(agregarHorarioListener);

        addListeners();

        SERVER_URL = getString(R.string.api_base_url);

        getLastData();

    }

    @Override
    protected void onStart() {
        super.onStart();

        WebSocketManager.getInstance().setMessageListener(message -> runOnUiThread(() -> {
            Log.d("WebSocket", "Mensaje recibido en AlimentoClass: " + message);
            try {
                JSONObject json = new JSONObject(message);
                String tipo = json.getString("type");

                if (tipo.equals("update_status")) {
                    JSONObject floodgates = json.getJSONObject("floodgates");

                    JSONObject fg1 = floodgates.getJSONObject("1");
                    int foodLevelc1 = fg1.getInt("foodLevel");

                    JSONObject fg2 = floodgates.getJSONObject("2");
                    int foodLevelc2 = fg2.getInt("foodLevel");

                    // Actualiza los TextViews específicos de AlimentoClass
                    foodLevel1.setText("Alimento restante en compuerta 1: " + foodLevelc1 + "%");
                    foodLevel2.setText("Alimento restante en compuerta 2: " + foodLevelc2 + "%");

                } else {
                    Toast.makeText(this, "Tipo: " + tipo, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al parsear mensaje", Toast.LENGTH_SHORT).show();
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
                        //
                        JSONObject data = response.getJSONObject("data");


                        JSONObject floodgates = data.getJSONObject("floodgates");

                        JSONObject fg1 = floodgates.getJSONObject("1");
                        int foodLevelc1 = fg1.getInt("foodLevel");

                        JSONObject fg2 = floodgates.getJSONObject("2");
                        int foodLevelc2 = fg2.getInt("foodLevel");

                        // Actualizar la interfaz de usuario con los nuevos valores
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

    private void mostrarDialogoAgregar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Horario");

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_agregar_horario, null);
        builder.setView(customLayout);

        TextView txtFecha = customLayout.findViewById(R.id.txtFecha);
        TextView txtHora = customLayout.findViewById(R.id.txtHora);

        Calendar calendar = Calendar.getInstance();

        txtFecha.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> txtFecha.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        txtHora.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> txtHora.setText(hourOfDay + ":" + String.format("%02d", minute)),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
            );
            timePickerDialog.show();
        });

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String fecha = txtFecha.getText().toString();
            String hora = txtHora.getText().toString();
            Toast.makeText(this, "Horario agregado: " + fecha + " a las " + hora, Toast.LENGTH_LONG).show();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
