package es.studium.mispedidospendientes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Pedido {

    private int idPedido;
    private String fechaPedido;
    private String fechaEstimadaPedido;
    private String descripcionPedido;
    private double importePedido;
    private int estadoPedido;
    private int idTienda;
    private String nombreTienda;

    public Pedido() {}

    public Pedido(int idPedido, String fechaPedido, String fechaEstimadaPedido, String descripcionPedido,
                  double importePedido, int estadoPedido, int idTienda, String nombreTienda) {

        this.idPedido = idPedido;
        this.fechaPedido = fechaPedido;
        this.fechaEstimadaPedido = fechaEstimadaPedido;
        this.descripcionPedido = descripcionPedido;
        this.importePedido = importePedido;
        this.estadoPedido = estadoPedido;
        this.idTienda = idTienda;
        this.nombreTienda = nombreTienda;
    }

    // Convertir un objeto Date en una cadena de texto (String)
    private String formatearFecha(String fechaOriginal) {

        SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat formatoSalida = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date fecha = formatoEntrada.parse(fechaOriginal);
            return formatoSalida.format(fecha);
        } catch (ParseException e) { //  En el caso que no formatea correctamente, devuelve la fecha original sin cambios.(ParseException e)
            e.printStackTrace();
            return fechaOriginal;
        }
    }

    public String getFechaPedido() {
        return formatearFecha(fechaPedido);
    }

    public String getFechaEstimadaPedido() {
        return formatearFecha(fechaEstimadaPedido);
    }

    public int getIdPedido() {
        return idPedido;
    }

    public String getDescripcionPedido() {
        return descripcionPedido;
    }

    public double getImportePedido() {
        return importePedido;
    }

    public int getEstadoPedido() {
        return estadoPedido;
    }

    public int getIdTienda() {
        return idTienda;
    }

    public String getNombreTienda() {
        return nombreTienda;
    }

    public String getFechaEstimadaFormateada() {
        return fechaEstimadaPedido;
    }
}
