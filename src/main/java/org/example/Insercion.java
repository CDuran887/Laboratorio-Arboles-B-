package org.example;
public class Insercion {
    private final ArbolBMas arbol;
    private final Busqueda busqueda;

    public Insercion(ArbolBMas arbol, Busqueda busqueda) {
        this.arbol = arbol;
        this.busqueda = busqueda;
    }

    public boolean insertar(String nombre) {
        // TODO: duplicado, arbol vacio, bajar a hoja, insertar ordenado, split
        return false;
    }

    // TODO: insertarEnHoja(Nodo hoja, String nombre)
    // TODO: dividirHoja(Nodo hoja)        (la clave sube COPIADA)
    // TODO: dividirInterno(Nodo n)        (la clave del medio sube, NO se copia)
    // TODO: insertarEnPadre(Nodo izq, String clave, Nodo der)
}