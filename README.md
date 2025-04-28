# BudgetMaster 💰

Aplicación Android nativa para la gestión de finanzas personales. Permite registrar transacciones (ingresos/gastos) y establecer metas de ahorro.

## Descripción

BudgetMaster es una herramienta diseñada para ayudarte a tomar el control de tus finanzas personales de manera sencilla y eficiente. Registra tus movimientos diarios, categorízalos y visualiza cómo avanzas hacia tus objetivos de ahorro.

## Tecnologías Utilizadas 🛠️

*   **Lenguaje:** Kotlin
*   **UI Toolkit:** Jetpack Compose
*   **Persistencia de Datos:** Room (base de datos local SQLite)
*   **Navegación:** Navigation Compose
*   **Gestión de Dependencias:** Gradle con catálogo de versiones (`libs.versions.toml`)

## Arquitectura 🏗️

El proyecto sigue el patrón de arquitectura **MVVM (Model-View-ViewModel)** para una separación clara de responsabilidades y facilitar el mantenimiento y la escalabilidad.

## Características Implementadas ✅

*   **Capa de Datos:**
    *   Modelos de datos (`Transaction`, `Category`, `Goal`).
    *   DAOs (Data Access Objects) para cada modelo.
    *   Convertidores de tipos para Room.
    *   Configuración de la base de datos `AppDatabase`.
    *   Repositorio (`BudgetRepository` y `OfflineBudgetRepository`).
*   **Configuración de la Aplicación:**
    *   Clase `BudgetMasterApplication` con `AppContainer` para inyección básica de dependencias.
    *   `AndroidManifest.xml` configurado.
*   **UI y Navegación:**
    *   Definición de rutas con `Navigation Compose`.
    *   `ViewModelFactory` para la creación de ViewModels.
    *   Implementación básica de la pantalla de lista de transacciones (`TransactionListScreen` y `TransactionListViewModel`).
    *   Barra de navegación inferior (`BottomNavigationBar`).

## Tareas Pendientes 🚧

*   **Implementar Pantallas Restantes:**
    *   Dashboard (Resúmenes, últimos movimientos, progreso de metas).
    *   Metas (Listar, crear, editar, eliminar, progreso).
    *   Reportes (Visualización de gastos por categoría).
    *   Categorías (Gestión manual opcional).
    *   Añadir/Editar Transacción (Formulario).
    *   Añadir/Editar Meta (Formulario).
*   **Conectar Navegación:**
    *   Habilitar navegación desde el botón flotante y listas hacia pantallas de edición.
*   **Refinar UI:**
    *   Reemplazar `PlaceholderScreen` con implementaciones reales.
    *   Mostrar nombres de categorías en listas.
    *   Implementar componentes visuales para reportes (gráficos).
    *   Diseñar y construir formularios.
    *   Aplicar estilo visual definido.
*   **Implementar Lógica de Negocio:**
    *   Funciones CRUD en ViewModels.
    *   Cálculos para Dashboard y Reportes.
    *   Actualización del `currentAmount` de las metas.
*   **Base de Datos (Opcional):**
    *   Considerar añadir categorías por defecto.
*   **Pruebas (Testing):**
    *   Añadir pruebas unitarias y de instrumentación.

## Cómo Empezar 🚀

1.  Clona este repositorio.
2.  Abre el proyecto en Android Studio.
3.  Sincroniza el proyecto con los archivos Gradle.
4.  Ejecuta la aplicación en un emulador o dispositivo físico.

## Licencia 📄

Este proyecto está bajo la Licencia MIT [LICENSE](LICENSE).
