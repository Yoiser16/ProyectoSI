# Guía para Añadir Soporte de Temas a Páginas HTML

## ✅ Páginas YA actualizadas con soporte de temas (9/18)

1. **menu.html** ✅ - Panel principal con toggle en topbar
2. **chatbot.html** ✅ - Asistente virtual con toggle flotante
3. **perfil.html** ✅ - Edición de perfil con toggle flotante
4. **eventos.html** ✅ - Calendario y lista de eventos
5. **reservas.html** ✅ - Gestión de reservas de estudiantes
6. **faq.html** ✅ - Preguntas frecuentes
7. **login.html** ✅ - Inicio de sesión
8. **register.html** ✅ - Registro de usuarios
9. **admin-eventos.html** ✅ - Administración de eventos

## ⏳ Páginas PENDIENTES de actualizar (9/18)

- admin-lugares.html
- admin-reservas.html
- admin-users.html
- seleccionar-perfil.html
- consentimiento.html
- forgot-password.html
- reset-password.html
- reset-password-form.html
- editar-lugar.html
- unlock-account-result.html

## Pasos para actualizar cada página:

### 1. En el `<head>`, después del `<title>`:

```html
<!-- Si no tiene FontAwesome, añadir: -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
<!-- Siempre añadir estos dos: -->
<link rel="stylesheet" th:href="@{/theme.css}">
<script th:src="@{/theme.js}"></script>
```

### 2. En el `<body>`, al inicio (justo después de `<body>`):

```html
<span class="theme-toggle-fixed" id="themeToggle" title="Cambiar tema">
    <i class="fa-solid fa-moon"></i>
</span>
```

### 3. Reemplazar colores hard-coded por variables CSS:

| Color hard-coded | Reemplazar con variable |
|------------------|-------------------------|
| `#f5f7fa`, `#f5f6fa` (fondo) | `var(--bg-primary)` |
| `#fff`, `#ffffff` (fondo card) | `var(--bg-card)` |
| `#007bff` (azul primary) | `var(--nav-text)` |
| `#333`, `#222` (texto oscuro) | `var(--text-primary)` |
| `#444`, `#555` (texto secundario) | `var(--text-secondary)` |
| `#e0e0e0`, `#d1d9e6` (bordes) | `var(--border-color)` |
| `input background #fff` | `var(--input-bg)` |
| `input border` | `var(--input-border)` |
| `button background #007bff` | `var(--button-primary)` |

### 4. Ejemplo de conversión:

**Antes:**
```css
body {
    background: #f5f7fa;
    color: #333;
}
.card {
    background: #fff;
    border: 1px solid #e0e0e0;
}
```

**Después:**
```css
body {
    background: var(--bg-primary);
    color: var(--text-primary);
}
.card {
    background: var(--bg-card);
    border: 1px solid var(--border-color);
}
```

## Variables CSS disponibles:

Ver archivo `theme.css` para la lista completa de variables.

## Testing:

1. Abrir la página en el navegador
2. Hacer clic en el botón de luna/sol
3. Verificar que todos los elementos cambien de color
4. Refrescar la página y verificar que el tema persista
