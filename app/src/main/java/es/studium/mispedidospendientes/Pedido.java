package es.studium.mispedidospendientes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Pedido
{
    private int idPedido;
    private String fechaPedido;
    private String fechaEstimadaPedido;
    private String descripcionPedido;
    private double importePedido;
    private int estadoPedido;
    private int idTienda;
    private String nombreTienda;

    public Pedido() {}

    public Pedido(int idPedido, String fechaPedido, String fechaEstimadaPedido, String descripcionPedido, double importePedido, int estadoPedido, int idTienda, String nombreTienda)
    {
        this.idPedido = idPedido;
        this.fechaPedido = fechaPedido;
        this.fechaEstimadaPedido = fechaEstimadaPedido;
        this.descripcionPedido = descripcionPedido;
        this.importePedido = importePedido;
        this.estadoPedido = estadoPedido;
        this.idTienda = idTienda;
        this.nombreTienda = nombreTienda;
    }

    private String formatearFecha(String fechaOriginal)
    {
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat formatoSalida = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date fecha = formatoEntrada.parse(fechaOriginal);
            return formatoSalida.format(fecha);
        } catch (ParseException e) {
            e.printStackTrace();
            return fechaOriginal;
        }
    }

    public String getFechaPedidoFormateada() {
        return formatearFecha(fechaPedido);
    }

    public String getFechaEstimadaPedidoFormateada() {
        return formatearFecha(fechaEstimadaPedido);
    }

    public int getIdPedido() {
        return idPedido;
    }

    public String getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(String fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getFechaEstimadaPedido() {
        return fechaEstimadaPedido;
    }

    public void setFechaEstimadaPedido(String fechaEstimadaPedido) {
        this.fechaEstimadaPedido = fechaEstimadaPedido;
    }

    public String getDescripcionPedido() {
        return descripcionPedido;
    }

    public void setDescripcionPedido(String descripcionPedido) {
        this.descripcionPedido = descripcionPedido;
    }

    public double getImportePedido() {
        return importePedido;
    }

    public void setImportePedido(double importePedido) {
        this.importePedido = importePedido;
    }

    public int getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(int estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public int getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
    }

    public String getNombreTienda() {
        return nombreTienda;
    }

    public void setNombreTienda(String nombreTienda) {
        this.nombreTienda = nombreTienda;
    }
}