DROP DATABASE IF EXISTS empresa;
CREATE DATABASE empresa;
USE empresa;

-- TABLAS

CREATE TABLE empleados (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL,
                           salario DECIMAL(10,2) NOT NULL
);

CREATE TABLE proyectos (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL,
                           presupuesto DECIMAL(12,2) NOT NULL
);

CREATE TABLE asignaciones (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              empleado_id INT,
                              proyecto_id INT,
                              fecha DATE,
                              FOREIGN KEY (empleado_id) REFERENCES empleados(id),
                              FOREIGN KEY (proyecto_id) REFERENCES proyectos(id)
);

-- PROCEDIMIENTO ALMACENADO

DELIMITER $$

CREATE PROCEDURE asignar_empleado(
    IN emp_id INT,
    IN proj_id INT
)
BEGIN
INSERT INTO asignaciones (empleado_id, proyecto_id, fecha)
VALUES (emp_id, proj_id, CURDATE());
END$$

DELIMITER ;
