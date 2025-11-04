# ProyectoSI — Guía rápida de ejecución y solución de errores

Esta guía resume cómo ejecutar y depurar la app de forma estable en VS Code, cómo usar el panel de configuración de tareas (backup y fitness) y cómo resolver el error del Spring Boot Dashboard (ClassNotFoundException).

## Requisitos
- Java JDK 21 (recomendado para Dashboard) o JDK 24 (funciona bien con Maven/JAR)
- Maven Wrapper (incluido: `mvnw.cmd`)
- Configuración de correo en `src/main/resources/application.properties` (para Gmail, usa «Contraseña de aplicación»)

## Ejecutar la app (estable)

### Opción A: Tareas de VS Code (sin Dashboard)
1. Abre VS Code en el proyecto.
2. Terminal → Run Task… → `Spring Boot: Run (Maven)`
3. Ve a: http://localhost:8080

Para depurar:
- Terminal → Run Task… → `Spring Boot: Run (Debug)`
- F5 → `Attach to Spring Boot (5005)`

### Opción B: Ejecutar el JAR
```cmd
cd /d c:\Users\PALOMEQUE\OneDrive\Desktop\sistemasinfor
mvnw.cmd -DskipTests clean package
cd target
"C:\\Program Files\\Java\\jdk-24\\bin\\java.exe" -jar sistemasinfor-0.0.1-SNAPSHOT.jar
```

## Spring Boot Dashboard — ClassNotFoundException
Si ves: `Could not find or load main class com.proyecto.sistemasinfor.SistemasinforApplication` al lanzar desde el Dashboard, sigue estos pasos:

1) Limpia y recarga el entorno Java
- Ctrl+Shift+P → `Java: Clean Java Language Server Workspace` → Reload Window
- Ctrl+Shift+P → `Maven: Reload project`

2) Recompila
```cmd
cd /d c:\Users\PALOMEQUE\OneDrive\Desktop\sistemasinfor
rmdir /s /q target
mvnw.cmd -B -DskipTests compile
```

3) Usa JDK 21 con el Dashboard (recomendado)
- Ctrl+Shift+P → `Java: Configure Java Runtime`
- Selecciona un JDK 21 como Default

Además, ya está configurado en `.vscode/settings.json`:
```jsonc
{
  "java.debug.settings.classPathFile": "off",
  "java.configuration.updateBuildConfiguration": "automatic"
}
```
Esto evita, en la medida de lo posible, el uso del argfile que causa el fallo.

## Panel de configuración de tareas (admin)
Ruta: `/admin/config` (visible para `ADMIN_ESPACIOS` y `ADMIN_TI`).

- Selecciona Día y Hora; el sistema genera el cron automáticamente (no hace falta escribirlo).
- Mensajes:
  - Backup: mensaje personalizado para la notificación.
  - Fitness: mensaje personalizado para recomendaciones.
- Habilitar envío: activa/desactiva cada tarea.
- Envíos de prueba:
  - "Probar copia de seguridad" → envía el mensaje de backup al correo indicado o a un destinatario por defecto.
  - "Probar fitness" → envía el mensaje de fitness (sin requerir aceptación de marketing en la prueba).

Notas de envío:
- Backup: se envía a todos los usuarios del sistema.
- Fitness: se envía únicamente a usuarios con `marketingEmailsAccepted = true`.
- Los cambios se aplican en caliente: no requiere reiniciar la app.
- Revisa la consola; verás logs como:
  - `[Scheduler] Enviando aviso de backup a N usuarios`
  - `[Scheduler] Enviando recomendaciones fitness a N usuarios (con marketing)`

## Consejos de correo (SMTP)
- Gmail requiere **Contraseña de aplicación** (no la contraseña normal).
- Comprueba `spring.mail.username`/`spring.mail.password` en `application.properties`.
- Si no llegan, prueba los botones de «correo de prueba» y revisa el log del servidor.

## Problemas habituales y soluciones
- Dashboard lanza ClassNotFound: usa las tareas Maven o cambia a JDK 21 y limpia el workspace.
- Cron no dispara: revisa la zona horaria del servidor y pon una hora 1–2 minutos al futuro para probar.
- No llegan correos fitness: verifica que existan usuarios con `marketingEmailsAccepted = true`.
- OneDrive a veces bloquea rutas/archivos temporales: si notas comportamientos extraños, considera mover el proyecto a una ruta local (p. ej. `C:\\dev\\sistemasinfor`).

---
Cualquier mejora que quieras (botón “Ejecutar ahora”, vista avanzada de cron, etc.), la añadimos rápido.
