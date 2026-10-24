package org.example;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class Impresor {

    // Espacios entre subarboles vecinos
    private static final int SEPARACION = 3;
    // Filas por nivel: 1 de etiqueta + 3 de conectores
    private static final int FILAS_POR_NIVEL = 4;

    private final ArbolBMas arbol;
    private final Map<Nodo, Integer> anchos = new IdentityHashMap<>();

    public Impresor(ArbolBMas arbol) {
        this.arbol = arbol;
    }

    public void imprimir() {
        System.out.println("\n=== VISUALIZACION DEL ARBOL B+ (orden " + arbol.orden + ") ===\n");

        if (arbol.raiz == null) {
            System.out.println("El arbol esta vacio.");
            return;
        }

        anchos.clear();
        int anchoTotal = calcularAncho(arbol.raiz);
        int niveles = profundidad(arbol.raiz);

        char[][] lienzo = new char[(niveles - 1) * FILAS_POR_NIVEL + 1][anchoTotal];
        for (char[] fila : lienzo) {
            java.util.Arrays.fill(fila, ' ');
        }

        colocar(arbol.raiz, 0, 0, lienzo);

        for (char[] fila : lienzo) {
            System.out.println(new String(fila).stripTrailing());
        }

        System.out.println();
        imprimirHojasEnlazadas();
    }

    // ---------- Calculo de anchos ----------

    private String etiqueta(Nodo nodo) {
        String s = nodo.toString();
        return s.startsWith("[") ? s : "[" + s + "]";
    }

    private boolean sinHijos(Nodo nodo) {
        return nodo.esHoja() || nodo.hijos == null || nodo.hijos.isEmpty();
    }

    /** Ancho que necesita el subarbol completo de este nodo. */
    private int calcularAncho(Nodo nodo) {
        int propio = etiqueta(nodo).length();
        int ancho = propio;

        if (!sinHijos(nodo)) {
            int sumaHijos = 0;
            for (Nodo hijo : nodo.hijos) {
                sumaHijos += calcularAncho(hijo);
            }
            sumaHijos += SEPARACION * (nodo.hijos.size() - 1);
            ancho = Math.max(propio, sumaHijos);
        }

        anchos.put(nodo, ancho);
        return ancho;
    }

    private int profundidad(Nodo nodo) {
        int p = 1;
        while (!sinHijos(nodo)) {
            nodo = nodo.hijos.get(0);
            p++;
        }
        return p;
    }

    // ---------- Dibujo ----------

    /**
     * Dibuja el nodo dentro del area [inicio, inicio + ancho del subarbol)
     * y devuelve la columna central de su etiqueta.
     */
    private int colocar(Nodo nodo, int inicio, int nivel, char[][] g) {
        String texto = etiqueta(nodo);
        int w = texto.length();
        int areaW = anchos.get(nodo);
        int fila = nivel * FILAS_POR_NIVEL;

        if (sinHijos(nodo)) {
            int izq = inicio + (areaW - w) / 2;
            escribir(g, fila, izq, texto);
            return izq + w / 2;
        }

        // Los hijos se reparten de izquierda a derecha, centrados en el area
        int totalHijos = SEPARACION * (nodo.hijos.size() - 1);
        for (Nodo h : nodo.hijos) {
            totalHijos += anchos.get(h);
        }

        int x = inicio + (areaW - totalHijos) / 2;
        List<Integer> centros = new ArrayList<>();
        for (Nodo hijo : nodo.hijos) {
            centros.add(colocar(hijo, x, nivel + 1, g));
            x += anchos.get(hijo) + SEPARACION;
        }

        int primero = centros.get(0);
        int ultimo = centros.get(centros.size() - 1);

        // El padre queda centrado sobre sus hijos (sin salirse de su area)
        int izq = (primero + ultimo) / 2 - w / 2;
        izq = Math.max(inicio, Math.min(izq, inicio + areaW - w));
        escribir(g, fila, izq, texto);
        int centroPadre = izq + w / 2;

        // Conectores
        g[fila + 1][centroPadre] = '|';

        int desde = Math.min(centroPadre, primero);
        int hasta = Math.max(centroPadre, ultimo);
        for (int c = desde; c <= hasta; c++) {
            g[fila + 2][c] = '-';
        }
        g[fila + 2][centroPadre] = '+';
        for (int c : centros) {
            g[fila + 2][c] = '+';
            g[fila + 3][c] = '|';
        }

        return centroPadre;
    }

    private void escribir(char[][] g, int fila, int col, String texto) {
        for (int i = 0; i < texto.length(); i++) {
            g[fila][col + i] = texto.charAt(i);
        }
    }

    // ---------- Hojas enlazadas ----------

    private void imprimirHojasEnlazadas() {
        System.out.println("Hojas enlazadas:");
        Nodo hoja = arbol.raiz;

        while (hoja != null && !sinHijos(hoja)) {
            hoja = hoja.hijos.get(0);
        }

        while (hoja != null) {
            System.out.print(etiqueta(hoja));
            if (hoja.siguiente != null) {
                System.out.print(" -> ");
            }
            hoja = hoja.siguiente;
        }
        System.out.println(" -> null");
    }
}