package org.example;
 
import java.util.LinkedList;
import java.util.Queue;
 
public class Impresor {
    private final ArbolBMas arbol;
 
    public Impresor(ArbolBMas arbol) { this.arbol = arbol; }
 
    private static class NodoNivel {
        Nodo nodo;
        int nivel;
 
        NodoNivel(Nodo nodo, int nivel) {
            this.nodo = nodo;
            this.nivel = nivel;
        }
    }
 
    public void imprimir() {
        System.out.println("\n=== VISUALIZACION DEL ARBOL B+ (orden " + arbol.orden + ") ===");
 
        if (arbol.raiz == null) {
            System.out.println("El arbol esta vacio.");
            return;
        }
 
        Queue<NodoNivel> cola = new LinkedList<>();
        cola.add(new NodoNivel(arbol.raiz, 0));
 
        int nivelActual = -1;
 
        while (!cola.isEmpty()) {
            NodoNivel actual = cola.poll();
 
            if (actual.nivel != nivelActual) {
                nivelActual = actual.nivel;
                System.out.print("Nivel " + nivelActual + etiqueta(actual) + ": ");
            }
 
            System.out.print(actual.nodo + " ");
 
            if (!actual.nodo.esHoja()) {
                for (Nodo hijo : actual.nodo.hijos) {
                    cola.add(new NodoNivel(hijo, actual.nivel + 1));
                }
            }
 
            if (cola.isEmpty() || cola.peek().nivel != nivelActual) {
                System.out.println();
            }
        }
 
        System.out.println();
        System.out.print("Hojas enlazadas: ");
        Nodo hoja = arbol.raiz;
        while (!hoja.esHoja()) {
            hoja = hoja.hijos.get(0); 
        }
        while (hoja != null) {
            System.out.print(hoja + " -> ");
            hoja = hoja.siguiente;
        }
        System.out.println("null");
    }
 
    private String etiqueta(NodoNivel n) {
        boolean raiz = n.nodo == arbol.raiz;
        boolean hoja = n.nodo.esHoja();
        if (raiz && hoja) return " (raiz y hoja)";
        if (raiz) return " (raiz)";
        if (hoja) return " (hojas)";
        return "";
    }
}
 