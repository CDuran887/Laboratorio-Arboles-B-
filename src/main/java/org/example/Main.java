package org.example;

import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int orden = pedirOrden(sc);
        ArbolBMas arbol = new ArbolBMas(orden);   // inicia vacio
        System.out.println("Arbol B+ creado (orden " + orden + ").");

        int op;
        do {
            System.out.println("\n1) Insertar  2) Buscar  3) Eliminar  4) Imprimir  5) Salir");
            System.out.print("Opcion: ");
            op = leerEntero(sc);
            switch (op) {
                case 1: { String n = pedirNombre(sc); if (n != null) arbol.insertar(n); break; }
                case 2: { String n = pedirNombre(sc); if (n != null) arbol.buscar(n); break; }
                case 3: { String n = pedirNombre(sc); if (n != null) arbol.eliminar(n); break; }
                case 4: arbol.imprimir(); break;
                case 5: System.out.println("Hasta luego."); break;
                default: System.out.println("Opcion invalida.");
            }
        } while (op != 5);
    }

    static int pedirOrden(Scanner sc) {
        while (true) {
            System.out.print("Orden del arbol (entre 4 y 9): ");
            int o = leerEntero(sc);
            if (o >= 4 && o <= 9) return o;
            System.out.println("Orden invalido. Debe estar entre 4 y 9.");
        }
    }

    static String pedirNombre(Scanner sc) {
        System.out.print("Nombre: ");
        String n = sc.nextLine().trim();
        if (n.isEmpty()) { System.out.println("El nombre no puede estar vacio."); return null; }
        return n;
    }

    static int leerEntero(Scanner sc) {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }
}