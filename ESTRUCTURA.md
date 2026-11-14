# Estructura de carpetas del frontend

```
src/
├── main.ts
├── index.html
├── styles.css                  ← variables globales (--surface, --border, --primary...) y estilos base
├── environments/               ← apiUrl por entorno (environment.ts / environment.development.ts)
└── app/
    ├── app.ts / app.html / app.css
    ├── app.config.ts           ← providers (HttpClient, interceptor, router)
    ├── app.routes.ts           ← todas las rutas (lazy loading con loadComponent)
    │
    ├── core/                   ← SERVICIOS y modelos por recurso del backend (sin pantallas)
    │   ├── auth/               → auth.ts, auth-guard.ts, rol-guard.ts, auth-interceptor.ts
    │   ├── ejercicios/         → ejercicio-service.ts + ejercicio.model.ts
    │   ├── musculos/           → musculo-service.ts + musculo.model.ts
    │   ├── rutinas/            → rutinas.ts + rutinas.model.ts
    │   └── alumnos/ amigos/ entrenador/ entrenamiento/ gimnasios/ records/ suscripciones/ usuario/
    │
    ├── shared/                 ← piezas reutilizables, sin lógica de un recurso puntual
    │   ├── components/
    │   │   ├── progresion/     → <app-progresion> (gráfico de progresión)
    │   │   └── rachas/         → <app-rachas> (racha actual / máxima)
    │   └── models/
    │       └── page.ts         → Page<T> (respuesta paginada genérica)
    │
    ├── layout/                 ← estructura de la app logueada
    │   ├── layout.ts           → contenedor con <router-outlet>
    │   └── sidebar/
    │
    └── features/               ← PANTALLAS (componentes ruteados)
        ├── administracion/     → ganancias/ gimnasios/ usuarios/
        ├── alumnos/            → mis-alumnos/ alumno-detalle/
        ├── amigos/             → mis-amigos/ amigo-detalle/
        ├── auth/               → login/ registro/
        ├── ejercicios/         → ejercicio-list/ ejercicio-form/ ejercicio-details/
        ├── entrenamientos/     → entrenamiento-hoy/ historial/
        ├── gimnasios/          → gimnasios-list/ gimnasio-details/ gestion-gimnasio/ mi-suscripcion/
        ├── musculos/           → musculo-list/ musculo-form/
        ├── records/            → mis-records/ record-detalle/
        ├── rutinas/            → mis-rutinas/ rutina-form/ rutina-details/ rutinas-publicas/
        └── entrenador/ home/ perfil/ not-found/
```
