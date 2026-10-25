package org.example;

public class Eliminacion {


    static final String CASO_1  = "CASO 1 - ELIMINACION SIMPLE";
    static final String CASO_2A = "CASO 2a - PRESTAMO";
    static final String CASO_2B = "CASO 2b - MERGE";
    static final String CASO_3  = "CASO 3 - PROPAGACION";

    private static final boolean REPARAR_PADRES = true;

    private final ArbolBMas arbol;
    private final Busqueda busqueda;

    String ultimoCaso = null;
    private int rangoCaso = 0;

    public Eliminacion(ArbolBMas arbol, Busqueda busqueda) {
        this.arbol = arbol;
        this.busqueda = busqueda;
    }

    // =========================================================
    // ELIMINAR UNA CLAVE
    // CASO 1: ELIMINACION SIMPLE
    // =========================================================

    public boolean eliminar(String nombre) {
        ultimoCaso = null;
        rangoCaso = 0;

        if (arbol.raiz == null) {
            Trazabilidad.log("ELIMINAR", "El arbol esta vacio, no hay nada que eliminar.");
            return false;
        }

        if (REPARAR_PADRES) repararPadres(arbol.raiz, null);

        Nodo hoja = busqueda.encontrarHoja(nombre);
        int pos = (hoja == null) ? -1 : posicionEnHoja(hoja, nombre);

        if (pos < 0) {
            Trazabilidad.log("NO EXISTE", "'" + nombre + "' no esta en el arbol; no se elimina nada.");
            return false;
        }

        String antes = fmt(hoja);
        String clave = hoja.claves.remove(pos);

        Trazabilidad.log("ELIMINAR", "Se quita '" + clave + "' de la hoja " + antes + " -> queda " + fmt(hoja));

        if (hoja == arbol.raiz) {
            marcarCaso(CASO_1);

            if (hoja.claves.isEmpty()) {
                arbol.raiz = null;
                Trazabilidad.log(CASO_1, "La raiz (que era hoja) quedo vacia: el arbol queda vacio.");
            } else {
                Trazabilidad.log(CASO_1, "Eliminacion simple en la raiz-hoja; la raiz no tiene minimo de ocupacion.");
            }

            resultado();
            validarArbol();
            return true;
        }

        if (pos == 0 && !hoja.claves.isEmpty()) {
            actualizarSeparador(hoja, clave);
        }

        if (hoja.claves.size() >= arbol.minClavesHoja) {
            marcarCaso(CASO_1);

            Trazabilidad.log(CASO_1, "Eliminacion simple: la hoja queda con " + hoja.claves.size()
                    + " clave(s) (minimo " + arbol.minClavesHoja + "). No hay underflow, no se requiere prestamo ni fusion.");

            resultado();
            validarArbol();
            return true;
        }

        Trazabilidad.log("UNDERFLOW", "La hoja " + fmt(hoja) + " tiene " + hoja.claves.size()
                + " clave(s) y el minimo es " + arbol.minClavesHoja + ".");

        resolverUnderflow(hoja, false);
        resultado();

        validarArbol();
        return true;
    }

    private void validarArbol() {
        if (!Validador.validarPropiedades(arbol)) {
            Trazabilidad.log("ERROR", "El árbol no cumple sus propiedades después de eliminar.");
            throw new IllegalStateException("El árbol B+ quedó inválido después de eliminar.");
        }
    }

    // =========================================================
    // RESOLVER UNDERFLOW
    // Decide entre prestamo, fusion y propagacion.
    // =========================================================

