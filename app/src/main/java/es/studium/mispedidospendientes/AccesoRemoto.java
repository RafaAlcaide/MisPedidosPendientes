package es.studium.mispedidospendientes;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccesoRemoto
{
    private static final String URL_PEDIDOS = "http://10.0.2.2/APIRest/pedidos.php";
    private static final String URL_TIENDAS = "http://10.0.2.2/APIRest/tiendas.php";
    private OkHttpClient client = new OkHttpClient();

    public List<Pedido> obtenerPedidos() throws IOException, JSONException
    {
        Request request = new Request.Builder().url(URL_PEDIDOS).get().build();
        Response response = client.newCall(request).execute();
        List<Pedido> pedidos = new ArrayList<>();

        if (response.body() != null)
        {
            String responseData = response.body().string();
            JSONArray jsonArray = new JSONArray(responseData);
            Map<Integer, String> tiendasMap = obtenerTiendasMap();

            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonPedido = jsonArray.getJSONObject(i);
                int idPedido = jsonPedido.getInt("idPedido");
                String fechaPedido = jsonPedido.getString("fechaPedido");
                String fechaEstimadaPedido = jsonPedido.getString("fechaEstimadaPedido");
                String descripcionPedido = jsonPedido.getString("descripcionPedido");
                double importePedido = jsonPedido.getDouble("importePedido");
                int estadoPedido = jsonPedido.getInt("estadoPedido");
                int idTienda = jsonPedido.getInt("idTiendaFK");
                String nombreTienda = tiendasMap.getOrDefault(idTienda, "Desconocida");

                if (estadoPedido == 0)
                {
                    pedidos.add(new Pedido(idPedido, fechaPedido, fechaEstimadaPedido, descripcionPedido, importePedido, estadoPedido, idTienda, nombreTienda));
                }
            }
        }
        return pedidos;
    }

    public Map<Integer, String> obtenerTiendasMap() throws IOException, JSONException
    {
        Map<Integer, String> tiendas = new HashMap<>();
        Request request = new Request.Builder().url(URL_TIENDAS).get().build();
        Response response = client.newCall(request).execute();
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
        return tiendas;
    }

    public void agregarPedido(int idTienda, String fechaEntrega, String descripcion, String importe) throws IOException
    {
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

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            System.out.println("Pedido agregado correctamente");
        } else {
            System.out.println("Error al agregar el pedido" + response.code());
        }
    }

    public void actualizarPedido(int idPedido, String fechaPedido, String fechaEntrega, String descripcion, String importe, int estado, int idTienda) throws IOException {
        String url = URL_PEDIDOS + "?idPedido=" + idPedido +
                "&fechaPedido=" + fechaPedido +
                "&fechaEstimadaPedido=" + fechaEntrega +
                "&descripcionPedido=" + descripcion +
                "&importePedido=" + importe +
                "&estadoPedido=" + estado +
                "&idTiendaFK=" + idTienda;

        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(null, new byte[0]))
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).execute();
    }

    public void eliminarPedido(int idPedido) throws IOException
    {
        String url = URL_PEDIDOS + "?idPedido=" + idPedido;
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        client.newCall(request).execute();
    }

    public List<Tienda> obtenerTiendas() throws IOException, JSONException
    {
        Request request = new Request.Builder().url(URL_TIENDAS).get().build();
        Response response = client.newCall(request).execute();
        List<Tienda> tiendas = new ArrayList<>();
        if (response.body() != null) {
            String responseData = response.body().string();
            JSONArray jsonArray = new JSONArray(responseData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonTienda = jsonArray.getJSONObject(i);
                int idTienda = jsonTienda.getInt("idTienda");
                String nombreTienda = jsonTienda.getString("nombreTienda");
                tiendas.add(new Tienda(idTienda, nombreTienda));
            }
        }
        return tiendas;
    }

    public void agregarTienda(String nombreTienda) throws IOException
    {
        RequestBody body = new FormBody.Builder()
                .add("nombreTienda", nombreTienda)
                .build();

        Request request = new Request.Builder()
                .url(URL_TIENDAS)
                .post(body)
                .build();

        client.newCall(request).execute();
    }

    public void actualizarTienda(int idTienda, String nuevoNombre) throws IOException
    {
        String url = URL_TIENDAS + "?idTienda=" + idTienda + "&nombreTienda=" + nuevoNombre;
        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(null, new byte[0]))
                .build();

        client.newCall(request).execute();
    }

    public void eliminarTienda(int idTienda) throws IOException
    {
        String url = URL_TIENDAS + "?idTienda=" + idTienda;
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        client.newCall(request).execute();
    }
}