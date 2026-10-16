package org.example;

import java.io.IOException;
import java.io.RandomAccessFile;

public class GestionDepartamentos {
    static final long LONGITUD_REGISTRO = 64;

    static final int LONGITUD_TEXTO = 15;

    static final int BYTES_SHORT = 2;
    static final int BYTES_STRING = 30;

    static final String RELLENO_NUNCA_UTILIZADO = "---------------";
    static final String RELLENO_ELIMINADO = "***************";

    enum ESTADO {
        ACTIVO("Activo"),
        ELIMINADO("Eliminado"),
        NUNCA_UTILIZADO("Nunca utilizado"),
        ANOMALA("Anomalía detectada"),
        FUERA_FICHERO("Fuera de fichero");

        private final String descripcion;

        ESTADO(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    // ------------------------------------------------  AUXILIARES ------------------------------------------------
    private static long getPosicion(short dep){
        return (dep - 1) * LONGITUD_REGISTRO;
    }

    // metodo auxiliar para leer un string del fichero
    private static String readString(RandomAccessFile file) throws IOException{
        StringBuffer buffer = new StringBuffer(LONGITUD_TEXTO);
        for (int i = 0; i < LONGITUD_TEXTO; i++) {
            char c = file.readChar();
            buffer.append(c);
        }
        String cadena = buffer.toString();
        return cadena;
    }

    // metodo auxiliar para escribir un string y que tenga 15 caracteres
    private static String rellenarString(String cadena) {
        if (cadena.length() < LONGITUD_TEXTO) {
            StringBuffer buffer = new StringBuffer(LONGITUD_TEXTO);
            buffer.append(cadena);
            for (int i = cadena.length(); i < LONGITUD_TEXTO; i++) {
                buffer.append(' ');
            }
            return buffer.toString();
        }
        return cadena;
    }

    public static ESTADO getEstado(short dep, RandomAccessFile file) throws IOException {
        long posicion = getPosicion(dep);

        // si la posición final del registro es mayor a la longitud del fichero el registro esta fuera de rango
        if (posicion + LONGITUD_REGISTRO > file.length()) return ESTADO.FUERA_FICHERO;

        // me muevo y leo valores
        file.seek(posicion);
        short id = file.readShort();
        String nombre = readString(file);
        String localidad = readString(file);
        short empleados = file.readShort();

        // si el id es 0 el registro no ha sido utilizado
        if (id == 0 && nombre.equals(RELLENO_NUNCA_UTILIZADO) &&
                localidad.equals(RELLENO_NUNCA_UTILIZADO) &&
                empleados == 0) return ESTADO.NUNCA_UTILIZADO;

        // si el id es -1 el registro ha sido eliminado
        if (id == -1 && nombre.equals(RELLENO_ELIMINADO) &&
                localidad.equals(RELLENO_ELIMINADO) &&
                empleados == -1) return ESTADO.ELIMINADO;

        // registro anomalo si:
        // identificador incorrecto, empreados negativos o inicializacion o borrado incompleto
        if (id != dep || empleados < 0 ||
                nombre.equals(RELLENO_NUNCA_UTILIZADO) || localidad.equals(RELLENO_NUNCA_UTILIZADO) ||
                nombre.equals(RELLENO_ELIMINADO) || localidad.equals(RELLENO_ELIMINADO) ||
                nombre.trim().isEmpty() || localidad.trim().isEmpty()) return ESTADO.ANOMALA;

        return ESTADO.ACTIVO;
    }

    // ------------------------------------------------  METODOS TAREA ------------------------------------------------
    public static boolean existeDepartamento(short dep, RandomAccessFile file) throws IOException {
        ESTADO estado = getEstado(dep, file);

        // devuelvo true si el registro esta activo
        if (estado == ESTADO.ACTIVO) return true;

        return false;
    }

    public static void consultaDepartamento(short dep, RandomAccessFile file){
        try {
            ESTADO estado = getEstado(dep, file);

            if (estado == ESTADO.ACTIVO) {
                // lo muestro si existe

                //me muevo y leo los valores
                long posicion = getPosicion(dep);
                file.seek(posicion);
                short id =
                        file.readShort();
                String nombre = readString(file).trim();
                String localidad = readString(file).trim();
                short empleados = file.readShort();

                System.out.println("Departamento:" + id);
                System.out.println("Nombre:" + nombre);
                System.out.println("Localidad:" + localidad);
                System.out.println("Número de empleados:" + empleados);
            } else if (estado == ESTADO.ANOMALA) {
                // hay una anomalía
                System.out.println("No se puede mostrar el departamento " + dep + ": su posición contiene un registro anómalo. El fichero no se ha modificado. Consulta la opción de integridad para ver el detalle.");

            } else if (estado == ESTADO.ELIMINADO || estado == ESTADO.NUNCA_UTILIZADO || estado == ESTADO.FUERA_FICHERO) {
                System.out.println("El departamento " + dep + " no existe.");
            }
        } catch (IOException e) {
            System.out.println("Se ha producido un error de E/S en el fichero o la posición indicada no es la correcta.");
        }
    }

    public static void actualizaNumeroEmpleados(short dep, short empleados, RandomAccessFile file){
        try {
            ESTADO estado = getEstado(dep, file);

            if (estado == ESTADO.ACTIVO) {
                // se puede modificar
                // me muevo y sobrescribo el numero de empleados
                long posicion = (getPosicion(dep)) + BYTES_SHORT + BYTES_STRING * 2;
                file.seek(posicion);
                file.writeShort(empleados);
                System.out.println("Departamento modificado.");

            } else if (estado == ESTADO.ANOMALA) {
                // hay una anomalía
                System.out.println("No se puede modificar el departamento " + dep + ": su posición contiene un registro anómalo. El fichero no se ha modificado. Consulta la opción de integridad para ver el detalle.");

            } else if (estado == ESTADO.ELIMINADO || estado == ESTADO.NUNCA_UTILIZADO || estado == ESTADO.FUERA_FICHERO) {
                System.out.println("El departamento " + dep + " no existe.");
            }

        } catch (IOException e) {
            System.out.println("Se ha producido un error de E/S en el fichero o la posición indicada no es la correcta.");
        }
    }

    public static void actualizaNombreLocalidad(short dep,String nombre, String localidad, RandomAccessFile file){
        try {
            ESTADO estado = getEstado(dep, file);

            if (estado == ESTADO.ACTIVO) {
                // se puede modificar
                // me muevo y sobrescribo valores
                long posicion = (getPosicion(dep)) + BYTES_SHORT;
                file.seek(posicion);
                // debemos comprobar que los strings tenga 15 caracteres
                file.writeChars(rellenarString(nombre));
                file.writeChars(rellenarString(localidad));
                System.out.println("Departamento modificado.");

            } else if (estado == ESTADO.ANOMALA) {
                // hay una anomalía
                System.out.println("No se puede modificar el departamento " + dep + ": su posición contiene un registro anómalo. El fichero no se ha modificado. Consulta la opción de integridad para ver el detalle.");

            } else if (estado == ESTADO.ELIMINADO || estado == ESTADO.NUNCA_UTILIZADO || estado == ESTADO.FUERA_FICHERO) {
                System.out.println("El departamento " + dep + " no existe.");
            }

        } catch (IOException e) {
            System.out.println("Se ha producido un error de E/S en el fichero o la posición indicada no es la correcta.");
        }
    }

    public static void insertaDepartamento(short dep, String nombre, String localidad, short empleados, RandomAccessFile file) {
        try {
            ESTADO estado = getEstado(dep, file);

            if (estado == ESTADO.FUERA_FICHERO || estado == ESTADO.ELIMINADO || estado == ESTADO.NUNCA_UTILIZADO){
                // se puede insertar el departamento
                long posicion = getPosicion(dep);

                for (long i = file.length(); i < posicion; i += LONGITUD_REGISTRO) {
                    file.seek(i);
                    file.writeShort(0);
                    file.writeChars(RELLENO_NUNCA_UTILIZADO);
                    file.writeChars(RELLENO_NUNCA_UTILIZADO);
                    file.writeShort(0);
                }

                file.seek(posicion);
                file.writeShort(dep);
                file.writeChars(rellenarString(nombre));
                file.writeChars(rellenarString(localidad));
                file.writeShort(empleados);
                System.out.println("Departamento insertado.");

            } else if (estado == ESTADO.ACTIVO) {
                // ya existe
                System.out.println("El departamento " + dep + " ya existe");

            } else if (estado == ESTADO.ANOMALA) {
                // hay una anomalía
                System.out.println("No se puede insertar el departamento " + dep + ": su posición contiene un registro anómalo. El fichero no se ha modificado. Consulta la opción de integridad para ver el detalle.");
            }
        } catch (IOException e) {
            System.out.println("Se ha producido un error de E/S en el fichero o la posición indicada no es la correcta.");
        }
    }

    public static void eliminaDepartamento(short dep, RandomAccessFile file) {
        try {
            ESTADO estado = getEstado(dep, file);

            if (estado == ESTADO.ACTIVO) {
                // lo elimino si existe
                long posicion = getPosicion(dep);

                file.seek(posicion);
                file.writeShort(-1);
                file.writeChars(RELLENO_ELIMINADO);
                file.writeChars(RELLENO_ELIMINADO);
                file.writeShort(-1);
                System.out.println("Departamento eliminado");

            } else if (estado == ESTADO.ANOMALA) {
                // hay una anomalía
                System.out.println("No se puede eliminar el departamento " + dep + ": su posición contiene un registro anómalo. El fichero no se ha modificado. Consulta la opción de integridad para ver el detalle.");

            } else if (estado == ESTADO.ELIMINADO) {
                System.out.println("El departamento " + dep + " ya estaba eliminado");

            } else if (estado == ESTADO.NUNCA_UTILIZADO || estado == ESTADO.FUERA_FICHERO) {
                System.out.println("El departamento " + dep + " no existe.");
            }

        } catch (IOException e) {
            System.out.println("Se ha producido un error de E/S en el fichero o la posición indicada no es la correcta.");
        }
    }

    public static void  listadoDepartamentos(RandomAccessFile file){
        try {
            long numPos = file.length() / LONGITUD_REGISTRO;
            int contador = 0;

            for (int i = 1; i <= numPos; i++){
                // solo mostramos registros activos
                if (getEstado((short) i, file) == ESTADO.ACTIVO) {
                    consultaDepartamento((short) i, file);
                    System.out.println();
                    contador++;
                }
            }

            if (contador == 0) {
                System.out.println("No hay departamentos activos.");
            } else {
                System.out.println("Número de departamentos listados: " + contador);
            }

        } catch (IOException e) {
            System.out.println("Se ha producido un error de E/S en el fichero o la posición indicada no es la correcta.");
        }
    }

    public static void inspeccionFisica(RandomAccessFile file){
        try {
            long numPos = file.length() / LONGITUD_REGISTRO;

            if (numPos == 0) {
                System.out.println("El fichero está vacío.");
                return;
            }

            // recorremos registros, pos es el id esperado
            for (int pos = 1; pos <= numPos; pos++){
                // calculamos la posición
                long posBytes = (pos -1) * LONGITUD_REGISTRO;
                file.seek(posBytes);

                System.out.println("Número de posición: " + pos);
                System.out.println("Desplazamiento (bytes): " + posBytes);
                System.out.println("Identificador esperado: " + pos);
                System.out.println("Identificador almacenado: " + file.readShort());
                System.out.println("Estado: " + getEstado((short) pos, file).getDescripcion());
                System.out.println("-----------------------------------");
            }

        } catch (IOException e) {
            System.out.println("Se ha producido un error de E/S en el fichero o la posición indicada no es la correcta.");
        }
    }

    public static void estadisticas(RandomAccessFile file){
        try {
            long numPos = file.length() / LONGITUD_REGISTRO;

            if (numPos == 0) System.out.println("El fichero está vacío.");

            int posActivos = 0;
            int posEliminados = 0;
            int posNuncaUtilizados = 0;
            int empleadosActivos = 0;

            // recorremos registros, pos es el id esperado
            for (int pos = 1; pos <= numPos; pos++){
                // calculamos la posición
                long posBytes = (pos -1) * LONGITUD_REGISTRO;
                file.seek(posBytes);

                // sumamos 1 al contador segun estado
                ESTADO estado = getEstado((short) pos, file);
                if (estado == ESTADO.ACTIVO) {
                    posActivos++;
                    file.seek(posBytes + BYTES_SHORT + BYTES_STRING * 2);
                    // suma de los empleados activos
                    empleadosActivos += file.readShort();
                }
                if (estado == ESTADO.ELIMINADO) posEliminados++;
                if (estado == ESTADO.NUNCA_UTILIZADO) posNuncaUtilizados++;
            }

            System.out.println("Longitud del fichero (bytes): " + file.length());
            System.out.println("Posiciones activas: " + posActivos);
            System.out.println("Posiciones eliminadas: " + posEliminados);
            System.out.println("Posiciones nunca utilizadas: " + posNuncaUtilizados);
            System.out.println("Suma de los empleados de los departamentos activos: " + empleadosActivos);
            if (posEliminados + posNuncaUtilizados == 0){
                System.out.println("Porcentaje de ocupación: No hay posiciones eliminadas ni nunca utilizadas");
            } else {
                System.out.printf("Porcentaje de ocupación: %.2f%%%n", ((double) posActivos / (posEliminados + posNuncaUtilizados) * 100.0));
            }

        } catch (IOException e) {
            System.out.println("Se ha producido un error de E/S en el fichero o la posición indicada no es la correcta.");
        }
    }

    public static boolean integridad(RandomAccessFile file, boolean diagnostico) {
        // boolean para saber si hay anomalias
        boolean anomalias = false;

        try {
            long numPos = file.length() / LONGITUD_REGISTRO;

            if (file.length() == 0) {
                System.out.println("El fichero está vacío.");
                return anomalias;
            }

            if (file.length() % LONGITUD_REGISTRO != 0) {
                anomalias = true;
                System.out.println("Anomalía detectada: la longitud del fichero (" + file.length()
                        + " bytes) no es múltiplo de " + LONGITUD_REGISTRO
                        + ". El registro incompleto empieza en el byte " + (numPos * LONGITUD_REGISTRO) + ".");
            }

            for (int pos = 1; pos <= numPos; pos++){
                // calculamos la posición
                long posBytes = (pos -1) * LONGITUD_REGISTRO;

                ESTADO estado = getEstado((short) pos, file);
                if (estado == ESTADO.ANOMALA) {
                    anomalias = true;

                    // leemos datos
                    file.seek(posBytes);
                    short id = file.readShort();
                    String nombre = readString(file);
                    String localidad = readString(file);
                    short empleados = file.readShort();

                    if (diagnostico) {
                        // detectamos anomalía
                        if (id < -1)
                            System.out.println("Anomalía detectada: Id " + id + " en la posición " + posBytes + " bytes.");
                        if (id > 0 && id != pos)
                            System.out.println("Anomalía detectada: Se esperaba id " + pos + " y se encuentra id " + id + " en la posición " + posBytes + " bytes.");
                        if (id > 0 && empleados < 0)
                            System.out.println("Anomalía detectada: Número de empleados negativo (" + empleados + ") en la posición " + (posBytes + BYTES_SHORT + BYTES_STRING * 2) + " bytes.");
                        if (id > 0 && nombre.trim().isEmpty())
                            System.out.println("Anomalía detectada: Nombre vacío en un registro activo en la posición " + (posBytes + BYTES_SHORT) + " bytes.");
                        if (id > 0 && (nombre.equals(RELLENO_ELIMINADO) || nombre.equals(RELLENO_NUNCA_UTILIZADO)))
                            System.out.println("Anomalía detectada: Nombre con signos de inicializacion o borrado en un registro activo en la posición " + (posBytes + BYTES_SHORT) + " bytes.");
                        if (id > 0 && localidad.trim().isEmpty())
                            System.out.println("Anomalía detectada: Localidad vacía en un registro activo en la posición " + (posBytes + BYTES_SHORT + BYTES_STRING) + " bytes.");
                        if (id > 0 && (localidad.equals(RELLENO_ELIMINADO) || localidad.equals(RELLENO_NUNCA_UTILIZADO)))
                            System.out.println("Anomalía detectada: Localidad con signos de inicializacion o borrado en un registro activo en la posición " + (posBytes + BYTES_SHORT + BYTES_STRING) + " bytes.");
                        if (id == 0 && (!nombre.equals(RELLENO_NUNCA_UTILIZADO) || !localidad.equals(RELLENO_NUNCA_UTILIZADO) || empleados != 0))
                            System.out.println("Anomalía detectada: No se respeta el formato en un registro nunca utilizado en la posición " + posBytes + " bytes.");
                        if (id == -1 && (!nombre.equals(RELLENO_ELIMINADO) || !localidad.equals(RELLENO_ELIMINADO) || empleados != -1))
                            System.out.println("Anomalía detectada: No se respeta el formato en un registro eliminado en la posición " + posBytes + " bytes.");
                    }
                }
            }

            if (!anomalias) System.out.println("No se han detectado anomalías.");

        } catch (IOException e) {
            System.out.println("Se ha producido un error de E/S en el fichero o la posición indicada no es la correcta.");
            anomalias = true;
            System.out.println("No se puede confirmar la integridad del fichero.");
        }
        return anomalias;
    }

    // ------------------------------------------------  METODOS MENU  ------------------------------------------------
    public static void menu(boolean diagnostico) {
        System.out.println();
        System.out.println("========== GESTIÓN DE DEPARTAMENTOS ==========");

        if (diagnostico) {
            System.out.println("MODO DIAGNÓSTICO: se han detectado anomalías en el fichero.");
            System.out.println("Solo están disponibles las operaciones de comprobación.");
        } else {
            System.out.println("1. Insertar departamento");
            System.out.println("2. Consultar departamento");
            System.out.println("3. Modificar número de empleados");
            System.out.println("4. Modificar nombre y localidad");
            System.out.println("5. Eliminar departamento");
            System.out.println("6. Listar departamentos");
        }

        System.out.println("7. Inspección física del fichero");
        System.out.println("8. Estadísticas");
        System.out.println("9. Comprobar integridad");
        System.out.println("0. Salir");
        System.out.println("===============================================");
        System.out.print("Elige una opción: ");
    }

    // Indica si la opción elegida del menu está permitida en el modo actual
    public static boolean esOpcionValida(int opcion, boolean diagnostico) {
        if (opcion == 0 || (opcion >= 7 && opcion <= 9)) {
            return true;
        }
        // las opciones 1 a 6 solo se permiten fuera del modo diagnóstico
        return !diagnostico && opcion >= 1 && opcion <= 6;
    }
}