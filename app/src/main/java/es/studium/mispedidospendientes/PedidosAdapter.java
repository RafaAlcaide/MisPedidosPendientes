package es.studium.mispedidospendientes;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.ViewHolder> {
    private List<Pedido> pedidos;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemLongClick(Pedido pedido);
    }

    public PedidosAdapter(Context context, List<Pedido> pedidos, OnItemClickListener listener) {
        this.context = context;
        this.pedidos = pedidos;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);
        holder.tvDescripcionPedido.setText(pedido.getDescripcionPedido());
        holder.tvFechaPedido.setText("Pedido: " + pedido.getFechaPedidoFormateada());
        holder.tvFechaEstimadaPedido.setText("Entrega: " + pedido.getFechaEstimadaPedidoFormateada());
        holder.tvNombreTienda.setText("Tienda: " + pedido.getNombreTienda());

        holder.itemView.setOnClickListener(v -> {
            Log.d("ADAPTER_CLICK", "Pedido seleccionado para editar: " + pedido.getIdPedido());
            if (context instanceof MainActivity) {
                ((MainActivity) context).mostrarDialogoModificarPedido(pedido);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar Pedido")
                    .setMessage("¿Seguro que quieres eliminar este pedido?")
                    .setPositiveButton("Sí", (dialog, which) -> onItemClickListener.onItemLongClick(pedido))
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescripcionPedido, tvFechaPedido, tvFechaEstimadaPedido, tvNombreTienda;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDescripcionPedido = itemView.findViewById(R.id.tvDescripcionPedido);
            tvFechaPedido = itemView.findViewById(R.id.tvFechaPedido);
            tvFechaEstimadaPedido = itemView.findViewById(R.id.tvFechaEstimadaPedido);
            tvNombreTienda = itemView.findViewById(R.id.tvNombreTienda);
        }
    }
}