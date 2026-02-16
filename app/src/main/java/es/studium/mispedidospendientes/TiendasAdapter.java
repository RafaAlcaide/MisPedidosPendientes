package es.studium.mispedidospendientes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TiendasAdapter extends RecyclerView.Adapter<TiendasAdapter.ViewHolder> {
    private final List<Tienda> tiendas;
    private final TiendasActivity activity;

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
        final Tienda tienda = tiendas.get(position);
        holder.tvNombreTienda.setText(tienda.getNombreTienda());

        // CLIC CORTO: MODIFICAR
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.modificarTienda(tienda);
            }
        });

        // CLIC LARGO: ELIMINAR
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Eliminar Tienda")
                        .setMessage("¿Estás seguro de que quieres eliminar " + tienda.getNombreTienda() + "?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.eliminarTienda(tienda);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return tiendas.size();
    }

    // ViewHolder interno para reciclar vista de elementos de la lista de tiendas (item_tienda.xml)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreTienda;
        public ViewHolder(View itemView) {
            super(itemView);
            tvNombreTienda = itemView.findViewById(R.id.tvNombreTienda);
        }
    }
}
