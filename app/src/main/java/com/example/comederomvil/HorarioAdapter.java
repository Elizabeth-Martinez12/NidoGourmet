package com.example.comederomvil;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder> {

    private List<DiaHorario> horarios;
    private int compuertaNum;
    private String feederId;
    private Context context;

    public HorarioAdapter(List<DiaHorario> horarios, int compuertaNum, String feederId, Context context) {
        this.horarios = horarios;
        this.compuertaNum = compuertaNum;
        this.feederId = feederId;
        this.context = context;
    }

    @NonNull
    @Override
    public HorarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_horario, parent, false);
        return new HorarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorarioViewHolder holder, int position) {
        DiaHorario horario = horarios.get(position);
        holder.bind(horario, compuertaNum, feederId, context);
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

    public static class HorarioViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDia;
        private TextView tvHorario;
        private MaterialCardView cardView;
        private Button btnEditar;

        public HorarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDia = itemView.findViewById(R.id.tvDia);
            tvHorario = itemView.findViewById(R.id.tvHorario);
            cardView = itemView.findViewById(R.id.cardHorario);
            btnEditar = itemView.findViewById(R.id.btnEditar);
        }

        public void bind(DiaHorario horario, int compuertaNum, String feederId, Context context) {
            tvDia.setText(horario.getDiaEsp());
            tvHorario.setText(String.format("%s - %s", horario.getStartTime(), horario.getEndTime()));

            btnEditar.setOnClickListener(v -> {
                mostrarDialogoEdicion(horario, compuertaNum, feederId, context);
            });
        }

        private void mostrarDialogoEdicion(DiaHorario horario, int compuertaNum, String feederId, Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Editar horario - " + horario.getDiaEsp());

            View view = LayoutInflater.from(context).inflate(R.layout.dialog_editar_horario, null);
            EditText etHoraInicio = view.findViewById(R.id.etHoraInicio);
            EditText etHoraFin = view.findViewById(R.id.etHoraFin);

            etHoraInicio.setText(horario.getStartTime());
            etHoraFin.setText(horario.getEndTime());

            // Configurar TimePickers
            etHoraInicio.setOnClickListener(v -> mostrarTimePicker(etHoraInicio));
            etHoraFin.setOnClickListener(v -> mostrarTimePicker(etHoraFin));

            builder.setView(view);
            builder.setPositiveButton("Guardar", (dialog, which) -> {
                String nuevoInicio = etHoraInicio.getText().toString();
                String nuevoFin = etHoraFin.getText().toString();

                if (validarHoras(nuevoInicio, nuevoFin)) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("type", "update_floodgate");
                        json.put("feederId", feederId);
                        json.put("floodgate", compuertaNum);
                        json.put("day", horario.getDiaIng());
                        json.put("startTime", nuevoInicio);
                        json.put("endTime", nuevoFin);

                        WebSocketManager.getInstance().send(json.toString());

                        Toast.makeText(context, "Horario actualizado", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Hora de inicio debe ser menor a hora fin", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        }

        private boolean validarHoras(String inicio, String fin) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date horaInicio = sdf.parse(inicio);
                Date horaFin = sdf.parse(fin);
                return horaInicio != null && horaFin != null && horaInicio.before(horaFin);
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }

        private void mostrarTimePicker(EditText editText) {
            // Usamos itemView.getContext() en lugar de context directamente
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePicker = new TimePickerDialog(
                    itemView.getContext(),  // Aquí está el cambio importante
                    (view, hourOfDay, minute) -> {
                        String hora = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        editText.setText(hora);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);
            timePicker.show();
        }
    }
}