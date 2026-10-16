package org.example;

import org.iesalandalus.programacion.utilidades.Entrada;

public class EntradaDatos {
    static final int NUMERO_MAXIMO_DEPARTAMENTO = 32767;

    // pide por teclado numero de departamento hasta que sea válido
    public static short pedirNumeroDepartamento() {
        System.out.print("Introduce el número de departamento (1-" + NUMERO_MAXIMO_DEPARTAMENTO + "): ");
        int num = Entrada.entero();

        while (num > NUMERO_MAXIMO_DEPARTAMENTO || num < 1) {
            System.out.print("Número fuera de rango (1-" + NUMERO_MAXIMO_DEPARTAMENTO + "). Inténtalo de nuevo: ");
            num = Entrada.entero();
        }

        return (short) num;
    }

    // pide por teclado un string hasta que sea válido
    public static String pedirString(String nombreCampo){
        // nombreCampo debe ser nombre o localidad
        System.out.print("Introduce " + nombreCampo + " (máx. " + GestionDepartamentos.LONGITUD_TEXTO + " caracteres): ");
        String c;

        boolean valido;
        do {
            c = Entrada.cadena().trim();
            valido = true;
            if (c.length() > GestionDepartamentos.LONGITUD_TEXTO){
                System.out.print("Es demasiado largo (máx. " + GestionDepartamentos.LONGITUD_TEXTO + " caracteres). Inténtalo de nuevo: ");
                valido = false;
            } else if (c.isEmpty()) {
                System.out.print("Está vacía. Inténtalo de nuevo: ");
            }
        } while (!valido);

        return c;
    }

    // pide por teclado numero de empleados hasta que sea válido
    public static short pedirNumeroEmpleados() {
        System.out.print("Introduce el número de empleados (0-" + NUMERO_MAXIMO_DEPARTAMENTO + "): ");
        int num = Entrada.entero();

        while (num > NUMERO_MAXIMO_DEPARTAMENTO || num < 0) {
            System.out.print("Número fuera de rango (0-" + NUMERO_MAXIMO_DEPARTAMENTO + "). Inténtalo de nuevo: ");
            num = Entrada.entero();
        }

        return (short) num;
    }

    // pide confirmación al usuario, devuelve true solo si responde S
    public static boolean pedirConfirmacionEliminar(short dep) {
        String resp;
        do {
            System.out.print("Quieres eliminar el departamento " + dep + " (s/n): ");
            resp = Entrada.cadena().trim().toLowerCase();
        } while (!resp.equals("s") && !resp.equals("n"));

        return resp.equals("s");
    }
}