# 📅 Ejemplos de Funcionamiento - Horarios Automáticos

## 🎯 Comportamiento del Sistema

El sistema actualiza automáticamente el estado de los lugares **cada minuto** basándose en:
1. ⏰ **Hora actual** vs Horarios configurados (apertura/cierre)
2. 📆 **Día actual** vs Días de operación configurados
3. 🔒 **Respeto a estados especiales** (Mantenimiento, Reservado)

---

## 📋 Ejemplos Prácticos

### **Ejemplo 1: Gimnasio - Operación Normal**

**Configuración:**
- Hora Apertura: `10:00`
- Hora Cierre: `16:00`
- Días: `L,M,X,J,V` (Lunes a Viernes)

**Comportamiento:**

| Día | Hora | Estado Anterior | Estado Nuevo | Razón |
|-----|------|----------------|--------------|-------|
| Lunes | 09:59 | Cerrado | Cerrado | Antes del horario |
| Lunes | 10:00 | Cerrado | **Abierto** ✅ | Inicio de horario |
| Lunes | 12:00 | Abierto | Abierto | Dentro del horario |
| Lunes | 15:59 | Abierto | Abierto | Dentro del horario |
| Lunes | 16:00 | Abierto | **Cerrado** ❌ | Fin de horario |
| **Sábado** | 12:00 | Abierto | **Cerrado** ❌ | No opera hoy |

**Log esperado:**
```
🔄 Actualizando estado de 'Gimnasio': Cerrado → Abierto (Inicio de horario) (Hora: 10:00, Día: MONDAY)
🔄 Actualizando estado de 'Gimnasio': Abierto → Cerrado (Fuera de horario) (Hora: 16:00, Día: MONDAY)
🔄 Actualizando estado de 'Gimnasio': Abierto → Cerrado (No opera hoy) (Hora: 12:00, Día: SATURDAY)
```

---

### **Ejemplo 2: Cambio de Días de Operación en Tiempo Real**

**Escenario:** Hoy es **Martes** a las 14:00, el gimnasio está **Abierto**

**Acción del Admin:**
- Edita el gimnasio
- **Quita el Martes** de los días de operación
- Nueva configuración: `L,X,J,V` (Lunes, Miércoles, Jueves, Viernes)
- Guarda los cambios

**Resultado:**
- ⏱️ En el **siguiente minuto** (14:01), el sistema detecta:
  - Hoy es Martes
  - El lugar NO opera los martes
  - Estado actual: "Abierto"
  - ✅ **Cambia automáticamente a "Cerrado"**

**Log esperado:**
```
🔄 Actualizando estado de 'Gimnasio': Abierto → Cerrado (No opera hoy) (Hora: 14:01, Día: TUESDAY)
```

---

### **Ejemplo 3: Biblioteca - Fines de Semana**

**Configuración:**
- Hora Apertura: `09:00`
- Hora Cierre: `18:00`
- Días: `S,D` (Sábado y Domingo)

**Comportamiento:**

| Día | Hora | Estado Anterior | Estado Nuevo | Razón |
|-----|------|----------------|--------------|-------|
| Viernes | 12:00 | Cerrado | Cerrado | No opera hoy |
| **Sábado** | 08:59 | Cerrado | Cerrado | Antes del horario |
| **Sábado** | 09:00 | Cerrado | **Abierto** ✅ | Inicio de horario |
| **Sábado** | 18:00 | Abierto | **Cerrado** ❌ | Fin de horario |
| **Domingo** | 10:00 | Cerrado | **Abierto** ✅ | Dentro del horario |

---

### **Ejemplo 4: Respeto a Estados Especiales**

**Escenario:** Laboratorio en **Mantenimiento**

**Configuración:**
- Hora Apertura: `08:00`
- Hora Cierre: `17:00`
- Días: `L,M,X,J,V`
- **Estado Manual:** `Mantenimiento` 🔧

**Comportamiento:**

| Día | Hora | Estado Actual | Acción del Sistema |
|-----|------|---------------|-------------------|
| Lunes | 08:00 | Mantenimiento | ⏭️ **No cambia** (respeta estado especial) |
| Lunes | 12:00 | Mantenimiento | ⏭️ **No cambia** |
| Lunes | 17:00 | Mantenimiento | ⏭️ **No cambia** |

