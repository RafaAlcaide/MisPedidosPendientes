package es.studium.mispedidospendientes;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TiendasAdapter extends RecyclerView.Adapter<TiendasAdapter.ViewHolder> {
    private List<Tienda> tiendas;
    private TiendasActivity activity;

    public TiendasAdapter(TiendasActivity activity, List<Tienda> tiendas) {
        this.activity = activity;
        this.tiendas = tiendas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tienda, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tienda tienda = tiendas.get(position);
        holder.tvNombreTienda.setText(tienda.getNombreTienda());

        holder.itemView.setOnClickListener(v -> activity.mostrarDialogoEditarTienda(tienda));

        holder.itemView.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Eliminar Tienda");
            builder.setMessage("¿Estás seguro de que quieres eliminar la tienda " + tienda.getNombreTienda() + "?");

            builder.setPositiveButton("Sí", (dialog, which) -> activity.eliminarTienda(tienda));
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            builder.show();
            return true;
        });
    }

    @Override
    public int getItemCount()

    {
        return tiendas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvNombreTienda;

        public ViewHolder(View itemView)
        {
            super(itemView);
            tvNombreTienda = itemView.findViewById(R.id.tvNombreTienda);
        }
    }
}