    private void resolverUnderflow(Nodo n, boolean propagado) {

        if (n == arbol.raiz) {
            if (!n.esHoja && n.claves.isEmpty()) {
                Nodo nueva = n.hijos.get(0);
                nueva.padre = null;
                arbol.raiz = nueva;

                marcarCaso(CASO_3);

                Trazabilidad.log(CASO_3, "La raiz quedo sin claves: su unico hijo " + fmt(nueva)
                        + " es la nueva raiz (la altura del arbol disminuye en 1).");
            }
            return;
        }

        int min = n.esHoja ? arbol.minClavesHoja : arbol.minClavesInterno;

        if (n.claves.size() >= min) return;

        Nodo p = n.padre;
        int idx = indiceEnPadre(n);

        Nodo izq = (idx > 0) ? p.hijos.get(idx - 1) : null;
        Nodo der = (idx < p.hijos.size() - 1) ? p.hijos.get(idx + 1) : null;

        String tipo = n.esHoja ? "hoja" : "nodo interno";

        // -----------------------------------------------------
        // CASO 2a: PRESTAMO
        // -----------------------------------------------------

        if (izq != null && izq.claves.size() > min) {
            prestarDeIzquierdo(n, izq, p, idx, propagado);
            return;
        }

        if (der != null && der.claves.size() > min) {
            prestarDeDerecho(n, der, p, idx, propagado);
            return;
        }

        // -----------------------------------------------------
        // CASO 2b: FUSION
        // Ningun hermano tiene claves disponibles para prestar.
        // -----------------------------------------------------

        Trazabilidad.log("UNDERFLOW", "Ningun hermano de " + tipo + " " + fmt(n)
                + " puede prestar (tienen el minimo): se aplica fusion.");

        if (izq != null) {
            fusionar(izq, n, p, idx - 1);
        } else {
            fusionar(n, der, p, idx);
        }

        boolean padreEsRaiz = (p == arbol.raiz);

        boolean padreEnProblema = padreEsRaiz ? p.claves.isEmpty()
                                              : p.claves.size() < arbol.minClavesInterno;

        if (propagado || padreEnProblema) {
            marcarCaso(CASO_3);

            Trazabilidad.log(CASO_3, "La fusion deja al padre " + fmt(p) + " "
                    + (padreEsRaiz ? "sin claves" : "en underflow (minimo " + arbol.minClavesInterno + ")")
                    + ": el problema se propaga hacia arriba.");
        } else {
            marcarCaso(CASO_2B);

            Trazabilidad.log(CASO_2B, "Fusion resuelta: el padre " + fmt(p) + " queda con "
                    + p.claves.size() + " clave(s), cumple el minimo. Fin.");
        }

        if (padreEnProblema) {
            resolverUnderflow(p, true);
        }
    }

    // =========================================================
    // CASO 2a: PRESTAMO DESDE EL HERMANO IZQUIERDO
    // =========================================================

    private void prestarDeIzquierdo(Nodo n, Nodo izq, Nodo p, int idx, boolean propagado) {
        String tag = propagado ? CASO_3 : CASO_2A;
        marcarCaso(tag);

        if (n.esHoja) {

            String prestada = izq.claves.remove(izq.claves.size() - 1);
            n.claves.add(0, prestada);

            String viejo = p.claves.get(idx - 1);
            p.claves.set(idx - 1, n.claves.get(0));

            Trazabilidad.log(tag, "Prestamo entre hojas desde el hermano IZQUIERDO: pasa '" + prestada
                    + "'. Hojas: " + fmt(izq) + " y " + fmt(n)
                    + ". Separador del padre '" + viejo + "' -> '" + p.claves.get(idx - 1) + "'.");

        } else {

            String bajaDelPadre = p.claves.get(idx - 1);

            Nodo hijoMovido = izq.hijos.remove(izq.hijos.size() - 1);
            String subeDelHermano = izq.claves.remove(izq.claves.size() - 1);

            n.claves.add(0, bajaDelPadre);
            n.hijos.add(0, hijoMovido);
            hijoMovido.padre = n;

            p.claves.set(idx - 1, subeDelHermano);

            Trazabilidad.log(tag, "Prestamo interno desde el hermano IZQUIERDO: baja '" + bajaDelPadre
                    + "' del padre, sube '" + subeDelHermano + "' del hermano y se traslada un puntero a hijo. "
                    + "Nodos: " + fmt(izq) + " y " + fmt(n) + ".");
        }
    }

    // =========================================================
    // CASO 2a: PRESTAMO DESDE EL HERMANO DERECHO
    // =========================================================

