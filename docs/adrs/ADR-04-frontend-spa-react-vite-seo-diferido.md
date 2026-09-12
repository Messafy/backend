# ADR-04: Frontend como SPA con React 19 + Vite, sin SSR, con SEO diferido

- **Fecha:** 2026-09-02 (registro en ADR: 2026-09-08)
- **Estado:** Aceptada
- **Ámbito:** Frontend

## Contexto

Ghost Notes es, en esencia, una aplicación privada: las vistas principales (notas, editor, sidebar) viven detrás de autenticación y no tienen valor para un buscador. Sin embargo, el producto eventualmente necesitará una cara pública (landing, registro) que sí podría querer posicionarse.

Hay que elegir el stack y, sobre todo, la estrategia de renderizado, porque de ella depende la arquitectura: una SPA pura (Vite) entrega HTML casi vacío y renderiza en cliente; los frameworks con SSR (Next.js, Remix) entregan HTML con contenido pero imponen su propia estructura de proyecto.

## Alternativas consideradas

### Alternativa A: Next.js (App Router) con SSR/SSG

- **A favor:** SEO de primera clase desde el día uno; ecosistema enorme; renderizado híbrido por ruta.
- **En contra:** para una app privada de notas introduce complejidad y convenciones que no se aprovechan (RSC, server actions, cacheo de framework); el modelo mental se aleja del React puro; el backend propio (Spring) hace que casi ninguna capacidad de Next se use.

### Alternativa B: Astro para la parte pública + SPA para la app

- **A favor:** SEO impecable en la landing, cero JS innecesario.
- **En contra:** dos proyectos frontend que mantener en fase temprana; la app privada (la mayoría del trabajo actual) seguiría siendo React puro de todas formas.

### Alternativa C: SPA con React 19 + Vite (elegida)

Vite como bundler/dev server, react-router-dom v7 para el enrutado, sin SSR. Las decisiones de SEO se toman conscientemente:

- Meta tags correctos en `index.html` (description, robots, Open Graph, theme-color, canonical).
- HTML semántico y accesible (ver ADR-06) para que el contenido, cuando exista en páginas públicas, sea indexable.
- **El SEO completo (prerender de la landing, robots.txt, sitemap.xml, JSON-LD, dominio real) se difiere al final del proyecto**, cuando exista contenido público real que posicionar.
- Si el SEO llegara a ser crítico, las opciones evaluadas para entonces son prerrenderizado (`vite-plugin-prerender`) o una landing estática separada montada en la raíz del dominio, sin reescribir la app.

## Decisión

1. SPA con **React 19** (JSX, sin TypeScript en esta fase), **Vite** y **react-router-dom v7**.
2. No se adopta SSR en ninguna parte del proyecto actual.
3. El trabajo SEO se **difiere al final del proyecto**, con las decisiones ya tomadas para ese momento: prerender estático de la landing o landing separada; nunca migración a Next.
4. Rutas privadas protegidas en el router (`requireAuth`/`requireGuest`) de forma que el contenido sensible nunca se renderice sin sesión.

## Consecuencias

### Positivas

- Tooling minimalista y DX excelente (HMR instantáneo); el proyecto es 100% React estándar, sin capas de framework por encima.
- La app privada no gana nada con SSR: no hay contenido que indexar ni primer render de servidor que optimizar detrás del login.
- Evita la migración más cara y probable del proyecto (Vite → Next a mitad de camino), decidiendo ya que no ocurrirá.
- El SEO final queda reducido a tareas cosméticas de última semana (assets, sitemap, og:image) gracias a que la semántica se cuida desde el principio.

### Negativas / riesgos

- Las previews al compartir URLs (og:image en redes/Slack) no funcionarán bien hasta el prerender: aceptado, no hay tráfico que perder en esta fase.
- El primer render depende de la ejecución de JS: aceptable para una app autenticada; deberá reevaluarse solo si nace un producto público con contenido relevante.
- Riesgo de "olvidar" el SEO diferido: mitigado porque este ADR documenta la tarea y su plan de ejecución.

## Referencias

- Vite — https://vite.dev/
- react-router-dom v7 — https://reactrouter.com/
- ADR-06 (HTML semántico y accesibilidad), del que depende la indexabilidad futura.
