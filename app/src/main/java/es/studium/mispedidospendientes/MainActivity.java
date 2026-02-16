package es.studium.mispedidospendientes;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPedidos;
    private PedidosAdapter adapter;
    private List<Pedido> listaPedidos;
    private Button btnTiendas;
    private FloatingActionButton fabAgregarPedido;

    private AccesoRemoto accesoRemoto = new AccesoRemoto();
    private List<Map.Entry<Integer, String>> tiendasList; // Lista de tiendas para el Spinner


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewPedidos = findViewById(R.id.recyclerViewPedidos);
        btnTiendas = findViewById(R.id.btnTiendas);
        fabAgregarPedido = findViewById(R.id.fabAgregarPedido);

        recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));
        listaPedidos = new ArrayList<>();

        adapter = new PedidosAdapter(listaPedidos, new PedidosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Pedido pedido) {
                modificarPedido(pedido);
            }

            @Override
            public void onItemLongClick(Pedido pedido) {
                eliminarPedido(pedido);
            }
        });
        recyclerViewPedidos.setAdapter(adapter);

        // Reemplazar lambda por OnClickListener tradicional
        btnTiendas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TiendasActivity.class);
                startActivity(intent);
            }
        });

        // Reemplazar lambda por OnClickListener tradicional
        fabAgregarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarPedido();
            }
        });

        cargarPedidos();
    }

    // -------------------- CARGAR PEDIDOS --------------------
    private void cargarPedidos() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Pedido> pedidos = accesoRemoto.obtenerPedidos();
                    Map<Integer, String> tiendasMap = accesoRemoto.obtenerTiendasMap();
                    tiendasList = new ArrayList<>(tiendasMap.entrySet());

                    // Reemplazar lambda por Runnable tradicional
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.actualizarDatos(pedidos);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // -------------------- AGREGAR PEDIDO --------------------
    private void agregarPedido() {
        if (tiendasList == null || tiendasList.isEmpty()) {
            Toast.makeText(this, "Cargando tiendas...", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo Pedido");

        View view = LayoutInflater.from(this).inflate(R.layout.anadir_pedido, null);
        final Spinner spinnerTiendas = view.findViewById(R.id.spinnerTiendas);
        final EditText etFechaEntrega = view.findViewById(R.id.etFechaEntrega);
        final EditText etDescripcion = view.findViewById(R.id.etDescripcion);
        final EditText etImporte = view.findViewById(R.id.etImporte);

        List<String> nombresTiendas = new ArrayList<>();
        for (Map.Entry<Integer, String> entrada : tiendasList) {
            nombresTiendas.add(entrada.getValue());
        }
        spinnerTiendas.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nombresTiendas));

        // Reemplazar lambda por OnClickListener tradicional
        etFechaEntrega.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarSelectorFecha(etFechaEntrega);
            }
        });

        builder.setView(view);
        builder.setPositiveButton("Aceptar", null);
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        // EVENTO AL PULSAR ACEPTAR - Reemplazar lambda por OnClickListener tradicional
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedTiendaIndex = spinnerTiendas.getSelectedItemPosition();
                String fechaEntrega = etFechaEntrega.getText().toString();
                String descripcion = etDescripcion.getText().toString();
                String importe = etImporte.getText().toString();

                boolean hayError = false;

                if (descripcion.isEmpty()) { etDescripcion.setError("Obligatorio"); hayError = true; }
                if (fechaEntrega.isEmpty()) { etFechaEntrega.setError("Obligatorio"); hayError = true; }
                if (importe.isEmpty()) { etImporte.setError("Obligatorio"); hayError = true; }
                if (selectedTiendaIndex == -1) { hayError = true; }

                // VALIDACIÓN: NO PERMITIR FECHAS ANTERIORES
                if (!hayError && !fechaValida(fechaEntrega)) {
                    etFechaEntrega.setError("La fecha no puede ser anterior a hoy");
                    hayError = true;
                }

                // VALIDACIÓN: IMPORTE DEL PEDIDO (POSITIVO)
                if (importe.isEmpty()) {
                    etImporte.setError("Obligatorio");
                    hayError = true;
                } else {
                    try {
                        double valorImporte = Double.parseDouble(importe);
                        if (valorImporte <= 0) {
                            etImporte.setError("Debe ser mayor que 0");
                            hayError = true;
                        }
                    } catch (NumberFormatException e) {
                        etImporte.setError("Formato numérico inválido");
                        hayError = true;
                    }
                }

                if (hayError) return;

                confirmarYEnviar(selectedTiendaIndex, fechaEntrega, descripcion, importe, dialog);
            }
        });
    }

    private void confirmarYEnviar(final int index, final String fecha, final String desc, final String imp, final AlertDialog dialogPadre) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar")
                .setMessage("¿Deseas agregar este pedido?")
                // Reemplazar lambda por DialogInterface.OnClickListener tradicional
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int i) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    int idTienda = tiendasList.get(index).getKey();
                                    accesoRemoto.agregarPedido(idTienda, convertirFechaAPI(fecha), desc, imp);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Agregado", Toast.LENGTH_SHORT).show();
                                            cargarPedidos();
                                            dialogPadre.dismiss();
                                        }
                                    });
                                } catch (Exception e) { e.printStackTrace(); }
                            }
                        }).start();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    // -------------------- MODIFICAR PEDIDO --------------------
    public void modificarPedido(final Pedido pedido) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modificar Pedido");

        View view = LayoutInflater.from(this).inflate(R.layout.modificar_pedido, null);
        final Spinner spinnerTiendas = view.findViewById(R.id.spinnerTiendas);
        final EditText etFechaEntrega = view.findViewById(R.id.etFechaEntrega);
        final EditText etDescripcion = view.findViewById(R.id.etDescripcion);
        final EditText etImporte = view.findViewById(R.id.etImporte);
        final CheckBox cbxEntregado = view.findViewById(R.id.cbEntregado);

        // Configurar Spinner
        List<String> nombresTiendas = new ArrayList<>();
        int posicionTiendaActual = 0;
        for (int i = 0; i < tiendasList.size(); i++) {
            nombresTiendas.add(tiendasList.get(i).getValue());
            if (tiendasList.get(i).getKey() == pedido.getIdTienda()) posicionTiendaActual = i;
        }
        spinnerTiendas.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nombresTiendas));
        spinnerTiendas.setSelection(posicionTiendaActual);

        // Rellenar datos
        etFechaEntrega.setText(pedido.getFechaEstimadaPedido());
        etDescripcion.setText(pedido.getDescripcionPedido());
        etImporte.setText(String.valueOf(pedido.getImportePedido()));
        cbxEntregado.setChecked(pedido.getEstadoPedido() == 1);

        // Reemplazar lambda por OnClickListener tradicional
        etFechaEntrega.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarSelectorFecha(etFechaEntrega);
            }
        });

        builder.setView(view);
        // Reemplazar lambda por DialogInterface.OnClickListener tradicional
        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String f = etFechaEntrega.getText().toString();

                // VALIDACIÓN: No permitir fechas pasadas
                if (!fechaValida(f)) {
                    Toast.makeText(MainActivity.this, "Fecha no válida (es anterior a hoy)", Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int idTiendaNueva = tiendasList.get(spinnerTiendas.getSelectedItemPosition()).getKey();
                            accesoRemoto.actualizarPedido(
                                    pedido.getIdPedido(),
                                    pedido.getFechaPedido(), // No se convierte, ya es yyyy-MM-dd
                                    convertirFechaAPI(f),
                                    etDescripcion.getText().toString(),
                                    etImporte.getText().toString(),
                                    cbxEntregado.isChecked() ? 1 : 0,
                                    idTiendaNueva);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Actualizado", Toast.LENGTH_SHORT).show();
                                    cargarPedidos();
                                }
                            });
                        } catch (IOException e) { e.printStackTrace(); }
                    }
                }).start();
            }
        });
        builder.setNegativeButton("Cancelar", null).show();
    }

    // -------------------- ELIMINAR PEDIDO --------------------
    private void eliminarPedido(final Pedido pedido) {

        if (pedido.getEstadoPedido() == 1) {
            Toast.makeText(this, "No se puede eliminar un pedido entregado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si no está entregado, procedemos con la confirmación
        AlertDialog.Builder borrar = new AlertDialog.Builder(this);
        borrar.setTitle("Eliminar Pedido");
        borrar.setMessage("¿Estás seguro de que deseas eliminar el pedido: " + pedido.getDescripcionPedido() + "?");
        borrar.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            accesoRemoto.eliminarPedido(pedido.getIdPedido());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    cargarPedidos();
                                    Toast.makeText(MainActivity.this, "Pedido eliminado correctamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        borrar.setNegativeButton("Cancelar", null);
        borrar.show();
    }

    // -------------------- UTILIDADES --------------------
    // MOSTRAR SELECTOR DE FECHA
    private void mostrarSelectorFecha(final EditText campo) {

        Calendar c = Calendar.getInstance(); // Obtenemos la fecha actual

        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) { // Cuando se selecciona una fecha
                campo.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year));
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        // BLOQUEAR FECHAS ANTERIORES (visual en el calendario del movil)
        dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dpd.show();
    }

    // VALIDAR FECHA
    private boolean fechaValida(String fechaStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date fechaSeleccionada = sdf.parse(fechaStr);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0); // Establece la hora en 0
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            // Válida si NO es anterior a hoy (permite hoy y futuro)
            return !fechaSeleccionada.before(cal.getTime());
        } catch (ParseException e) { return false; }
    }

    // CONVERTIR FECHA
    private String convertirFechaAPI(String fecha) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return out.format(in.parse(fecha));
        } catch (Exception e) { return fecha; }
    }
}
