package com.example.comederomvil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AvesClass extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_ave);

        Button btnHistorial = findViewById(R.id.btn_historial);
        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(AvesClass.this, HistorialClass.class);
            startActivity(intent);
        });
    }
}
