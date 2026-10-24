package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class Validador {

    public static boolean validarPropiedades(ArbolBMas arbol) {
        // Confirma que el arbol y sus limites de claves sean validos.
        if (arbol == null || arbol.orden < 3
                || arbol.maxClaves != arbol.orden - 1
                || arbol.minClavesInterno != (int) Math.ceil(arbol.orden / 2.0) - 1
                || arbol.minClavesHoja != (int) Math.ceil((arbol.orden - 1) / 2.0)) {
            return false;
        }

        // Una raiz nula representa un arbol vacio valido.
        if (arbol.raiz == null) {
            return true;
        }

        // La raiz no debe depender de otro nodo.
        if (arbol.raiz.padre != null) {
            return false;
        }

        // Guarda las hojas, los nodos visitados y el nivel comun de las hojas.
        List<Nodo> hojas = new ArrayList<>();
        Set<Nodo> visitados = Collections.newSetFromMap(new IdentityHashMap<>());
        int[] nivelHojas = {-1};

        // Valida recursivamente la estructura, ocupacion, claves y separadores.
        if (!validarNodo(arbol, arbol.raiz, null, true, 0, nivelHojas, visitados, hojas)) {
            return false;
        }

        // Comprueba que las hojas esten enlazadas en orden y que la ultima apunte a null.
        for (int i = 0; i < hojas.size(); i++) {
            Nodo siguienteEsperado = i + 1 < hojas.size() ? hojas.get(i + 1) : null;
            if (hojas.get(i).siguiente != siguienteEsperado) {
                return false;
            }
        }

        return true;
    }

    private static boolean validarNodo(ArbolBMas arbol, Nodo nodo, Nodo padreEsperado,
                                       boolean esRaiz, int nivel, int[] nivelHojas,
                                       Set<Nodo> visitados, List<Nodo> hojas) {
        // Evita nodos nulos,padres incorrectos y exceso de claves.
        if (nodo == null || !visitados.add(nodo) || nodo.padre != padreEsperado
                || nodo.claves == null || nodo.hijos == null
                || nodo.claves.size() > arbol.maxClaves) {
            return false;
        }

        // Las claves deben tener contenido y estar estrictamente ordenadas sin duplicados.
        for (int i = 0; i < nodo.claves.size(); i++) {
            String clave = nodo.claves.get(i);
            if (clave == null || clave.trim().isEmpty()
                    || (i > 0 && comparar(nodo.claves.get(i - 1), clave) >= 0)) {
                return false;
            }
        }

        if (nodo.esHoja) {
            int minimo = esRaiz ? 1 : arbol.minClavesHoja;
            // Las hojas no tienen hijos y deben respetar su minimo de ocupacion.
            if (!nodo.hijos.isEmpty() || nodo.claves.size() < minimo) {
                return false;
            }

            // Todas las hojas deben encontrarse a la misma profundidad.
            if (nivelHojas[0] == -1) {
                nivelHojas[0] = nivel;
            } else if (nivelHojas[0] != nivel) {
                return false;
            }

            hojas.add(nodo);
            return true;
        }

        int minimo = esRaiz ? 1 : arbol.minClavesInterno;
        // Los nodos internos respetan el minimo, no usan enlace de hoja y tienen un hijo extra.
        if (nodo.siguiente != null || nodo.claves.size() < minimo
                || nodo.hijos.size() != nodo.claves.size() + 1) {
            return false;
        }

        // Valida cada hijo y obtiene su rango de claves.
        List<Rango> rangos = new ArrayList<>(nodo.hijos.size());
        for (Nodo hijo : nodo.hijos) {
            if (!validarNodo(arbol, hijo, nodo, false, nivel + 1, nivelHojas, visitados, hojas)) {
                return false;
            }
            rangos.add(obtenerRango(hijo));
        }

        // Cada separador debe ser la menor clave del hijo derecho y mayor que el rango izquierdo.
        for (int i = 0; i < nodo.claves.size(); i++) {
            String separador = nodo.claves.get(i);
            Rango rangoIzquierdo = rangos.get(i);
            Rango rangoDerecho = rangos.get(i + 1);

            if (comparar(rangoIzquierdo.maximo, separador) >= 0
                    || comparar(rangoDerecho.minimo, separador) != 0) {
                return false;
            }
        }

        return true;
    }

    private static Rango obtenerRango(Nodo nodo) {
        // En una hoja, el rango va de la primera a la ultima clave.
        if (nodo.esHoja) {
            return new Rango(nodo.claves.get(0), nodo.claves.get(nodo.claves.size() - 1));
        }

        // En un nodo interno, el rango abarca desde el primer hasta el ultimo hijo.
        Rango izquierdo = obtenerRango(nodo.hijos.get(0));
        Rango derecho = obtenerRango(nodo.hijos.get(nodo.hijos.size() - 1));
        return new Rango(izquierdo.minimo, derecho.maximo);
    }

    // Usa el mismo criterio de comparacion que insercion y busqueda.
    private static int comparar(String izquierda, String derecha) {
        return izquierda.compareToIgnoreCase(derecha);
    }

    private static final class Rango {
        private final String minimo;
        private final String maximo;

        private Rango(String minimo, String maximo) {
            this.minimo = minimo;
            this.maximo = maximo;
        }
    }
}
