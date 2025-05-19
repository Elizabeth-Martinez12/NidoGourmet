package com.example.comederomvil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class AvesClass extends AppCompatActivity {

    private RecyclerView rvVisitas1, rvVisitas2;
    private TextView tvTotalVisitas1, tvTotalVisitas2;
    private VisitasAdapter adapter1, adapter2;

    private SessionManager sessionManager;
    private String SERVER_URL;
    private RequestQueue listaSolicitudes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_ave);

        // Inicializar el SessionManager
        this.sessionManager = new SessionManager(this);

        this.listaSolicitudes = Volley.newRequestQueue(this);

        // Configurar toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Inicializar vistas
        tvTotalVisitas1 = findViewById(R.id.tvTotalVisitas1);
        tvTotalVisitas2 = findViewById(R.id.tvTotalVisitas2);

        rvVisitas1 = findViewById(R.id.rvVisitas1);
        rvVisitas2 = findViewById(R.id.rvVisitas2);

        rvVisitas1.setLayoutManager(new LinearLayoutManager(this));
        rvVisitas2.setLayoutManager(new LinearLayoutManager(this));

        adapter1 = new VisitasAdapter(new ArrayList<>());
        adapter2 = new VisitasAdapter(new ArrayList<>());

        rvVisitas1.setAdapter(adapter1);
        rvVisitas2.setAdapter(adapter2);

        SERVER_URL = getString(R.string.api_base_url);


        getLastData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        WebSocketManager.getInstance().setMessageListener(this::handleWebSocketMessage);
    }

    @Override
    protected void onStop() {
        super.onStop();
        WebSocketManager.getInstance().removeMessageListener();
    }

    private void handleWebSocketMessage(String message) {
        runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(message);
                if (json.getString("type").equals("update_status")) {
                    JSONObject floodgates = json.getJSONObject("floodgates");

                    // Procesar compuerta 1
                    JSONObject fg1 = floodgates.getJSONObject("1");
                    JSONArray visits1 = fg1.getJSONArray("visits");
                    List<String> visitasFormateadas1 = formatVisits(visits1);
                    adapter1.updateData(visitasFormateadas1);
                    tvTotalVisitas1.setText("Total visitas: " + visits1.length());

                    // Procesar compuerta 2
                    JSONObject fg2 = floodgates.getJSONObject("2");
                    JSONArray visits2 = fg2.getJSONArray("visits");
                    List<String> visitasFormateadas2 = formatVisits(visits2);
                    adapter2.updateData(visitasFormateadas2);
                    tvTotalVisitas2.setText("Total visitas: " + visits2.length());
                }
            } catch (JSONException e) {
                Log.e("AvesClass", "Error parsing WebSocket message", e);
            }
        });
    }

    private List<String> formatVisits(JSONArray visits) throws JSONException {
        List<Date> dates = new ArrayList<>();

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Asegura el parseo correcto

        SimpleDateFormat outputFormat = new SimpleDateFormat("d 'de' MMMM 'de' yyyy 'a las' h:mma", new Locale("es", "ES"));
        outputFormat.setTimeZone(TimeZone.getDefault()); // Zona local del dispositivo

        // Parsear las fechas y agregarlas a la lista
        for (int i = 0; i < visits.length(); i++) {
            String dateStr = visits.getString(i);
            try {
                Date date = inputFormat.parse(dateStr);
                if (date != null) {
                    dates.add(date);
                }
            } catch (ParseException e) {
                Log.e("formatVisits", "Error parseando fecha: " + dateStr, e);
            }
        }

        // Ordenar fechas de más reciente a más antigua
        dates.sort((d1, d2) -> Long.compare(d2.getTime(), d1.getTime()));

        // Formatear las fechas ordenadas
        List<String> formattedVisits = new ArrayList<>();
        for (Date date : dates) {
            String formatted = outputFormat.format(date)
                    .replace("AM", "am")
                    .replace("PM", "pm");
            formattedVisits.add(formatted);
        }

        return formattedVisits;
    }


    // metodo para obtener la ultima data del servidor
    private void getLastData() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, SERVER_URL + "device/feeder/67f57821c597ab31781c074d", null,
                response -> {
                    try {
                        if (response.has("data")) {
                            // intentar obtener el objeto "data"
                            JSONObject datos = response.getJSONObject("data");


                            // procesar el objeto "data"
                            JSONObject floodgates = datos.getJSONObject("floodgates");

                            // obteniendo las compuertas
                            JSONObject fg1 = floodgates.getJSONObject("1");
                            JSONObject fg2 = floodgates.getJSONObject("2");

                            // obtener el arreglo de visitas
                            JSONArray visits1 = fg1.getJSONArray("visits");
                            JSONArray visits2 = fg2.getJSONArray("visits");

                            // formatear las visitas
                            List<String> visitasFormateadas1 = formatVisits(visits1);
                            adapter1.updateData(visitasFormateadas1);
                            tvTotalVisitas1.setText("Total visitas: " + visits1.length());

                            List<String> visitasFormateadas2 = formatVisits(visits2);
                            adapter2.updateData(visitasFormateadas2);
                            tvTotalVisitas2.setText("Total visitas: " + visits2.length());

                            // imprimir respuesta
                            Log.d("AlimentoClass", "Respuesta del servidor: " + datos.toString());
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

}