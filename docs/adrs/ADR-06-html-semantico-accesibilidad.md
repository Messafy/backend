# ADR-06: HTML semántico y accesibilidad como estándar del frontend

- **Fecha:** 2026-09-08
- **Estado:** Aceptada
- **Ámbito:** Frontend

## Contexto

Tras implementar login, lista de notas y editor, una revisión de estructura detectó que el marcado era funcionalmente correcto pero semánticamente incompleto: elementos clicables construidos con `<div>`, inputs identificables solo por placeholder, `<section>` sin nombre accesible, jerarquía de headings inconsistente.

Dejar esto para el final era la receta clásica para no hacerlo nunca. Además, otras decisiones del proyecto dependen de la semántica:

- El SEO diferido (ADR-04) presupone HTML indexable y significativo.
- La accesibilidad es un requisito de calidad básico del producto, no una mejora opcional.

## Alternativas consideradas

### Alternativa A: Corregir a demanda, cuando toque cada pantalla

- **A favor:** cero coste ahora.
- **En contra:** deuda acumulada invisible; cada componente nuevo copia los antimientos defectuosos; el esfuerzo total es mayor que una pasada estructurada.

### Alternativa B: Introducir una librería de componentes accesibles (Radix, Headless UI)

- **A favor:** a11y de primera clase "gratis" en componentes complejos (menús, diálogos).
- **En contra:** para el inventario actual (botones, inputs, etiquetas, listas) el HTML nativo ya resuelve casi todo; adoptar una librería ahora acoplada el sistema visual propio (ADR-05) antes de que exista la necesidad real. Queda como opción puntual para primitivas complejas futuras (combos, modales).

### Alternativa C: Pasada estructurada con reglas permanentes (elegida)

## Decisión

Se realizó una revisión completa de la UI y se corrigió todo el árbol de componentes, estableciendo además reglas permanentes para el código nuevo:

1. **Interactividad real:** todo elemento clicable es `<button>` o `<a>`. Las etiquetas del sidebar (`Label`) y acciones de sección son botones reales, enfocables y anunciados por lectores de pantalla.
2. **Nombre accesible obligatorio:** ningún `<section>`, input o control sin `aria-label` o `aria-labelledby`. Los `<section>` sin nombre equivalen a `<div>` y pierden su propósito.
3. **Landmarks completos:** un único `<main>` por página, `<nav>` para la navegación del sidebar, `<header>`/`<footer>` donde corresponde, `<aside>` para el sidebar.
4. **Jerarquía de headings:** exactamente un `h1` por página; los títulos de tarjetas usan `h3`; nunca se usa un heading por su tamaño visual (para eso existe el átomo `Text` con `size=`).
5. **Formularios:** inputs con tipo correcto (`email`, `password`, `search`), `aria-label`, y `autoComplete` semántico.
6. **Elementos decorativos** (ej. `Rain`) con `aria-hidden='true'`.
7. **Semántica de contenido:** fechas en `<time dateTime>`, notas como `<article>`, `rel='noreferrer'` automático en enlaces `target='_blank'`.

## Consecuencias

### Positivas

- La aplicación es navegable por teclado y por lectores de pantalla desde ya; los usuarios con necesidades de accesibilidad no quedan excluidos.
- El SEO futuro (ADR-04) tiene la base resuelta: landmarks, headings y marcado significativo ya existen.
- Las reglas son codificables: cualquier componente nuevo se revisa contra esta lista en minutos.

### Negativas / riesgos

- Coste puntual ya pagado (una sesión de refactor, 14 archivos), y coste marginal pequeño por componente nuevo.
- La disciplina depende de revisión manual: no hay linting de a11y en el pipeline. Mitigación futura: `eslint-plugin-jsx-a11y` y auditorías Lighthouse periódicas.
- Los componentes interactivos complejos futuros (menús contextuales, arrastrar y soltar) exigirán patrones ARIA más avanzados; en ese punto se reevaluará una librería headless.

## Referencias

- WAI-ARIA Authoring Practices — https://www.w3.org/WAI/ARIA/apg/
- ADR-04 (SEO diferido), ADR-05 (sistema de componentes).
- Commits `fix(privatenotes): Improve semantic HTML and accessibility` en `frontend/`.
