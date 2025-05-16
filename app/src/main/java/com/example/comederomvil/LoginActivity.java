package com.example.comederomvil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private RequestQueue queue;
    private Button btnIngresar;
    private TextView btnRegistro, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa el SessionManager
        sessionManager = new SessionManager(this);

        // Inicializar la cola de solicitudes de Volley
        queue = Volley.newRequestQueue(this);

        // Verifica si el usuario ya está logueado
        if (sessionManager.isLoggedIn()) {
            // Redirigir a MainActivity si ya está logueado
            startActivity(new Intent(this, MainActivity.class));
            // Cerrar la actividad actual
            finish();
        }


        this.email = findViewById(R.id.email);
        this.password = findViewById(R.id.password);

        this.btnIngresar = findViewById(R.id.btn_ingresar);
        btnIngresar.setOnClickListener(v -> {
            login();
        });

        this.btnRegistro = findViewById(R.id.btn_registro);
        btnRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });
    }


    // Método para realizar la solicitud de inicio de sesión
    private void login() {
        // Aquí se debe realizar la solicitud a la API
        String url = getString(R.string.api_base_url) + "auth";

        // Crear un objeto UsuarioLogin con los datos ingresados por el usuario
        UsuarioLogin usuarioLogin = new UsuarioLogin(email.getText().toString(), password.getText().toString());

        // Validar los campos ingresados por el usuario
        if (!usuarioLogin.validarCampos()) {
            Toast.makeText(this, "Llena los campos correctamente", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, usuarioLogin.getJSON(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Procesar la respuesta
                        try {
                            // Obtener el código de estado de la respuesta
                            String ok = response.getString("ok");
                            if (ok.equals("true")) {

                                JSONObject user = response.getJSONObject("usuario");
                                String id_person = user.getString("id");
                                String name = user.getString("name");
                                String paternal_surname = user.getString("paternalSurname");
                                String maternal_surname = user.getString("maternalSurname");
                                String email = user.getString("email");
                                String phone = user.getString("phone");
                                String rol = user.getString("rol");
                                String token = response.getString("token");

                                sessionManager.createLoginSession(true, id_person, name, paternal_surname, maternal_surname, phone, email, rol, token);

                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(jsonObjectRequest);
    }
}
