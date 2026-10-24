package org.example;
 
public class Insercion {
 
    private final ArbolBMas arbol;
    private final Busqueda busqueda; 
    public Insercion(ArbolBMas arbol, Busqueda busqueda) {
        this.arbol = arbol;
        this.busqueda = busqueda;
    }
 
    private static class ResultadoSplit {
        String claveSubida; 
        Nodo izquierda;     
        Nodo derecha;    
 
        ResultadoSplit(String claveSubida, Nodo izquierda, Nodo derecha) {
            this.claveSubida = claveSubida;
            this.izquierda = izquierda;
            this.derecha = derecha;
        }
    }
 
    private static class EstadoInsercion {
        boolean insertado = false;
        boolean huboOverflow = false;
    }
 
    // ---------------------------------------------------------------
    // Operación principal
    // ---------------------------------------------------------------
 
    public boolean insertar(String nombre) {
 
        if (nombre == null || nombre.trim().isEmpty()) {
            Trazabilidad.log("ERROR", "Nombre vacío, no se inserta");
            return false;
        }
        nombre = nombre.trim();
 
        if (arbol.raiz == null) {
            arbol.raiz = new Nodo(true);
            arbol.raiz.claves.add(nombre);
            Trazabilidad.log("INSERCION", "Árbol vacío: se crea la raíz (hoja) " + texto(arbol.raiz)
                    + " con '" + nombre + "'");
            validarArbol();
            return true;
        }
 
        EstadoInsercion estado = new EstadoInsercion();
 
        ResultadoSplit split = insertarRecursivo(arbol.raiz, nombre, estado);
 
        if (!estado.insertado) {
            Trazabilidad.log("DUPLICADO", "'" + nombre + "' ya existe. Duplicado, no se inserta");
            return false;
        }
 
        if (split != null) {
            Nodo nuevaRaiz = new Nodo(false);
            nuevaRaiz.claves.add(split.claveSubida);
            nuevaRaiz.hijos.add(split.izquierda);
            nuevaRaiz.hijos.add(split.derecha);
            split.izquierda.padre = nuevaRaiz;
            split.derecha.padre = nuevaRaiz;
            arbol.raiz = nuevaRaiz;
            Trazabilidad.log("NUEVA RAIZ", "Nueva raíz creada (altura +1): " + texto(nuevaRaiz));
        }

        validarArbol();
        return true;
    }

    private void validarArbol() {
        if (!Validador.validarPropiedades(arbol)) {
            Trazabilidad.log("ERROR", "El árbol no cumple sus propiedades después de insertar.");
            throw new IllegalStateException("El árbol B+ quedó inválido después de insertar.");
        }
    }
 
    private ResultadoSplit insertarRecursivo(Nodo nodo, String nombre, EstadoInsercion estado) {
 
        if (nodo.esHoja) {
            int pos = posicionEnHoja(nodo, nombre);
 
            if (pos < nodo.claves.size() && nodo.claves.get(pos).compareToIgnoreCase(nombre) == 0) {
                estado.insertado = false;
                return null;
            }
 
            nodo.claves.add(pos, nombre);
            estado.insertado = true;
 
            if (nodo.claves.size() >= arbol.orden) {
                estado.huboOverflow = true;
                Trazabilidad.log("OVERFLOW", "Hoja con " + nodo.claves.size() + " claves: " + texto(nodo));
                return dividirHoja(nodo);
            }
 
            Trazabilidad.log("INSERCION", "Inserción simple en hoja " + texto(nodo) + " (sin overflow)");
            return null;
        }
 
        int indiceHijo = posicionHijo(nodo, nombre);
 
        ResultadoSplit splitHijo = insertarRecursivo(nodo.hijos.get(indiceHijo), nombre, estado);
 
        if (!estado.insertado)
            return null;
 
        if (splitHijo != null) {

            nodo.claves.add(indiceHijo, splitHijo.claveSubida);
            nodo.hijos.add(indiceHijo + 1, splitHijo.derecha);
            splitHijo.derecha.padre = nodo;
 
            if (nodo.claves.size() >= arbol.orden) {
                estado.huboOverflow = true;
                Trazabilidad.log("OVERFLOW", "Nodo interno con " + nodo.claves.size() + " claves: " + texto(nodo));
                return dividirInterno(nodo);
            }
        }
 
        return null;
    }
 
    // ---------------------------------------------------------------
    // Splits
    // ---------------------------------------------------------------
 
    private ResultadoSplit dividirHoja(Nodo hoja) {
        int total = hoja.claves.size();
        int mid = (total - 1) / 2; 
 
        String antes = texto(hoja);
 
        Nodo derecha = new Nodo(true);
        for (int i = mid + 1; i < total; i++) {
            derecha.claves.add(hoja.claves.get(i));
        }
        hoja.claves.subList(mid + 1, total).clear(); 
 
        derecha.siguiente = hoja.siguiente;
        hoja.siguiente = derecha;
        derecha.padre = hoja.padre;
 
        String copiada = derecha.claves.get(0);
 
        Trazabilidad.log("SPLIT", "SPLIT de hoja: " + antes + " -> " + texto(hoja) + " y " + texto(derecha)
                + ". Se COPIA '" + copiada + "' al padre");
 
        return new ResultadoSplit(copiada, hoja, derecha);
    }
 
    private ResultadoSplit dividirInterno(Nodo nodo) {
        int total = nodo.claves.size();
        int mid = (total - 1) / 2; 
 
        String antes = texto(nodo);
        String promovida = nodo.claves.get(mid);
 
        Nodo derecha = new Nodo(false);
 
        for (int i = mid + 1; i < total; i++) {
            derecha.claves.add(nodo.claves.get(i));
        }
 
        for (int i = mid + 1; i < nodo.hijos.size(); i++) {
            Nodo hijo = nodo.hijos.get(i);
            derecha.hijos.add(hijo);
            hijo.padre = derecha;
        }
 
        nodo.claves.subList(mid, total).clear();                   
        nodo.hijos.subList(mid + 1, nodo.hijos.size()).clear();   
        derecha.padre = nodo.padre;
 
        Trazabilidad.log("SPLIT", "SPLIT de nodo interno: " + antes + " -> " + texto(nodo) + " y " + texto(derecha)
                + ". Se PROMUEVE '" + promovida + "' (" + nodo.hijos.size() + " y "
                + derecha.hijos.size() + " hijos)");
 
        return new ResultadoSplit(promovida, nodo, derecha);
    }
 
    private int posicionEnHoja(Nodo hoja, String nombre) {
        int i = 0;
        while (i < hoja.claves.size() && nombre.compareToIgnoreCase(hoja.claves.get(i)) > 0) {
            i++;
        }
        return i;
    }
 

    private int posicionHijo(Nodo interno, String nombre) {
        int i = 0;
        while (i < interno.claves.size() && nombre.compareToIgnoreCase(interno.claves.get(i)) >= 0) {
            i++;
        }
        return i;
    }
 
    private String texto(Nodo nodo) {
        return "[" + String.join(" | ", nodo.claves) + "]";
    }
}
 