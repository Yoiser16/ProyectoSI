# Funcionalidad de Subida de Imágenes en Admin Config

## 📝 Descripción

Se ha implementado la funcionalidad para subir imágenes en el panel de configuración del administrador (`/admin/config`). Ahora los administradores pueden adjuntar imágenes personalizadas que se incluirán en los correos electrónicos de:

- **Notificaciones de Copia de Seguridad**
- **Recomendaciones Fitness**

## ✨ Características Implementadas

### 1. Subida de Imágenes
- Campo de tipo `file` para subir imágenes en cada sección (Backup y Fitness)
- Validación de tipo de archivo (solo imágenes: JPG, PNG, GIF)
- Tamaño máximo permitido: **5MB**
- Previsualización de la imagen actual cargada

### 2. Almacenamiento
- Las imágenes se guardan en: `src/main/resources/static/img/config/`
- Nombres únicos generados con UUID para evitar conflictos
- Ruta almacenada en la base de datos en la tabla `app_settings`

### 3. Envío de Correos con Imágenes
- Nuevo método `sendEmailWithImage()` en `MailService`
- Los correos se envían en formato **HTML** cuando incluyen imágenes
- Las imágenes se incrustan directamente en el correo (inline)
- Fallback a texto plano si hay errores

### 4. Vista Previa
- Muestra la imagen actual cargada debajo del campo de subida
- Diseño visual mejorado con bordes redondeados y sombras

## 🔧 Archivos Modificados

### Backend (Java)
1. **AdminConfigController.java**
   - Añadidos parámetros `MultipartFile` para recibir imágenes
   - Método `saveImage()` para procesar y guardar archivos
   - Actualización de métodos de prueba para incluir imágenes

2. **AppSettingsService.java**
   - Nuevas constantes: `BACKUP_IMAGE` y `FITNESS_IMAGE`
   - Métodos getter: `getBackupImage()` y `getFitnessImage()`

3. **MailService.java**
   - Nuevo método `sendEmailWithImage()` para envío con HTML
   - Soporte para imágenes inline usando `MimeMessageHelper`

4. **DynamicSchedulingConfig.java**
   - Actualizado para usar imágenes en notificaciones programadas
   - Verifica si hay imagen antes de enviar

### Frontend (HTML)
5. **admin-config.html**
   - Añadido `enctype="multipart/form-data"` al formulario
   - Campos de input tipo `file` con accept="image/*"
   - Previsualización de imagen actual con Thymeleaf

### Configuración
6. **application.properties**
   - Configuración de tamaño máximo de archivos:
     ```properties
     spring.servlet.multipart.max-file-size=5MB
     spring.servlet.multipart.max-request-size=10MB
     ```

## 📂 Estructura de Directorios

```
src/main/resources/static/img/config/
├── backup_[UUID].jpg
├── backup_[UUID].png
├── fitness_[UUID].jpg
└── fitness_[UUID].png
```

## 🚀 Cómo Usar

### 1. Acceder al Panel de Configuración
- Iniciar sesión como administrador
- Navegar a: `http://localhost:8080/admin/config`

### 2. Subir una Imagen
- En la sección **Copia de Seguridad** o **Recomendaciones Fitness**
- Hacer clic en el campo "Imagen de Notificación"
- Seleccionar una imagen (JPG, PNG, GIF)
- Escribir el mensaje personalizado
- Hacer clic en **"Guardar Configuración"**

### 3. Probar el Envío
- Usar los botones de prueba al final de la página
- Ingresar un email o dejar vacío para usar destinatario por defecto
- Verificar que el correo incluya la imagen

### 4. Vista en el Correo
Los correos se verán así:

```
┌─────────────────────────────┐
│  [Imagen centrada]          │
├─────────────────────────────┤
│                             │
│  Mensaje personalizado      │
│  con saltos de línea        │
│  respetados                 │
│                             │
└─────────────────────────────┘
```

## 🔒 Validaciones de Seguridad

1. **Tipo de archivo**: Solo se aceptan imágenes (validación por Content-Type)
2. **Tamaño**: Máximo 5MB por archivo
3. **Permisos**: Solo administradores pueden subir imágenes
4. **Nombres únicos**: UUID evita sobrescrituras

## 💾 Base de Datos

Se añadieron dos nuevas claves en la tabla `app_settings`:

| Clave                         | Descripción                          |
|-------------------------------|--------------------------------------|
| `backup.notification.image`   | Ruta de imagen para notif. backup   |
| `fitness.recommendation.image`| Ruta de imagen para recomendaciones |

Ejemplo de valores:
```
/img/config/backup_a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
/img/config/fitness_x9y8z7w6-v5u4-3210-zyxw-vut987654321.png
```

## 🐛 Solución de Problemas

### La imagen no se muestra en el correo
- Verificar que el archivo se guardó en `src/main/resources/static/img/config/`
- Comprobar la ruta en la base de datos
- Revisar los logs de Spring Boot para errores

### Error al subir archivo
- Verificar que el archivo sea menor a 5MB
- Confirmar que el tipo sea imagen (JPG, PNG, GIF)
- Revisar permisos de escritura en el directorio

### El correo llega sin formato HTML
- Normal si no hay imagen configurada (se envía texto plano)
- Si hay imagen pero falla, verifica que exista físicamente el archivo

## 📊 Ejemplo de Uso

```java
// El sistema automáticamente detecta si hay imagen
String message = "Recordatorio: Próxima copia de seguridad";
String imagePath = "/img/config/backup_12345.jpg";

// Si hay imagen, envía HTML con imagen inline
mailService.sendEmailWithImage(
    "usuario@ejemplo.com",
    "Aviso de Backup",
    message,
    imagePath
);

// Si no hay imagen (imagePath == null o ""), envía texto plano
```

## 🎨 Personalización

Para cambiar el estilo del correo HTML, modificar el método `sendEmailWithImage()` en `MailService.java`:

```java
// Personalizar estilos CSS inline
htmlContent.append("<div style='max-width: 600px; padding: 20px; background: #f5f5f5;'>");
htmlContent.append("<img src='cid:emailImage' style='border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);' />");
```

## 📝 Notas Importantes

1. **Producción**: En producción, considera usar un servicio de almacenamiento externo (AWS S3, Azure Blob Storage) en lugar de guardar en el sistema de archivos local
2. **Backups**: Incluir el directorio `static/img/config/` en las copias de seguridad
3. **Limpieza**: Implementar un job para eliminar imágenes antiguas no utilizadas
4. **Optimización**: Las imágenes no se redimensionan automáticamente, se recomienda subir imágenes ya optimizadas

## ✅ Testing

Para probar la funcionalidad:

1. Subir una imagen de prueba
2. Usar el botón "Probar Backup" o "Probar Fitness"
3. Verificar el correo recibido
4. Confirmar que la imagen se muestra correctamente

---

**Autor**: Sistema Sinfor Team  
**Fecha**: 4 de noviembre de 2025  
**Versión**: 1.0.0