**Log esperado:**
```
⏭️ Saltando 'Laboratorio' - Estado especial: Mantenimiento
```

**Importante:** El sistema **SOLO** gestiona automáticamente estados `Abierto` y `Cerrado`. Los estados especiales (`Mantenimiento`, `Reservado`) se mantienen hasta que el administrador los cambie manualmente.

---

### **Ejemplo 5: Horario Nocturno (Cruce de Medianoche)**

**Configuración:** Bar Universitario
- Hora Apertura: `20:00` (8:00 PM)
- Hora Cierre: `02:00` (2:00 AM del día siguiente)
- Días: `V,S` (Viernes y Sábado)

**Comportamiento:**

| Día | Hora | Estado | Explicación |
|-----|------|--------|-------------|
| Viernes | 19:59 | Cerrado | Antes de apertura |
| Viernes | 20:00 | **Abierto** ✅ | Inicia horario nocturno |
| Viernes | 23:59 | Abierto | Dentro del horario |
| Sábado | 00:00 | Abierto | Sigue dentro del horario (cruzó medianoche) |
| Sábado | 01:59 | Abierto | Última hora de operación |
| Sábado | 02:00 | **Cerrado** ❌ | Fin de horario |

---

## 🔍 Casos de Prueba Recomendados

### **Prueba 1: Día de No Operación**
1. Crea un lugar con días L-V
2. Ponlo en estado "Abierto" manualmente
3. Cambia el sistema a día Sábado (o espera al sábado)
4. ✅ Verifica que cambia a "Cerrado" automáticamente

### **Prueba 2: Quitar Día Actual**
1. Hoy es Miércoles, lugar abierto
2. Edita el lugar y desmarca "Miércoles"
3. Guarda
4. ✅ Espera 1 minuto, verifica que cambió a "Cerrado"

### **Prueba 3: Cambio de Horario**
1. Lugar con horario 08:00-17:00
2. Son las 16:00, está "Abierto"
3. Edita y cambia horario de cierre a 15:00
4. Guarda
5. ✅ Espera 1 minuto, verifica que cambió a "Cerrado"

### **Prueba 4: Estado Especial**
1. Lugar en "Mantenimiento"
2. Llega la hora de apertura configurada
3. ✅ Verifica que NO cambia a "Abierto" (respeta el mantenimiento)

---

## 📊 Logs del Sistema

### **Activación del Servicio (al iniciar la app):**
```
✅ Servicio de actualización automática de lugares activado
📅 Los lugares se verificarán cada minuto para actualizar su estado según horario
```

### **Verificación cada minuto (modo debug):**
```
Verificando estado de lugares...
⏭️ Saltando 'Laboratorio' - Estado especial: Mantenimiento
```

### **Cambio de estado:**
```
🔄 Actualizando estado de 'Gimnasio': Cerrado → Abierto (Inicio de horario) (Hora: 10:00, Día: MONDAY)
🔄 Actualizando estado de 'Biblioteca': Abierto → Cerrado (No opera hoy) (Hora: 14:23, Día: SATURDAY)
🔄 Actualizando estado de 'Sala de Estudio': Abierto → Cerrado (Fuera de horario) (Hora: 18:00, Día: FRIDAY)
```

---

## 🎯 Resumen de Reglas

1. ✅ **Se actualiza automáticamente:** Si el estado es `Abierto` o `Cerrado`
2. ⏭️ **NO se actualiza:** Si el estado es `Mantenimiento`, `Reservado` u otro
3. 📅 **Considera el día:** Si hoy no opera, cierra automáticamente
4. ⏰ **Considera la hora:** Abre/cierra según el horario configurado
5. 🔄 **Frecuencia:** Verifica cada 60 segundos
6. 🚀 **Inmediato:** Los cambios de configuración se aplican en el siguiente minuto

---

## 💡 Recomendaciones

- 🔧 Usa "Mantenimiento" cuando necesites que permanezca cerrado independientemente del horario
- 📅 Usa "Reservado" para eventos especiales
- ✅ Deja que "Abierto"/"Cerrado" se gestionen automáticamente según el horario
- 📊 Revisa los logs para entender el comportamiento del sistema
