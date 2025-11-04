# 🤖 Sistema de Control Automático de Horarios de Lugares

## 📋 Descripción General

Este sistema actualiza automáticamente el estado de los lugares (Gimnasio, Biblioteca, Salas, etc.) según:
- ⏰ **Horarios configurados** (hora de apertura y cierre)
- 📅 **Días de operación** (L, M, X, J, V, S, D)
- 🔄 **Verificación cada minuto** para cambios precisos

---

## 🎯 ¿Cómo Funciona?

### 1️⃣ **Actualización Automática**
Cada **60 segundos**, el sistema verifica todos los lugares y:

```
┌─────────────────────────────────────────┐
│  ¿Tiene horarios configurados?          │
│  (hora_apertura y hora_cierre)          │
└──────────────┬──────────────────────────┘
               │
               ├─── NO ──→ ⏭️ Omitir (no cambiar estado)
               │
               └─── SÍ ──→ Continuar
                           │
        ┌──────────────────┴──────────────────┐
        │  ¿Estado actual es 'Abierto'        │
        │  o 'Cerrado'?                       │
        └──────────────┬─────────────────────┘
                       │
                       ├─── NO ──→ ⏭️ Omitir (Mantenimiento/Reservado)
                       │
                       └─── SÍ ──→ Verificar condiciones
                                   │
                ┌──────────────────┴──────────────────┐
                │  ¿Opera hoy según días de operación?│
                └──────────────┬─────────────────────┘
                               │
                               ├─── NO ──→ 🔴 Cambiar a "Cerrado"
                               │
                               └─── SÍ ──→ ¿Está dentro del horario?
                                           │
                                           ├─── SÍ ──→ 🟢 Cambiar a "Abierto"
                                           │
                                           └─── NO ──→ 🔴 Cambiar a "Cerrado"
```

---

## 📊 Reglas de Actualización

### ✅ **SE ACTUALIZA AUTOMÁTICAMENTE**
| Estado Actual | Condición | Nuevo Estado |
|---------------|-----------|--------------|
| Abierto | Fuera de horario | Cerrado |
| Abierto | Día no operativo | Cerrado |
| Cerrado | Dentro de horario + día operativo | Abierto |

### ❌ **NO SE ACTUALIZA AUTOMÁTICAMENTE**
| Estado Actual | Razón |
|---------------|-------|
| Mantenimiento | Estado manual - requiere intervención humana |
| Reservado | Estado manual - requiere intervención humana |
| (Sin horarios) | No tiene hora_apertura o hora_cierre configuradas |

---

## 🔍 Ejemplos Prácticos

### **Ejemplo 1: Gimnasio - Lunes a Viernes, 10:00-16:00**

**Configuración:**
- Apertura: 10:00
- Cierre: 16:00
- Días: L,M,X,J,V (1,1,1,1,1,0,0)

**Comportamiento:**

| Día | Hora | Estado Antes | Estado Después | Razón |
|-----|------|--------------|----------------|-------|
| Lunes | 09:59 | Cerrado | Cerrado | Antes de apertura |
| Lunes | 10:00 | Cerrado | **Abierto** ✅ | Hora de apertura |
| Lunes | 12:00 | Abierto | Abierto | Dentro de horario |
| Lunes | 16:00 | Abierto | **Cerrado** 🔴 | Hora de cierre |
| **Sábado** | 12:00 | Abierto | **Cerrado** 🔴 | No opera sábados |

### **Ejemplo 2: Cambio de Días de Operación**

**Escenario:** Hoy es **martes** a las 14:00
- Estado actual: Abierto
- Horario: 10:00-18:00
- Días configurados: L,M,X,J,V (1,1,1,1,1,0,0)

**Acción:** Admin **desmarca el martes** → Días: L,X,J,V (1,0,1,1,1,0,0)

**Resultado:**
```
🔄 Actualizando estado de 'Gimnasio': Abierto → Cerrado (No opera los martes)
```
⏱️ **Tiempo de actualización:** Máximo 60 segundos

### **Ejemplo 3: Estado Manual no se Modifica**

**Escenario:** Biblioteca en Mantenimiento
- Estado actual: Mantenimiento
- Horario: 08:00-20:00
- Hoy es lunes a las 10:00 (debería estar abierto)

**Resultado:**
```
⏭️ Omitiendo 'Biblioteca' - Estado manual: Mantenimiento
```
El sistema **NO cambia** el estado. Debe cambiarse manualmente desde el panel de admin.

---

## 🌙 Soporte para Horarios Nocturnos

El sistema soporta lugares que operan pasada la medianoche:

**Ejemplo: Bar Universitario 22:00-02:00**
- Apertura: 22:00
- Cierre: 02:00

