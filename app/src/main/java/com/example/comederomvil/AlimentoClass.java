package com.example.comederomvil;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Calendar;

public class AlimentoClass extends AppCompatActivity {

    // Variables para los botones
    private Button openCs, closeCs, openC1, closeC1, openC2, closeC2;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_alimento);

        // Configurar los botones
        openCs = findViewById(R.id.bntOpenCs);
        closeCs = findViewById(R.id.btnCloseCs);
        openC1 = findViewById(R.id.bntOpenC1);
        closeC1 = findViewById(R.id.btnCloseC1);
        openC2 = findViewById(R.id.bntOpenC2);
        closeC2 = findViewById(R.id.btnCloseC2);


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


        ImageView btnAgregar1 = findViewById(R.id.btnAgregar1);
        ImageView btnAgregar2 = findViewById(R.id.btnAgregar2);
        ImageView btnAgregarTodas = findViewById(R.id.btnAgregarTodas);

        View.OnClickListener agregarHorarioListener = v -> mostrarDialogoAgregar();

        btnAgregar1.setOnClickListener(agregarHorarioListener);
        btnAgregar2.setOnClickListener(agregarHorarioListener);
        btnAgregarTodas.setOnClickListener(agregarHorarioListener);

        WebSocketManager.getInstance().send("Hola desde el Alimentos");
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
