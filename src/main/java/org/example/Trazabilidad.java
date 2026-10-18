package org.example;
public class Trazabilidad {
    public static void log(String tag, String mensaje) {
        System.out.println("  [" + tag + "] " + mensaje);    }
}
// Traza.log("SPLIT", "Hoja dividida, sube 'Luis'"); 
// Traza.log("CASO 2a", "Prestamo del hermano izquierdo");