| Hora | Estado |
|------|--------|
| 21:59 | Cerrado |
| 22:00 | Abierto |
| 23:30 | Abierto |
| 00:30 | Abierto (pasó medianoche) |
| 01:59 | Abierto |
| 02:00 | Cerrado |

---

## 📝 Logs del Sistema

### **Al Iniciar la Aplicación:**
```
✅ Servicio de actualización automática de lugares activado
📅 Los lugares se verificarán cada minuto para actualizar su estado según:
   • Hora de apertura/cierre configurada
   • Días de operación seleccionados
   • Solo se actualizan lugares en estado 'Abierto' o 'Cerrado'
   • Estados 'Mantenimiento' y 'Reservado' no se modifican automáticamente
```

### **Cuando Cambia un Estado:**
```
🔄 Actualizando estado de 'Gimnasio': Cerrado → Abierto (Hora de apertura alcanzada (10:00))
🔄 Actualizando estado de 'Biblioteca': Abierto → Cerrado (No opera los domingos)
🔄 Actualizando estado de 'Sala A': Abierto → Cerrado (Después de hora de cierre (18:00))
```

### **Cuando Omite un Lugar:**
```
⏭️ Omitiendo 'Laboratorio' - Estado manual: Mantenimiento
```

---

## ⚙️ Configuración en Panel Admin

### **Pasos para Configurar un Lugar:**

1. Ir a `/admin/lugares`
2. Clic en "Editar" en el lugar deseado
3. En "Control Automático de Estado":
   - **Hora de Apertura:** Seleccionar (ej: 10:00)
   - **Hora de Cierre:** Seleccionar (ej: 16:00)
   - **Días de Operación:** Marcar checkboxes (L,M,X,J,V,S,D)
4. El campo "Horario de funcionamiento" se **genera automáticamente**
   - Ejemplo: `10:00 AM - 4:00 PM, Lunes a Viernes`
5. Guardar cambios

### **Para Desactivar el Control Automático:**
- Dejar vacíos los campos de Hora de Apertura y Hora de Cierre
- El lugar quedará en estado manual permanente

---

## 🎨 Estados de los Lugares

| Estado | Color | Ícono | Actualización Automática |
|--------|-------|-------|--------------------------|
| Abierto | 🟢 Verde | ✅ | SÍ |
| Cerrado | 🔴 Rojo | ❌ | SÍ |
| Mantenimiento | 🟠 Naranja | 🔧 | NO |
| Reservado | 🟠 Naranja | 📅 | NO |

---

## 🛠️ Archivos del Sistema

### **Backend:**
- `Lugar.java` - Modelo con campos: horaApertura, horaCierre, diasOperacion
- `LugarService.java` - Lógica de negocio para verificar horarios
- `LugarSchedulerService.java` - Tarea programada que actualiza estados
- `AdminLugarController.java` - Controlador para gestión de lugares

### **Frontend:**
- `admin-lugares.html` - Lista de lugares con badges de colores
- `editar-lugar.html` - Formulario con auto-generación de descripción

### **Base de Datos:**
```sql
ALTER TABLE lugares ADD COLUMN hora_apertura TIME;
ALTER TABLE lugares ADD COLUMN hora_cierre TIME;
ALTER TABLE lugares ADD COLUMN dias_operacion VARCHAR(20) DEFAULT '1,1,1,1,1,0,0';
```

---

## 💡 Tips y Mejores Prácticas

1. **Para cerrar temporalmente un lugar:**
   - Cambiar estado a "Mantenimiento" (se mantiene manual)
   - Cuando esté listo, cambiar a "Cerrado" y el sistema lo abrirá automáticamente

2. **Para eventos especiales:**
   - Cambiar a "Reservado" mientras dure el evento
   - Después volver a "Cerrado" o "Abierto" según corresponda

3. **Días festivos:**
   - El sistema no detecta festivos automáticamente
   - Cambiar manualmente a "Cerrado" ese día
   - O ajustar temporalmente los días de operación

4. **Verificación de cambios:**
   - Los cambios se aplican en máximo 60 segundos
   - Revisa los logs de la aplicación para confirmar

---

## 📞 Solución de Problemas

### **El estado no cambia automáticamente**
✅ Verificar que:
- Hora de apertura y cierre estén configuradas
- El estado actual sea "Abierto" o "Cerrado" (no manual)
- Los días de operación incluyan el día actual
- La aplicación esté corriendo

### **El horario descriptivo no se genera**
✅ Asegurarse de:
- Seleccionar ambas horas (apertura y cierre)
- Marcar al menos un día de operación
- JavaScript esté habilitado en el navegador

---

**Desarrollado por:** Yoiser Agualimpia, Januar Diaz - 2025
