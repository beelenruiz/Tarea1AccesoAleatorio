package org.example;

import org.iesalandalus.programacion.utilidades.Entrada;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Main {
    static void main(String[] args) {
        File f = new File("AleatorioDepart.dat");
        RandomAccessFile file = null;

        try {
            // rw abre el archivo y si no existe lo crea
            file = new RandomAccessFile(f, "rw");

            // diagnostico al abrir el fichero
            boolean diagnostico = GestionDepartamentos.integridad(file, false);

            // mostramos el menu
            int opcion;
            do {
                GestionDepartamentos.menu(diagnostico);
                opcion = Entrada.entero();

                // se repite la lectura hasta que la opción sea válida
                while (!GestionDepartamentos.esOpcionValida(opcion, diagnostico)) {
                    System.out.print("Opción no válida. Inténtalo de nuevo: ");
                    opcion = Entrada.entero();
                }

                switch (opcion) {
                    case 1 -> {
                        short dep = EntradaDatos.pedirNumeroDepartamento();
                        String nombre = EntradaDatos.pedirString("el nombre");
                        String localidad = EntradaDatos.pedirString("la localidad");
                        short empleados = EntradaDatos.pedirNumeroEmpleados();
                        GestionDepartamentos.insertaDepartamento(dep, nombre, localidad, empleados, file);
                    }
                    case 2 -> {
                        short dep = EntradaDatos.pedirNumeroDepartamento();
                        GestionDepartamentos.consultaDepartamento(dep, file);
                    }
                    case 3 -> {
                        short dep = EntradaDatos.pedirNumeroDepartamento();
                        short empleados = EntradaDatos.pedirNumeroEmpleados();
                        GestionDepartamentos.actualizaNumeroEmpleados(dep, empleados, file);
                    }
                    case 4 -> {
                        short dep = EntradaDatos.pedirNumeroDepartamento();
                        String nombre = EntradaDatos.pedirString("el nombre");
                        String localidad = EntradaDatos.pedirString("la localidad");
                        GestionDepartamentos.actualizaNombreLocalidad(dep, nombre, localidad, file);
                    }
                    case 5 -> {
                        short dep = EntradaDatos.pedirNumeroDepartamento();
                        if (EntradaDatos.pedirConfirmacionEliminar(dep)) {
                            GestionDepartamentos.eliminaDepartamento(dep, file);
                        } else System.out.println("Operación cancelada");
                    }
                    case 6 -> GestionDepartamentos.listadoDepartamentos(file);

                    case 7 -> GestionDepartamentos.inspeccionFisica(file);
                    case 8 -> GestionDepartamentos.estadisticas(file);
                    case 9 -> GestionDepartamentos.integridad(file, diagnostico);
                    case 0 -> System.out.println("Hasta pronto.");
                }
            } while (opcion != 0);

            file.close();

        } catch (IOException e) {
            System.out.println("Error al acceder al fichero " + f.getName() + ".");

        } finally {
            // lo he buscado para segurarme de que el fichero se cierra aunque haya un error
            try {
                file.close();
            } catch (IOException e) {
                System.out.println("No se ha podido cerrar el fichero.");
            }
        }
    }
}