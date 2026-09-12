# ADR-05: Atomic Design con design tokens en SCSS

- **Fecha:** 2026-09-02 (registro en ADR: 2026-09-08)
- **Estado:** Aceptada
- **Ámbito:** Frontend

## Contexto

El frontend crece rápido (páginas de login, notas, editor, sidebar, componentes de autoría). Sin convenciones, los proyectos React acaban con estilos duplicados, colores hardcodeados y componentes imposibles de reutilizar. Había que elegir:

1. Una estrategia de CSS (CSS global, CSS Modules, Tailwind, CSS-in-JS, preprocesador).
2. Una metodología de composición de componentes.

Restricciones:

- Se quiere un sistema visual propio y coherente (tema oscuro tipo zinc, con acentos), no una librería de componentes pronta.
- Sass ya está en el proyecto y es una dependencia que el equipo domina.
- El proyecto usa `motion` para animaciones y `react-icons` para iconografía.

## Alternativas consideradas

### Alternativa A: Tailwind CSS

- **A favor:** velocidad de prototipado, consistencia por diseño (escala de utilidades).
- **En contra:** el marcado se llena de clases de presentación (acoplamiento visual en el JSX), choca con el objetivo de HTML semántico legible (ver ADR-06) y con el uso intensivo de animaciones custom y estados pseudo-elemento que el diseño del proyecto necesita.

### Alternativa B: CSS-in-JS (styled-components, emotion)

- **A favor:** co-localización total de estilos y lógica.
- **En contra:** coste en runtime o configuración adicional, y en React 19 el ecosistema está migrando hacia soluciones sin runtime; añade complejidad sin necesidad para este tamaño de proyecto.

### Alternativa C: CSS plano global

- **A favor:** cero dependencias.
- **En contra:** sin variables anidadas ni mixins, colisiones de nombres y duplicación a medida que crece la superficie de estilos.

### Alternativa D: SCSS + Atomic Design (elegida)

- SCSS para anidamiento, mixins y, sobre todo, **design tokens** versionados como código.
- **Atomic Design** para la composición: `atoms/` (Button, Input, Text, Tag, Icon...), `molecules/` (Card, Brand, CollectionTags...), `organisms/` (Sidebar, NoteEditor, LoginForm...), `pages/`.

## Decisión

1. **Estilos en SCSS**, un archivo `Component.scss` por componente, side-by-side con `Component.jsx` en su propia carpeta.
2. **Design tokens centralizados** en `src/styles/tokens/` (`_colors.scss`, `_typography.scss`, `_mixins.scss`, `_index.scss`). Regla dura: **ningún componente hardcodea colores o tipografías**; se importa de tokens. Excepción transitoria documentada: el acento azul del botón save (`Button.scss`) debe migrar a token.
3. **Atomic Design** como metodología de composición: un átomo no importa moléculas; una molécula no importa organismos; una página compone organismos. Datos de ejemplo en `src/mocks/`.
4. Nomenclatura BEM-like en las clases (`note-editor__header`, `label--active`) para encapsular estilos por componente sin CSS Modules.

## Consecuencias

### Positivas

- Sistema visual coherente y refactorizable: cambiar un token cambia la app entera; el `::selection` global, por ejemplo, salió gratis de los tokens.
- Componentes predecibles: la jerarquía atoms→molecules→organisms hace obvio dónde vive cada pieza nueva.
- SCSS da las herramientas (mixins como `soft-border`, `control-focus`) sin runtime ni dependencias nuevas.
- El acoplamiento visual se queda en los `.scss`, y el JSX conserva su significado semántico.

### Negativas / riesgos

- Riesgo de colisiones de nombres si se abandona la disciplina BEM; mitigado por convención y por la revisión de estructura.
- La regla de "cero hardcodeo" exige vigilancia: el acento `#284c9f` en `Button.scss` es el caso pendiente de migrar a token.
- Atomic Design puede llevar a sobre-atomización (átomos triviales); se acepta cuando el átomo se reutiliza al menos dos veces.

## Referencias

- Atomic Design (Brad Frost) — https://atomicdesign.bradfrost.com/
- Tokens del proyecto: `frontend/src/styles/tokens/`
