package es.studium.mispedidospendientes;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class AccesoRemoto {

    private static final String URL_PEDIDOS = "http://10.0.2.2/ApiRest/pedidos.php";
    private static final String URL_TIENDAS = "http://10.0.2.2/ApiRest/tiendas.php";

    private final OkHttpClient client = new OkHttpClient();

    // -------------------- PEDIDOS --------------------
    // -------------------- OBTENER PEDIDOS --------------------
    public List<Pedido> obtenerPedidos() throws IOException, JSONException {

        List<Pedido> pedidos = new ArrayList<>();

        Request request = new Request.Builder().url(URL_PEDIDOS).get().build();
        Response response = null;

        Map<Integer, String> tiendasMap = obtenerTiendasMap();

        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Error HTTP: " + response.code());
            }
            if (response.body() != null) {
                String responseData = response.body().string();
                JSONArray jsonArray = new JSONArray(responseData);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonPedido = jsonArray.getJSONObject(i);

                    // Obtener los datos del pedido
                    int idPedido = jsonPedido.getInt("idPedido");
                    String fechaPedido = jsonPedido.getString("fechaPedido");
                    String fechaEstimadaPedido = jsonPedido.getString("fechaEstimadaPedido");
                    String descripcionPedido = jsonPedido.getString("descripcionPedido");
                    double importePedido = jsonPedido.getDouble("importePedido");
                    int estadoPedido = jsonPedido.getInt("estadoPedido");
                    int idTienda = jsonPedido.getInt("idTiendaFK");

                    String nombreTienda = tiendasMap.getOrDefault(idTienda, "Desconocida");

                    if (estadoPedido == 0) {
                        pedidos.add(new Pedido(
                                idPedido,
                                fechaPedido,
                                fechaEstimadaPedido,
                                descripcionPedido,
                                importePedido,
                                estadoPedido,
                                idTienda,
                                nombreTienda
                        ));
                    }
                }
            }
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
        return pedidos;
    }

    // -------------------- AGREGAR PEDIDOS --------------------
    public void agregarPedido(int idTienda, String fechaEntrega,
                              String descripcion, String importe) throws IOException {

        RequestBody body = new FormBody.Builder()
                .add("fechaEstimadaPedido", fechaEntrega)
                .add("descripcionPedido", descripcion)
                .add("importePedido", importe)
                .add("idTiendaFK", String.valueOf(idTienda))
                .build();

        Request request = new Request.Builder()
                .url(URL_PEDIDOS)
                .post(body)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Error al agregar pedido. Código: " + response.code());
            }
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
    }

    // -------------------- ACTUALIZAR PEDIDOS --------------------
    public void actualizarPedido(int idPedido, String fechaPedido, String fechaEstimada, String desc,
                                 String imp, int estado, int idTienda) throws IOException {

        HttpUrl urlFinal = HttpUrl.parse(URL_PEDIDOS).newBuilder()
                .addQueryParameter("idPedido", String.valueOf(idPedido))
                .addQueryParameter("fechaPedido", fechaPedido)
                .addQueryParameter("fechaEstimadaPedido", fechaEstimada)
                .addQueryParameter("descripcionPedido", desc)
                .addQueryParameter("importePedido", imp)
                .addQueryParameter("estadoPedido", String.valueOf(estado))
                .addQueryParameter("idTiendaFK", String.valueOf(idTienda))
                .build();

        RequestBody cuerpoVacio = RequestBody.create(MediaType.parse("text/plain"), "");

        Request request = new Request.Builder()
                .url(urlFinal)
                .put(cuerpoVacio)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("El servidor no respondió correctamente");
        }
        if (response.body() != null) response.body().close();
    }

    // -------------------- ELIMINAR PEDIDOS --------------------
    public void eliminarPedido(int idPedido) throws IOException {
        HttpUrl url = HttpUrl.parse(URL_PEDIDOS).newBuilder()
                .addQueryParameter("idPedido", String.valueOf(idPedido))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Error al eliminar pedido. Código: " + response.code());
        }
        if (response.body() != null) response.body().close();
    }

    // -------------------- TIENDAS --------------------

    // -------------------- OBTENER TIENDAS (MAP) --------------------
    public Map<Integer, String> obtenerTiendasMap() throws IOException, JSONException {
        Map<Integer, String> tiendas = new HashMap<>();
        Request request = new Request.Builder().url(URL_TIENDAS).get().build();
        Response response = null;

        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Error HTTP: " + response.code());
            }
            if (response.body() != null) {
                String responseData = response.body().string();
                JSONArray jsonArray = new JSONArray(responseData);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonTienda = jsonArray.getJSONObject(i);
                    int idTienda = jsonTienda.getInt("idTienda");
                    String nombreTienda = jsonTienda.getString("nombreTienda");
                    tiendas.put(idTienda, nombreTienda);
                }
            }
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
        return tiendas;
    }

    // -------------------- OBTENER TIENDAS (LIST) --------------------
    public List<Tienda> obtenerTiendas() throws IOException, JSONException {
        List<Tienda> listaTiendas = new ArrayList<>();
        Request request = new Request.Builder().url(URL_TIENDAS).get().build();
        Response response = null;

        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Error HTTP: " + response.code());
            }
            if (response.body() != null) {
                String responseData = response.body().string();
                JSONArray jsonArray = new JSONArray(responseData);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonTienda = jsonArray.getJSONObject(i);
                    int idTienda = jsonTienda.getInt("idTienda");
                    String nombreTienda = jsonTienda.getString("nombreTienda");
                    listaTiendas.add(new Tienda(idTienda, nombreTienda));
                }
            }
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
        return listaTiendas;
    }

    // -------------------- AGREGAR TIENDAS --------------------
    public void agregarTienda(String nombreTienda) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("nombreTienda", nombreTienda)
                .build();

        Request request = new Request.Builder()
                .url(URL_TIENDAS)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Error al agregar tienda. Código: " + response.code());
        }
        if (response.body() != null) response.body().close();
    }

    // -------------------- ACTUALIZAR TIENDAS (AHORA IGUAL QUE PEDIDOS) --------------------
    public void actualizarTienda(int idTienda, String nuevoNombre) throws IOException {
        // Usamos addQueryParameter para idTienda y nombreTienda tal cual haces en Pedidos
        HttpUrl url = HttpUrl.parse(URL_TIENDAS).newBuilder()
                .addQueryParameter("idTienda", String.valueOf(idTienda))
                .addQueryParameter("nombreTienda", nuevoNombre)
                .build();

        // Creamos el cuerpo vacío con el mismo MediaType que Pedidos
        RequestBody cuerpoVacio = RequestBody.create(MediaType.parse("text/plain"), "");

        // Usamos PUT, que es el método que te funciona en actualizarPedido
        Request request = new Request.Builder()
                .url(url)
                .put(cuerpoVacio)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Error al actualizar tienda. Código: " + response.code());
        }
        if (response.body() != null) response.body().close();
    }

    // -------------------- ELIMINAR TIENDAS (AHORA IGUAL QUE PEDIDOS) --------------------
    public void eliminarTienda(int idTienda) throws IOException {
        HttpUrl url = HttpUrl.parse(URL_TIENDAS).newBuilder()
                .addQueryParameter("idTienda", String.valueOf(idTienda))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Error al eliminar tienda. Código: " + response.code());
        }
        if (response.body() != null) response.body().close();
    }
}
