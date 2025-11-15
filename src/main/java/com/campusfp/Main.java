package com.campusfp;

import conexion.ConexionPool;

import java.sql.*;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = ConexionPool.getConnection()) {

            insertarEmpleados(conn);
            insertarProyectos(conn);

            // Llamada al procedimiento almacenado
            asignarEmpleadoAProyecto(conn, 1, 1);

            // Transacción
            realizarTransaccion(conn, 1, 1, 500);

            // Mostrar resultados
            mostrarEstadoBD(conn);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===========================
    // INSERTS
    // ===========================

    private static void insertarEmpleados(Connection conn) throws Exception {
        String sql = "INSERT INTO empleados (nombre, salario) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Juan Pérez");
            ps.setDouble(2, 2000);
            ps.executeUpdate();

            ps.setString(1, "Ana Gómez");
            ps.setDouble(2, 2500);
            ps.executeUpdate();
        }
    }

    private static void insertarProyectos(Connection conn) throws Exception {
        String sql = "INSERT INTO proyectos (nombre, presupuesto) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Sistema ERP");
            ps.setDouble(2, 100000);
            ps.executeUpdate();

            ps.setString(1, "App Móvil");
            ps.setDouble(2, 50000);
            ps.executeUpdate();
        }
    }

    // ===========================
    // PROCEDIMIENTO ALMACENADO
    // ===========================

    private static void asignarEmpleadoAProyecto(Connection conn, int empId, int projId) throws Exception {
        try (CallableStatement cs = conn.prepareCall("{CALL asignar_empleado(?,?)}")) {
            cs.setInt(1, empId);
            cs.setInt(2, projId);
            cs.execute();
        }
    }

    // ===========================
    // TRANSACCIÓN
    // ===========================

    private static void realizarTransaccion(Connection conn, int empId, int projId, double incremento) throws Exception {

        String sqlEmpleado = "UPDATE empleados SET salario = salario + ? WHERE id = ?";
        String sqlProyecto = "UPDATE proyectos SET presupuesto = presupuesto - ? WHERE id = ?";

        try {
            conn.setAutoCommit(false);

            // subir salario
            try (PreparedStatement ps = conn.prepareStatement(sqlEmpleado)) {
                ps.setDouble(1, incremento);
                ps.setInt(2, empId);
                ps.executeUpdate();
            }

            // bajar presupuesto
            try (PreparedStatement ps = conn.prepareStatement(sqlProyecto)) {
                ps.setDouble(1, incremento);
                ps.setInt(2, projId);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("Transacción completada.");

        } catch (Exception e) {
            conn.rollback();
            System.out.println("Transacción revertida.");
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ===========================
    // MOSTRAR DATOS
    // ===========================

    private static void mostrarEstadoBD(Connection conn) throws Exception {
        System.out.println("\n=== EMPLEADOS ===");
        imprimirTabla(conn, "SELECT * FROM empleados");

        System.out.println("\n=== PROYECTOS ===");
        imprimirTabla(conn, "SELECT * FROM proyectos");

        System.out.println("\n=== ASIGNACIONES ===");
        imprimirTabla(conn, "SELECT * FROM asignaciones");
    }

    private static void imprimirTabla(Connection conn, String query) throws Exception {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    System.out.print(md.getColumnName(i) + ": " + rs.getString(i) + " | ");
                }
                System.out.println();
            }
        }
    }
}
