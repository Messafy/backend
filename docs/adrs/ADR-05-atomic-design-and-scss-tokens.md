# ADR-05: Atomic Design and SCSS Design Tokens

**Date:** 2026-09-02. **Status:** Accepted with known convention gaps. **Scope:** Frontend. **Last reviewed:** 2026-09-16.

## Context

The frontend contains a public landing page, authentication UI, a note browser, an editor, navigation, and increasingly rich note interactions. Without an explicit composition and styling model, this kind of application tends to accumulate one-off components, repeated colors, inconsistent interaction states, and JSX dominated by presentational utility classes.

The product requires a distinctive dark visual system rather than a prebuilt component library. Sass is already part of the toolchain and provides compile-time composition without adding a runtime styling layer. The design decision therefore needed to cover both the hierarchy of React components and the ownership of visual constants.

## Considered alternatives

### Utility-first CSS

Tailwind CSS would provide a constrained scale and rapid composition. It would also move a large amount of visual vocabulary into JSX. The project favors semantic component markup and complex component-local states, pseudo-elements, and responsive behavior, making authored SCSS a better fit for the intended visual language.

### CSS-in-JS

Runtime or extracted CSS-in-JS could colocate styles and props closely. It would introduce another abstraction and, depending on the library, runtime behavior or build configuration. The current application does not need dynamic theming or style computation complex enough to justify that cost.

### Global plain CSS

Plain CSS would avoid preprocessing but would provide weaker tools for the reusable tokens, mixins, nesting, and component-level organization already desired. It would also make accidental selector coupling easier as the interface grows.

### SCSS with Atomic Design

SCSS keeps styling static and browser-native while adding token modules and reusable mixins. Atomic Design provides a shared language for composition: atoms express primitive controls, molecules combine primitives, organisms implement substantial regions, and pages assemble the application experience.

## Decision

Frontend components follow Atomic Design under `src/components/atoms`, `molecules`, `organisms`, and `pages`. Dependency direction moves upward: atoms do not import molecules, molecules do not import organisms, and pages compose lower levels. Components normally live in a named folder with adjacent JSX and SCSS files. This is a convention rather than a claim that every legacy component already conforms perfectly.

SCSS is the styling language. Shared design values live under `src/styles/tokens` and are imported with `@use`. The token system includes palette values, semantic surfaces, editor and popover colors, selection states, typography, durations, easing, focus behavior, button treatments, scrolling, and layout-oriented mixins. Component classes use a BEM-like vocabulary such as `note-editor__header` and `label--active` to reduce global selector ambiguity without CSS Modules.

Visual constants should become semantic tokens when they represent repeated design intent. The rule is not interpreted as a ban on every literal value: one-off geometry, local spacing, and contextual effects can remain in component styles when extracting them would create an opaque token catalog. Repeated colors, typography, motion, focus states, and interaction surfaces belong in tokens or mixins.

The application uses `react-icons` for iconography. The `motion` package remains installed but is not currently imported by application code, so animation behavior should not be described as dependent on it. Notes now come from the API; files under `src/mocks` are legacy sample data and are not part of the active state flow.

## Current implementation status

The Atomic Design dependency direction is broadly respected, and the recent SCSS consolidation introduced semantic colors and reusable mixins for repeated behavior. The old hardcoded save-button accent has been removed. The system still contains raw `rgba` values, shadows, fixed font sizes, inconsistent token import paths, and a few filename or co-location exceptions. These are known deviations to address incrementally rather than evidence that the design decision has changed.

## Consequences

The visual system can evolve through named design decisions rather than broad search-and-replace work. JSX remains focused on structure and behavior, while complex hover, focus, responsive, and decorative states stay in CSS. Component placement communicates intended reuse and makes the cost of importing a higher-order component into a lower layer visible during review.

Atomic Design can encourage components so small that they add indirection without reuse. Tokens can likewise become less understandable than literals if every isolated measurement receives a global name. The project therefore optimizes for coherent reuse rather than taxonomy for its own sake. BEM-like naming remains convention-based, so discipline and review are still required to prevent global collisions.

## References

See [Atomic Design by Brad Frost](https://atomicdesign.bradfrost.com/), the frontend token modules under `frontend/src/styles/tokens`, and [ADR-06](ADR-06-semantic-html-and-accessibility.md).
