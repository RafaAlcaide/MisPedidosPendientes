package es.studium.mispedidospendientes;

public class Tienda {
    private int idTienda;
    private String nombreTienda;

    public Tienda() {}

    public Tienda(int idTienda, String nombreTienda) {
        this.idTienda = idTienda;
        this.nombreTienda = nombreTienda;
    }

    public int getIdTienda() {
        return idTienda;
    }

    public String getNombreTienda() {
        return nombreTienda;
    }
}
