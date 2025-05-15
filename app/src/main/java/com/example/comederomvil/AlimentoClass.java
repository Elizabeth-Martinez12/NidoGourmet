package com.example.comederomvil;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class AlimentoClass extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_alimento);

        ImageView btnAgregar1 = findViewById(R.id.btnAgregar1);
        ImageView btnAgregar2 = findViewById(R.id.btnAgregar2);
        ImageView btnAgregarTodas = findViewById(R.id.btnAgregarTodas);

        View.OnClickListener agregarHorarioListener = v -> mostrarDialogoAgregar();

        btnAgregar1.setOnClickListener(agregarHorarioListener);
        btnAgregar2.setOnClickListener(agregarHorarioListener);
        btnAgregarTodas.setOnClickListener(agregarHorarioListener);
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
