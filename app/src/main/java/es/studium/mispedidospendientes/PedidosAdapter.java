package es.studium.mispedidospendientes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder> {

    public void actualizarDatos(List<Pedido> pedidos) {
        this.listaPedidos = pedidos;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Pedido pedido);
        void onItemLongClick(Pedido pedido);
    }

    private List<Pedido> listaPedidos;
    private OnItemClickListener listener;

    public PedidosAdapter(List<Pedido> listaPedidos, OnItemClickListener listener) {
        this.listaPedidos = listaPedidos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = listaPedidos.get(position);

        // Rellenamos los datos usando los métodos originales de Pedido.java
        holder.tvTienda.setText(pedido.getNombreTienda());
        holder.tvFecha.setText("Realizado: " + pedido.getFechaPedido());
        holder.tvFechaEstimada.setText("Entrega: " + pedido.getFechaEstimadaPedido());
        holder.tvDescripcion.setText(pedido.getDescripcionPedido());
        holder.tvImporte.setText("Importe: " + pedido.getImportePedido() + "€");

        // Eventos de clic
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onItemClick(pedido);
            }
        });

        // Eventos de mantener pulsado
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) listener.onItemLongClick(pedido);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaPedidos.size();
    }

    static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTienda, tvFecha, tvFechaEstimada, tvDescripcion, tvImporte;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTienda = itemView.findViewById(R.id.tvNombreTienda);
            tvFecha = itemView.findViewById(R.id.tvFechaPedido);
            tvFechaEstimada = itemView.findViewById(R.id.tvFechaEstimadaPedido);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionPedido);
            tvImporte = itemView.findViewById(R.id.tvImportePedido);
        }
    }
}
