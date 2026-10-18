package org.example;
public class Eliminacion {
    private final ArbolBMas arbol;
    private final Busqueda busqueda;

    public Eliminacion(ArbolBMas arbol, Busqueda busqueda) {
        this.arbol = arbol;
        this.busqueda = busqueda;
    }

    public boolean eliminar(String nombre) {
        // TODO: validar que exista, localizar hoja, borrar (Caso 1/2a/2b/3)
        return false;
    }

    // TODO: resolverUnderflow(Nodo n)
    // TODO: prestarDeIzquierdo / prestarDeDerecho   (Caso 2a)
    // TODO: fusionar(Nodo izq, Nodo der, ...)       (Caso 2b)
    // TODO: propagacion hacia arriba / bajar altura (Caso 3)
}