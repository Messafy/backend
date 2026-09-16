# ADR-04: React and Vite SPA with Deferred SEO

**Date:** 2026-09-02. **Status:** Accepted; SEO work is partially implemented. **Scope:** Frontend. **Last reviewed:** 2026-09-16.

## Context

Most product value lives in an authenticated note workspace whose contents must be protected by the backend and have no search-indexing value. The product also has a public landing page and login flow, so some public metadata matters, but this surface does not currently justify a server-rendering platform around a Spring API.

The render strategy influences the entire frontend architecture. A client-rendered SPA preserves standard React and a small deployment model. A server-rendered framework can produce route-specific HTML and metadata, but it would introduce another server runtime and framework conventions while leaving the existing Spring backend responsible for the actual application API.

## Considered alternatives

### Next.js with server-side and static rendering

Next.js would provide route-level metadata, pre-rendering, and hybrid rendering. Those capabilities are valuable for a content-heavy public product. In this application, most routes are private, React Server Components would not replace the Spring backend, and adopting the framework would add a second server model before there is enough public content to justify it.

### A separate static public site

Astro or a small static site could optimize the landing experience while preserving the private SPA. This would create two frontend projects, duplicated design concerns, and separate deployment workflows during an early stage. It remains a practical option if the public product becomes independently substantial.

### React SPA built with Vite

React with Vite provides a fast development loop, a conventional browser application, and no rendering infrastructure beyond static assets. Search and social metadata can be improved incrementally or by pre-rendering selected public routes later.

## Decision

The frontend is a client-rendered SPA using React 19, Vite 8, react-router-dom 7, JSX, Sass, and PropTypes. It does not use TypeScript or server-side rendering. `/` is a public landing page, `/login` is guest-oriented, and `/notes` is protected by a client route guard. The route guard improves navigation behavior but is not considered a security boundary; the backend remains responsible for data confidentiality and authorization.

The static `index.html` contains a description, keywords, robots directive, author, theme color, and partial Open Graph metadata. It does not currently provide a canonical URL, Open Graph image or URL, Twitter card metadata, structured data, route-specific metadata, `robots.txt`, or `sitemap.xml`. The global `index, follow` directive is not route-aware.

SEO investment is deferred until public content and a real deployment domain make it measurable. The likely path is targeted pre-rendering or a separately deployed public landing surface, not an automatic full rewrite. The choice must be reevaluated if public, indexable content becomes a primary product capability.

## Consequences

The private application benefits from a small toolchain, fast hot module replacement, and direct use of the React ecosystem. It avoids coupling UI development to a framework whose server facilities would overlap with, rather than replace, Spring Boot.

The initial HTML is not route-specific and depends on JavaScript for meaningful page content. Search engines and social previews receive only static metadata, and private routes are not excluded at the document metadata level. Accessibility and semantic markup improve both usability and future indexability, but they do not substitute for canonicalization, crawl policy, structured metadata, or pre-rendering.

The decision intentionally leaves a reevaluation point rather than prohibiting all future framework changes. A shift in product shape, not a preference for fashionable tooling, should trigger that review.

## References

See [Vite](https://vite.dev/), [React Router](https://reactrouter.com/), and [ADR-06](ADR-06-semantic-html-and-accessibility.md).
