package com.example.comederomvil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VisitasAdapter extends RecyclerView.Adapter<VisitasAdapter.VisitaViewHolder> {

    private List<String> visitas;

    public VisitasAdapter(List<String> visitas) {
        this.visitas = visitas;
    }

    public void updateData(List<String> newVisitas) {
        this.visitas = newVisitas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VisitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visita, parent, false);
        return new VisitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitaViewHolder holder, int position) {
        holder.bind(visitas.get(position));
    }

    @Override
    public int getItemCount() {
        return visitas.size();
    }

    static class VisitaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFechaVisita;

        public VisitaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFechaVisita = itemView.findViewById(R.id.tvFechaVisita);
        }

        public void bind(String fechaVisita) {
            tvFechaVisita.setText(fechaVisita);
        }
    }
}