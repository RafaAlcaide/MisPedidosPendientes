package es.studium.mispedidospendientes;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    private RecyclerView recyclerViewPedidos;
    private PedidosAdapter adapter;
    private List<Pedido> listaPedidos;
    private Button btnTiendas;
    private FloatingActionButton fabAgregarPedido;
    private AccesoRemoto accesoRemoto = new AccesoRemoto();
    private Map<Integer, String> tiendasMap;
    private List<Integer> idTiendas;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerViewPedidos = findViewById(R.id.recyclerViewPedidos);
        btnTiendas = findViewById(R.id.btnTiendas);
        fabAgregarPedido = findViewById(R.id.fabAgregarPedido);

        recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));
        listaPedidos = new ArrayList<>();
        adapter = new PedidosAdapter(this, listaPedidos, pedido -> eliminarPedido(pedido));
        recyclerViewPedidos.setAdapter(adapter);

        cargarPedidos();

        btnTiendas.setOnClickListener(v ->
        {
            Intent intent = new Intent(MainActivity.this, TiendasActivity.class);
            startActivity(intent);
        });

        fabAgregarPedido.setOnClickListener(v -> mostrarDialogoAgregarPedido());
    }

    // CARGAR PEDIDOS
    private void cargarPedidos()
    {
        new Thread(() -> {
            try {
                listaPedidos = accesoRemoto.obtenerPedidos();
                tiendasMap = accesoRemoto.obtenerTiendasMap();
                idTiendas = new ArrayList<>(tiendasMap.keySet());

                runOnUiThread(() ->
                {
                    adapter = new PedidosAdapter(MainActivity.this, listaPedidos, pedido -> eliminarPedido(pedido));
                    recyclerViewPedidos.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                });
            } catch (IOException | JSONException e)
            {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error al cargar pedidos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void mostrarDialogoAgregarPedido()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo Pedido");

        View view = LayoutInflater.from(this).inflate(R.layout.anadir_pedido, null);
        Spinner spinnerTiendas = view.findViewById(R.id.spinnerTiendas);
        EditText etFechaEntrega = view.findViewById(R.id.etFechaEntrega);
        EditText etDescripcion = view.findViewById(R.id.etDescripcion);
        EditText etImporte = view.findViewById(R.id.etImporte);

        ArrayAdapter<String> adapterTiendas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(tiendasMap.values()));
        spinnerTiendas.setAdapter(adapterTiendas);

        etFechaEntrega.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this, (view1, year, month, day) -> {
                etFechaEntrega.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        builder.setView(view);
        builder.setPositiveButton("Aceptar", (dialog, which) ->
        {
            int selectedTiendaIndex = spinnerTiendas.getSelectedItemPosition();
            if (selectedTiendaIndex == -1) return;

            int idTienda = idTiendas.get(selectedTiendaIndex);
            String fechaEntrega = etFechaEntrega.getText().toString();
            String descripcion = etDescripcion.getText().toString();
            String importe = etImporte.getText().toString();

            new Thread(() -> {
                try {
                    accesoRemoto.agregarPedido(idTienda, convertirFechaParaAPI(fechaEntrega), descripcion, importe);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Pedido agregado correctamente", Toast.LENGTH_SHORT).show();
                        cargarPedidos();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private String convertirFechaParaAPI(String fecha)
    {
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat formatoSalida = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            Date fechaConvertida = formatoEntrada.parse(fecha);
            return formatoSalida.format(fechaConvertida);
        } catch (ParseException e) {
            e.printStackTrace();
            return fecha;
        }
    }

    public void mostrarDialogoModificarPedido(Pedido pedido)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modificar Pedido");

        View view = LayoutInflater.from(this).inflate(R.layout.modificar_pedido, null);
        TextView tvNumeroPedido = view.findViewById(R.id.tvNumeroPedido);
        EditText etFechaEntrega = view.findViewById(R.id.etFechaEntrega);
        EditText etDescripcion = view.findViewById(R.id.etDescripcion);
        EditText etImporte = view.findViewById(R.id.etImporte);
        CheckBox cbEntregado = view.findViewById(R.id.cbEntregado);
        Spinner spinnerTiendas = view.findViewById(R.id.spinnerTiendas);

        tvNumeroPedido.setText("Pedido nº " + pedido.getIdPedido());
        etFechaEntrega.setText(pedido.getFechaEstimadaPedidoFormateada());
        etDescripcion.setText(pedido.getDescripcionPedido());
        etImporte.setText(String.valueOf(pedido.getImportePedido()));
        cbEntregado.setChecked(pedido.getEstadoPedido() == 1);

        List<String> nombresTiendas = new ArrayList<>(tiendasMap.values());
        ArrayAdapter<String> adapterTiendas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nombresTiendas);
        spinnerTiendas.setAdapter(adapterTiendas);

        int selectedIndex = new ArrayList<>(tiendasMap.keySet()).indexOf(pedido.getIdTienda());
        if (selectedIndex >= 0) {
            spinnerTiendas.setSelection(selectedIndex);
        }

        etFechaEntrega.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this, (view1, year, month, day) -> {
                etFechaEntrega.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        builder.setView(view);
        builder.setPositiveButton("Guardar", (dialog, which) ->
        {
            String nuevaFechaEntrega = etFechaEntrega.getText().toString();
            String nuevaDescripcion = etDescripcion.getText().toString();
            String nuevoImporte = etImporte.getText().toString();
            boolean marcadoEntregado = cbEntregado.isChecked();
            int nuevoEstado = marcadoEntregado ? 1 : 0;

            int selectedTiendaIndex = spinnerTiendas.getSelectedItemPosition();
            int nuevaTiendaID = new ArrayList<>(tiendasMap.keySet()).get(selectedTiendaIndex);

            new Thread(() -> {
                try {
                    accesoRemoto.actualizarPedido(pedido.getIdPedido(), pedido.getFechaPedido(), convertirFechaParaAPI(nuevaFechaEntrega), nuevaDescripcion, nuevoImporte, nuevoEstado, nuevaTiendaID);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Pedido actualizado correctamente", Toast.LENGTH_SHORT).show();
                        cargarPedidos();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void eliminarPedido(Pedido pedido)
    {
        new Thread(() -> {
            try {
                accesoRemoto.eliminarPedido(pedido.getIdPedido());
                runOnUiThread(() -> {
                    listaPedidos.remove(pedido);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Pedido eliminado correctamente", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}