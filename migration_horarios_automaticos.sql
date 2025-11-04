-- Migración para agregar campos de control automático de horarios
-- Ejecutar este script en la base de datos sistemasinfor

USE sistemasinfor;

-- Agregar columnas para horario automático
ALTER TABLE lugares 
ADD COLUMN hora_apertura TIME NULL COMMENT 'Hora de apertura en formato 24h',
ADD COLUMN hora_cierre TIME NULL COMMENT 'Hora de cierre en formato 24h',
ADD COLUMN dias_operacion VARCHAR(20) DEFAULT '1,1,1,1,1,0,0' COMMENT 'Días de operación L-D: 1=opera, 0=no opera';

-- Actualizar lugares existentes con horarios (ejemplo)
-- Gimnasio: 6:00 AM - 9:00 PM, Lunes a Sábado
UPDATE lugares SET 
    hora_apertura = '06:00:00', 
    hora_cierre = '21:00:00',
    dias_operacion = '1,1,1,1,1,1,0'
WHERE nombre = 'Gimnasio';

-- Biblioteca: 7:00 AM - 8:00 PM, Lunes a Viernes
UPDATE lugares SET 
    hora_apertura = '07:00:00', 
    hora_cierre = '20:00:00',
    dias_operacion = '1,1,1,1,1,0,0'
WHERE nombre = 'Biblioteca Central';

-- Sala de Estudio: 8:00 AM - 6:00 PM, Lunes a Viernes
UPDATE lugares SET 
    hora_apertura = '08:00:00', 
    hora_cierre = '18:00:00',
    dias_operacion = '1,1,1,1,1,0,0'
WHERE nombre LIKE '%Sala de Estudio%';

-- Laboratorio: 9:00 AM - 5:00 PM, Lunes a Viernes
UPDATE lugares SET 
    hora_apertura = '09:00:00', 
    hora_cierre = '17:00:00',
    dias_operacion = '1,1,1,1,1,0,0'
WHERE nombre LIKE '%Laboratorio%';

-- Verificar los cambios
SELECT id, nombre, horario, estado, hora_apertura, hora_cierre, dias_operacion 
FROM lugares;
