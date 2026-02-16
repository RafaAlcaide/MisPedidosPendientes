package es.studium.mispedidospendientes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TiendasActivity extends AppCompatActivity {
    private RecyclerView recyclerViewTiendas;
    private TiendasAdapter adapter;
    private List<Tienda> listaTiendas;
    private Button btnVolver;
    private FloatingActionButton fabAgregarTienda;
    private AccesoRemoto accesoRemoto = new AccesoRemoto();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiendas);

        recyclerViewTiendas = findViewById(R.id.recyclerViewTiendas);
        btnVolver = findViewById(R.id.btnVolver);
        fabAgregarTienda = findViewById(R.id.fabAgregarTienda);

        recyclerViewTiendas.setLayoutManager(new LinearLayoutManager(this));
        listaTiendas = new ArrayList<>();
        adapter = new TiendasAdapter(this, listaTiendas);
        recyclerViewTiendas.setAdapter(adapter);

        cargarTiendas();

        // Volver a la actividad principal
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TiendasActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Agregar nueva tienda
        fabAgregarTienda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarTienda();
            }
        });
    }

    // Cargar tiendas desde el servidor
    private void cargarTiendas() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Tienda> nuevasTiendas = accesoRemoto.obtenerTiendas();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listaTiendas.clear();
                            listaTiendas.addAll(nuevasTiendas);
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // -------------------- AGREGAR TIENDA --------------------
    private void agregarTienda() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Nueva Tienda");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.anadir_tienda, null);
        final EditText inputTienda = viewInflated.findViewById(R.id.etNombreTienda);
        builder.setView(viewInflated);

        builder.setPositiveButton("Agregar", null); // Control manual abajo
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Control manual del botón
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nombreTienda = inputTienda.getText().toString().trim();
                if (nombreTienda.isEmpty()) {
                    inputTienda.setError("El nombre es obligatorio");
                    return;
                }
                if (existeTienda(nombreTienda)) {
                    inputTienda.setError("Esa tienda ya existe");
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            accesoRemoto.agregarTienda(nombreTienda);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    cargarTiendas();
                                    Toast.makeText(TiendasActivity.this, "Tienda agregada", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    // -------------------- VALIDACIONES --------------------
    // VALIDAR SI EXISTE LA TIENDA
    private boolean existeTienda(String nombreTienda) {
        for (Tienda t : listaTiendas) {
            if (t.getNombreTienda().equalsIgnoreCase(nombreTienda)) return true; // No importa si es mayúscula o minúscula
        }
        return false;
    }

    // -------------------- MODIFICAR TIENDA --------------------
    public void modificarTienda(final Tienda tienda) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Tienda");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.modificar_tienda, null);
        final EditText inputTienda = viewInflated.findViewById(R.id.etModificarNombreTienda);

        // Ponemos el nombre actual en el campo de texto
        inputTienda.setText(tienda.getNombreTienda());
        builder.setView(viewInflated);

        // Botón negativo (Cancelar)
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Botón positivo (Guardar) - Lo ponemos a null aquí para controlar el cierre manualmente
        builder.setPositiveButton("Guardar", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Sobrescribimos el click del botón para que no se cierre si hay error
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nuevoNombre = inputTienda.getText().toString().trim();

                if (nuevoNombre.isEmpty()) {
                    inputTienda.setError("El nombre no puede estar vacío");
                    return;
                }

                // Hilo secundario para la red
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 1. Intentamos actualizar en la base de datos remota
                            accesoRemoto.actualizarTienda(tienda.getIdTienda(), nuevoNombre);

                            // 2. Si tiene éxito, volvemos al hilo principal para UI
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Forzamos la recarga completa de la lista desde el servidor
                                    cargarTiendas();
                                    Toast.makeText(TiendasActivity.this, "Tienda actualizada correctamente", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss(); // Cerramos el diálogo SOLO si todo fue bien
                                }
                            });
                        } catch (final Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TiendasActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    // -------------------- ELIMINAR TIENDA --------------------
    public void eliminarTienda(final Tienda tienda) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    accesoRemoto.eliminarTienda(tienda.getIdTienda());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cargarTiendas();
                            Toast.makeText(TiendasActivity.this, "Tienda eliminada", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
