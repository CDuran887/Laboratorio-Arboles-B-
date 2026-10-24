package org.example;
public class ArbolBMas {
    final int orden;
    Nodo raiz;                  // null = arbol vacio
    final int maxClaves;        // orden - 1
    final int minClavesInterno; // ceil(orden/2) - 1
    final int minClavesHoja;    // ceil((orden-1)/2)
    private final Busqueda busqueda;
    private final Insercion insercion;
    private final Eliminacion eliminacion;
    private final Impresor impresor;

    public ArbolBMas(int orden) {
        this.orden = orden;
        this.maxClaves = orden - 1;
        this.minClavesInterno = (int) Math.ceil(orden / 2.0) - 1;
        this.minClavesHoja = (int) Math.ceil((orden - 1) / 2.0);
        this.raiz = null;
        this.busqueda    = new Busqueda(this);
        this.insercion   = new Insercion(this, busqueda);
        this.eliminacion = new Eliminacion(this, busqueda);
        this.impresor    = new Impresor(this);
    }

    public boolean insertar(String nombre) { return insercion.insertar(nombre); }
    public boolean eliminar(String nombre) { return eliminacion.eliminar(nombre); }
    public boolean buscar(String nombre)   { return busqueda.buscar(nombre); }
    public void imprimir()            { impresor.imprimir(); }
}