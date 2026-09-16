# ADR-06: Semantic HTML and Accessibility as a Frontend Standard

**Date:** 2026-09-08. **Status:** Accepted and partially implemented. **Scope:** Frontend. **Last reviewed:** 2026-09-16.

## Context

An early review of the login, note list, editor, and navigation found markup that worked visually but did not always communicate equivalent meaning to browsers, keyboards, or assistive technology. Clickable generic elements, controls identified only by placeholders, unnamed regions, and headings selected for appearance rather than document hierarchy are inexpensive during prototyping and expensive when they become copied conventions.

Accessibility is treated as a baseline quality attribute rather than a final polish pass. It also supports the deferred public SEO work described by [ADR-04](ADR-04-react-vite-spa-and-deferred-seo.md), although search visibility is not the reason accessible markup matters. Native semantics reduce custom keyboard and ARIA code and usually produce a more robust interface for every user.

## Considered alternatives

### Correct accessibility only when a screen is revisited

Incremental correction would have no immediate refactoring cost, but defective patterns would continue to spread. The eventual audit would be larger, and components shared across pages would encode the wrong semantics for longer.

### Adopt a headless accessible component library

Libraries such as Radix or Headless UI provide carefully implemented behavior for dialogs, menus, comboboxes, and other difficult widgets. Most current controls are simpler and can be represented with native HTML. Adopting a library for basic inputs and buttons would couple the design system before its more advanced primitives are needed. A headless library remains an appropriate future choice for complex interactions.

### Establish semantic rules and repair the active component tree

A structured review can improve the current UI and define expectations for subsequent components. Native HTML should be preferred, ARIA should supply names or state only where semantics do not already do so, and visible labels should remain the default for form controls.

## Decision

Interactive behavior should use native `button`, `a`, input, and form elements whenever those elements match the action. Application regions should use meaningful landmarks such as `main`, `nav`, `aside`, `header`, `section`, and `article`, with accessible names where a region requires one. Heading levels describe the document outline; visual size remains the responsibility of styling components.

Forms use appropriate input types, autocomplete hints, visible or programmatically associated labels, and explicit error relationships. Decorative elements are hidden from the accessibility tree. Dates should use `time` when the value represents a machine-readable date, and links that open a new browsing context must use a safe `rel` value.

ARIA is not a substitute for native semantics. An `aria-label` is appropriate when a control has no visible text, such as an icon-only pin button. It should not be added indiscriminately to every section or control when native text and association already provide a better accessible name.

## Current implementation status

The login fields have accessible labels and error associations. The note title and body controls are named. The sidebar uses `aside` and `nav`, note cards use `article`, card dates use `time`, and decorative preview elements are hidden from assistive technology. External links opened in a new tab receive `rel="noreferrer"`.

The implementation is not complete. Note cards are clickable articles without equivalent keyboard activation, the tag editor backdrop is a clickable generic element, the public hero contains an unnamed section, the notes page does not currently expose a top-level `h1`, and the editor's full creation date is rendered as text rather than `time`. The application should therefore not claim full keyboard or screen-reader conformance yet.

There is no automated accessibility linting, axe-based test suite, or Lighthouse gate. Accessibility remains a review discipline supported by native patterns. Introducing `eslint-plugin-jsx-a11y` and periodic automated browser audits is the next practical enforcement step.

## Consequences

Semantic components are easier to operate with keyboards and assistive technology and are less dependent on custom event code. Public content also gains a stronger document structure. The rules are local enough to apply during ordinary component review rather than through a separate late-stage remediation project.

Some interactions will require more than native elements, particularly dialogs, menus, drag-and-drop behavior, and composite widgets. At that point the team must follow established ARIA authoring patterns or adopt a specialized primitive library. Accessibility remains an ongoing product property; an accepted ADR and one refactoring commit do not constitute conformance.

## References

See the [WAI-ARIA Authoring Practices Guide](https://www.w3.org/WAI/ARIA/apg/), [ADR-04](ADR-04-react-vite-spa-and-deferred-seo.md), [ADR-05](ADR-05-atomic-design-and-scss-tokens.md), and commit `f2927a0 fix(a11y): improve semantic HTML and accessibility` in the frontend repository.
