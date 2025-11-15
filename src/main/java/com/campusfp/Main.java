package com.campusfp;

import conexion.ConexionPool;

import java.sql.*;

public class Main {
    public static void main(String[] args) {

        try (Connection conn = ConexionPool.getConnection()) {

            System.out.println("Conexion a la base de datos establecida correctamente\n");

            // Paso 1: Insertar datos iniciales
            System.out.println("PASO 1: INSERTANDO DATOS INICIALES");
            System.out.println("-----------------------------------");
            insertarEmpleados(conn);
            insertarProyectos(conn);

            // Mostrar estado después de las inserciones
            System.out.println("\nESTADO DESPUES DE LAS INSERCIONES:");
            mostrarEstadoBD(conn);

            // Paso 2: Llamada al procedimiento almacenado
            System.out.println("\nPASO 2: ASIGNANDO EMPLEADO A PROYECTO");
            System.out.println("--------------------------------------");
            asignarEmpleadoAProyecto(conn, 1, 1);

            // Mostrar estado después de la asignación
            System.out.println("\nESTADO DESPUES DE LA ASIGNACION:");
            mostrarEstadoBD(conn);

            // Paso 3: Transacción
            System.out.println("\nPASO 3: REALIZANDO TRANSACCION");
            System.out.println("------------------------------");
            realizarTransaccion(conn, 1, 1, 500);

            System.out.println("\nPROGRAMA EJECUTADO EXITOSAMENTE");

        } catch (Exception e) {
            System.err.println("ERROR durante la ejecucion:");
            e.printStackTrace();
        }
    }

    // ===========================
    // INSERTS
    // ===========================

    private static void insertarEmpleados(Connection conn) throws Exception {
        System.out.println("Insertando empleados...");
        String sql = "INSERT INTO empleados (nombre, salario) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Juan Perez");
            ps.setDouble(2, 2000);
            ps.executeUpdate();
            System.out.println("Empleado 'Juan Perez' insertado (Salario: 2000€)");

            ps.setString(1, "Ana Gomez");
            ps.setDouble(2, 2500);
            ps.executeUpdate();
            System.out.println("Empleado 'Ana Gomez' insertado (Salario: 2500€)");

            System.out.println("Empleados insertados correctamente");
        }
    }

    private static void insertarProyectos(Connection conn) throws Exception {
        System.out.println("Insertando proyectos...");
        String sql = "INSERT INTO proyectos (nombre, presupuesto) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Sistema ERP");
            ps.setDouble(2, 100000);
            ps.executeUpdate();
            System.out.println("Proyecto 'Sistema ERP' insertado (Presupuesto: 100,000€)");

            ps.setString(1, "App Movil");
            ps.setDouble(2, 50000);
            ps.executeUpdate();
            System.out.println("Proyecto 'App Movil' insertado (Presupuesto: 50,000€)");

            System.out.println("Proyectos insertados correctamente");
        }
    }

    // ===========================
    // PROCEDIMIENTO ALMACENADO
    // ===========================

    private static void asignarEmpleadoAProyecto(Connection conn, int empId, int projId) throws Exception {
        System.out.println("Ejecutando procedimiento almacenado...");
        System.out.println("Asignando empleado ID " + empId + " al proyecto ID " + projId);

        try (CallableStatement cs = conn.prepareCall("{CALL asignar_empleado(?,?)}")) {
            cs.setInt(1, empId);
            cs.setInt(2, projId);
            cs.execute();
            System.out.println("Procedimiento ejecutado correctamente");
        }
    }

    // ===========================
    // TRANSACCION
    // ===========================

    private static void realizarTransaccion(Connection conn, int empId, int projId, double incremento) throws Exception {
        System.out.println("Iniciando transaccion...");
        System.out.println("Incrementando salario del empleado " + empId + " en " + incremento + "€");
        System.out.println("Reduciendo presupuesto del proyecto " + projId + " en " + incremento + "€");

        String sqlEmpleado = "UPDATE empleados SET salario = salario + ? WHERE id = ?";
        String sqlProyecto = "UPDATE proyectos SET presupuesto = presupuesto - ? WHERE id = ?";

        try {
            conn.setAutoCommit(false);

            // Mostrar valores antes de la transacción
            System.out.println("\nVALORES ANTES DE LA TRANSACCION:");
            mostrarValoresEspecificos(conn, empId, projId);

            // Subir salario
            try (PreparedStatement ps = conn.prepareStatement(sqlEmpleado)) {
                ps.setDouble(1, incremento);
                ps.setInt(2, empId);
                int filasAfectadas = ps.executeUpdate();
            }

            // Bajar presupuesto
            try (PreparedStatement ps = conn.prepareStatement(sqlProyecto)) {
                ps.setDouble(1, incremento);
                ps.setInt(2, projId);
                int filasAfectadas = ps.executeUpdate();
            }

            conn.commit();
            System.out.println("\nTransaccion completada exitosamente");

            // Mostrar valores después de la transacción
            System.out.println("\nVALORES DESPUES DE LA TRANSACCION:");
            mostrarValoresEspecificos(conn, empId, projId);

        } catch (Exception e) {
            conn.rollback();
            System.out.println("Transaccion revertida debido a un error");
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ===========================
    // MOSTRAR DATOS
    // ===========================

    private static void mostrarEstadoBD(Connection conn) throws Exception {
        System.out.println("\nTABLA EMPLEADOS:");
        imprimirTabla(conn, "SELECT * FROM empleados");

        System.out.println("\nTABLA PROYECTOS:");
        imprimirTabla(conn, "SELECT * FROM proyectos");

        System.out.println("\nTABLA ASIGNACIONES:");
        imprimirTabla(conn, "SELECT * FROM asignaciones");
    }

    private static void mostrarValoresEspecificos(Connection conn, int empId, int projId) throws Exception {
        // Mostrar salario del empleado
        try (PreparedStatement ps = conn.prepareStatement("SELECT nombre, salario FROM empleados WHERE id = ?")) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("  Empleado: " + rs.getString("nombre") + " - Salario: " + rs.getDouble("salario") + "€");
            }
        }

        // Mostrar presupuesto del proyecto
        try (PreparedStatement ps = conn.prepareStatement("SELECT nombre, presupuesto FROM proyectos WHERE id = ?")) {
            ps.setInt(1, projId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("  Proyecto: " + rs.getString("nombre") + " - Presupuesto: " + rs.getDouble("presupuesto") + "€");
            }
        }
    }

    private static void imprimirTabla(Connection conn, String query) throws Exception {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();

            // Imprimir datos
            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    System.out.print(md.getColumnName(i) + ": " + rs.getString(i) + " | ");
                }
                System.out.println();
            }
        }
    }
}