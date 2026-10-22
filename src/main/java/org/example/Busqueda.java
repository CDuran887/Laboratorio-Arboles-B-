package org.example;
public class Busqueda {
    private final ArbolBMas arbol;

    public Busqueda(ArbolBMas arbol) { this.arbol = arbol; }

    public boolean buscar(String nombre) {
        Nodo hoja = encontrarHoja(nombre);
        if (hoja == null) {
            Trazabilidad.log("BUSQUEDA", "El arbol esta vacio");
            return false;
        }
        for (String clave : hoja.claves) {
            if (clave.compareToIgnoreCase(nombre) == 0) {
                Trazabilidad.log("BUSQUEDA", "'" + nombre + "' existe en la hoja " + hoja);
                return true;
            }
        }
        Trazabilidad.log("BUSQUEDA", "'" + nombre + "' NO existe en el arbol");
        return false;

    }

    public Nodo encontrarHoja(String nombre) {
        Nodo nodoActual = arbol.raiz;
        if (nodoActual == null) {
            return null; // El árbol está vacío
        }
        while (!nodoActual.esHoja()) {
            int i = 0;
            while (i < nodoActual.claves.size() &&  nombre.compareToIgnoreCase(nodoActual.claves.get(i)) >= 0){
                i++;
            }
            nodoActual = nodoActual.hijos.get(i);
        }
        return nodoActual;
    }
}