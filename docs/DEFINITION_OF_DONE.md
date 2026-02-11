# Definition of Done (DoD)

A backlog item is done when all relevant criteria in the following sections are met:

## 1. Code Implementation

- Code is implemented following the team’s coding standards.
- Code follows agreed naming conventions, structure, and formatting.
- Code is merged into the develop branch without conflicts.

## 2. Testing

- Unit tests are written and all pass.
- Integration tests (if applicable) pass successfully.
- End-to-End (E2E) tests (if applicable) are automated and pass successfully.
- Feature passes QA/manual testing and meets all acceptance criteria.
- Feature is integrated and tested in the integration environment.
- Automated test reports are available (unit, integration, E2E).
- Code coverage meets the team’s threshold (e.g., ≥80% for unit tests; integration and E2E coverage measured and reported).

## 3. Documentation
- API documentation is updated (Swagger/OpenAPI) with endpoint descriptions, parameters, response codes, and examples.
- Inline code comments and method-level documentation (e.g., JavaDoc) are added where necessary.
- Changelog/version history is maintained for traceability of changes.

## 4. Deployment Readiness

- The increment is potentially shippable and production-ready.