    private void prestarDeDerecho(Nodo n, Nodo der, Nodo p, int idx, boolean propagado) {
        String tag = propagado ? CASO_3 : CASO_2A;
        marcarCaso(tag);

        if (n.esHoja) {

            String prestada = der.claves.remove(0);
            n.claves.add(prestada);

            String viejo = p.claves.get(idx);
            p.claves.set(idx, der.claves.get(0));

            Trazabilidad.log(tag, "Prestamo entre hojas desde el hermano DERECHO: pasa '" + prestada
                    + "'. Hojas: " + fmt(n) + " y " + fmt(der)
                    + ". Separador del padre '" + viejo + "' -> '" + p.claves.get(idx) + "'.");

        } else {

            String bajaDelPadre = p.claves.get(idx);

            Nodo hijoMovido = der.hijos.remove(0);
            String subeDelHermano = der.claves.remove(0);

            n.claves.add(bajaDelPadre);
            n.hijos.add(hijoMovido);
            hijoMovido.padre = n;

            p.claves.set(idx, subeDelHermano);

            Trazabilidad.log(tag, "Prestamo interno desde el hermano DERECHO: baja '" + bajaDelPadre
                    + "' del padre, sube '" + subeDelHermano + "' del hermano y se traslada un puntero a hijo. "
                    + "Nodos: " + fmt(n) + " y " + fmt(der) + ".");
        }
    }

    // =========================================================
    // CASO 2b: FUSION DE NODOS
    // =========================================================

    private void fusionar(Nodo izq, Nodo der, Nodo p, int sep) {
        String separador = p.claves.get(sep);
        String antesIzq = fmt(izq), antesDer = fmt(der);

        if (izq.esHoja) {

            izq.claves.addAll(der.claves);
            izq.siguiente = der.siguiente;

            Trazabilidad.log("FUSION", "Hojas " + antesIzq + " + " + antesDer + " = " + fmt(izq)
                    + ". Se elimina el separador '" + separador + "' del padre (no se copia como dato).");

        } else {

            izq.claves.add(separador);
            izq.claves.addAll(der.claves);

            for (Nodo h : der.hijos) {
                h.padre = izq;
                izq.hijos.add(h);
            }

            Trazabilidad.log("FUSION", "Nodos internos " + antesIzq + " + '" + separador + "' (baja del padre) + "
                    + antesDer + " = " + fmt(izq) + ".");
        }

        p.claves.remove(sep);
        p.hijos.remove(sep + 1);

        der.padre = null;
        der.siguiente = null;
        der.claves.clear();
        der.hijos.clear();
    }

    // =========================================================
    // METODOS AUXILIARES
    // =========================================================

    private void actualizarSeparador(Nodo hoja, String viejaClave) {
        String nueva = hoja.claves.get(0);
        Nodo actual = hoja;

        while (actual.padre != null) {
            Nodo p = actual.padre;

            for (int i = 0; i < p.claves.size(); i++) {
                if (cmp(p.claves.get(i), viejaClave) == 0) {
                    p.claves.set(i, nueva);

                    Trazabilidad.log("SEPARADOR", "La menor clave de la hoja cambio: separador '" + viejaClave
                            + "' -> '" + nueva + "' en el ancestro " + fmt(p) + ".");

                    return;
                }
            }

            actual = p;
        }
    }

    private int posicionEnHoja(Nodo hoja, String nombre) {
        for (int i = 0; i < hoja.claves.size(); i++) {
            if (cmp(hoja.claves.get(i), nombre) == 0) return i;
        }

        return -1;
    }

    private int indiceEnPadre(Nodo n) {
        Nodo p = n.padre;

        for (int i = 0; i < p.hijos.size(); i++) {
            if (p.hijos.get(i) == n) return i;
        }

        throw new IllegalStateException("El nodo " + fmt(n) + " no aparece entre los hijos de su padre.");
    }

    private void repararPadres(Nodo n, Nodo padre) {
        n.padre = padre;

        if (!n.esHoja) {
            for (Nodo h : n.hijos) repararPadres(h, n);
        }
    }

    private void marcarCaso(String caso) {
        int r = rango(caso);

        if (r > rangoCaso) {
            rangoCaso = r;
            ultimoCaso = caso;
        }
    }

    private static int rango(String caso) {
        switch (caso) {
            case CASO_1:  return 1;
            case CASO_2A: return 2;
            case CASO_2B: return 3;
            default:      return 4;
        }
    }

    private void resultado() {
        Trazabilidad.log("RESULTADO", "Escenario resuelto: " + ultimoCaso);
    }

    private static int cmp(String a, String b) {
        return a.compareToIgnoreCase(b);
    }

    private static String fmt(Nodo n) {
        return "[" + String.join("|", n.claves) + "]";
    }
}
