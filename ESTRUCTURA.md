# Estructura de carpetas del frontend

```
src/
├── main.ts
├── index.html
├── styles.css                  ← variables globales (--surface, --border, --primary...) y estilos base
└── app/
    ├── app.ts / app.html / app.css
    ├── app.config.ts           ← providers (HttpClient, interceptor, router)
    ├── app.routes.ts           ← todas las rutas
    │
    ├── core/                   ← SERVICIOS y lógica compartida (sin pantallas)
    │   ├── auth/
    │   │   ├── auth.ts               → clase Auth (login, registro, token)
    │   │   ├── auth-guard.ts         → protege rutas
    │   │   └── auth-interceptor.ts   → agrega el token y maneja errores
    │   └── usuario/
    │       └── usuario.ts            → clase Usuario + interfaz UsuarioPerfil (GET /usuarios/me)
    │
    └── features/               ← PANTALLAS (componentes)
        ├── auth/
        │   ├── login/          → login.ts / .html / .css / .spec.ts
        │   └── registro/       → registro.ts / .html / .css / .spec.ts
        ├── home/               → home.ts / .html / .css / .spec.ts
        └── perfil/             → perfil.ts / .html / .css / .spec.ts
```

## Reglas

| Qué | Dónde | Ejemplo |
|---|---|---|
| Servicio que llama al backend | `core/<recurso>/<recurso>.ts` | `core/usuario/usuario.ts` |
| Interfaces de request/response | En el mismo archivo del servicio | `UsuarioPerfil` dentro de `usuario.ts` |
| Guard o interceptor | `core/<recurso>/` | `core/auth/auth-guard.ts` |
| Pantalla | `features/<pantalla>/` | `features/perfil/` |
| Varias pantallas del mismo tema | `features/<tema>/<pantalla>/` | `features/auth/login/` |
| Variables de color, radios, fuente | `styles.css` | `var(--primary)` |
