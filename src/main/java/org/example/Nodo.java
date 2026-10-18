package org.example;
import java.util.ArrayList;
import java.util.List;

public class Nodo {
    List<String> claves;
    List<Nodo> hijos;
    boolean esHoja;
    Nodo siguiente;   // NEXT: enlace a la siguiente hoja (solo hojas)
    Nodo padre;

    public Nodo(boolean esHoja) {
        this.esHoja = esHoja;
        this.claves = new ArrayList<>();
        this.hijos = new ArrayList<>();
    }
    public boolean esHoja() {
        return this.esHoja;
    }
    @Override
    public String toString() {   // [ Ana | Luis | Pedro ]
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < claves.size(); i++) {
            sb.append(claves.get(i));
            if (i < claves.size() - 1) sb.append(" | ");
        }
        sb.append("]");
        return sb.toString();
    }
}
