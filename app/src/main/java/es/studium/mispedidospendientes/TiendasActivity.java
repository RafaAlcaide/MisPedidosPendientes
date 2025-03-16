package es.studium.mispedidospendientes;

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


        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(TiendasActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        fabAgregarTienda.setOnClickListener(v -> mostrarDialogoAgregarTienda());
    }

    // CARGAR TIENDAS --------------------
    private void cargarTiendas() {
        new Thread(() -> {
            try {
                listaTiendas = accesoRemoto.obtenerTiendas();
                runOnUiThread(() -> {
                    adapter = new TiendasAdapter(TiendasActivity.this, listaTiendas);
                    recyclerViewTiendas.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                });
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(TiendasActivity.this, "Error al cargar tiendas", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void mostrarDialogoAgregarTienda() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Nueva Tienda");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.anadir_tienda, null);
        final EditText inputTienda = viewInflated.findViewById(R.id.etNombreTienda);
        builder.setView(viewInflated);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombreTienda = inputTienda.getText().toString().trim();
            if (nombreTienda.isEmpty()) {
                Toast.makeText(this, "El nombre de la tienda no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }
            if (existeTienda(nombreTienda)) {
                Toast.makeText(this, "Ya existe una tienda con ese nombre", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> {
                try {
                    accesoRemoto.agregarTienda(nombreTienda);
                    runOnUiThread(() -> {
                        cargarTiendas();
                        Toast.makeText(this, "Tienda agregada correctamente", Toast.LENGTH_SHORT).show();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Error de conexión con la API", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private boolean existeTienda(String nombreTienda) {
        for (Tienda tienda : listaTiendas) {
            if (tienda.getNombreTienda().equalsIgnoreCase(nombreTienda)) {
                return true;
            }
        }
        return false;
    }

    public void mostrarDialogoEditarTienda(Tienda tienda) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Tienda");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.modificar_tienda, null);
        final EditText inputTienda = viewInflated.findViewById(R.id.etModificarNombreTienda);
        inputTienda.setText(tienda.getNombreTienda());

        builder.setView(viewInflated);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevoNombre = inputTienda.getText().toString().trim();

            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "El nombre de la tienda no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            if (existeTienda(nuevoNombre) && !nuevoNombre.equalsIgnoreCase(tienda.getNombreTienda())) {
                Toast.makeText(this, "Ya existe una tienda con ese nombre", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    accesoRemoto.actualizarTienda(tienda.getIdTienda(), nuevoNombre);
                    runOnUiThread(() -> {
                        cargarTiendas();
                        Toast.makeText(this, "Tienda actualizada correctamente", Toast.LENGTH_SHORT).show();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Error de conexión con la API", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void eliminarTienda(Tienda tienda) {
        new Thread(() -> {
            try {
                accesoRemoto.eliminarTienda(tienda.getIdTienda());
                runOnUiThread(() -> {
                    cargarTiendas();
                    Toast.makeText(this, "Tienda eliminada correctamente", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error de conexión con la API", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}