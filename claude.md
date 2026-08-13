@AGENTS.md # Project Development Rules

## 1. Preserve Existing Project Structure

- **DO NOT change the current project structure** unless it is absolutely necessary for the requested feature or bug fix.
- Do not move, rename, delete, or reorganize existing files or folders without a clear technical reason.
- Follow the existing folder structure, naming conventions, and architectural patterns.
- Before creating a new file or folder, check whether the functionality can be implemented using the existing structure.
- Avoid introducing a new architecture, design pattern, or project organization when an existing pattern already exists.

## 2. Minimize Changes

- Make the **smallest possible changes** required to complete the task.
- Do not modify unrelated files or functionality.
- Do not refactor existing code unless the refactoring is directly required for the requested task.
- Do not rewrite working code just because there is a different or newer approach.
- Preserve existing behavior unless the task explicitly requires changing it.

## 3. Follow Existing Coding Patterns

- Inspect the existing code before making changes.
- Follow the project's existing:
  - Naming conventions
  - Component patterns
  - API/service patterns
  - State management patterns
  - Styling conventions
  - Error-handling patterns
  - TypeScript patterns
  - Validation patterns

- Reuse existing components, utilities, hooks, services, constants, and types whenever possible.
- Do not introduce duplicate functionality.

## 4. Dependencies

- **DO NOT install new packages** unless the requested functionality genuinely requires a dependency that is not already available.
- Before adding a dependency, check whether the project already has a suitable library or existing implementation.
- Do not upgrade or downgrade existing dependencies unless explicitly required.
- Do not modify `package.json` or lock files unnecessarily.

## 5. Existing Functionality

- Treat existing functionality as potentially intentional.
- Do not remove existing features, validations, API calls, components, or logic unless the task explicitly requires it.
- Ensure that changes do not break existing functionality.
- Consider backward compatibility when modifying shared components, APIs, hooks, or utilities.

## 6. UI/UX

- Preserve the existing UI design and user experience unless the task specifically requests UI changes.
- Reuse existing components and styles.
- Do not introduce a new design system or styling approach.
- Maintain existing responsive behavior and accessibility patterns.

## 7. API and Backend Integration

- Follow the existing API integration pattern.
- Do not create a new API layer if an existing service/API pattern is already being used.
- Do not change API request or response structures unless required by the task.
- Use existing types/interfaces where available.
- Handle loading, success, and error states consistently with the existing project.

## 8. Before Making Changes

Before modifying the code:

1. Inspect the relevant files.
2. Understand the existing implementation.
3. Identify reusable code.
4. Determine the minimum required changes.
5. Check for dependencies between the affected files.
6. Implement the change without unnecessarily affecting unrelated functionality.

## 9. After Making Changes

- Review all modified files.
- Check for TypeScript/compile errors.
- Check for lint errors where applicable.
- Ensure existing functionality remains intact.
- Do not leave unused imports, variables, files, or dependencies.
- Do not make additional "cleanup" changes unrelated to the task.

## 10. Important Rule

> **The existing project structure and architecture are the source of truth.**

When implementing a new feature, **adapt the feature to the existing project rather than adapting the project to the feature**.

If there are multiple valid approaches, always prefer the one that:

1. Requires the fewest changes.
2. Reuses existing code.
3. Follows existing project conventions.
4. Minimizes risk of breaking existing functionality.
5. Does not change the current project structure.

### Absolute Restrictions

Unless explicitly requested by the user, DO NOT:

- Reorganize folders.
- Rename existing files.
- Move files.
- Delete existing functionality.
- Replace the existing architecture.
- Replace existing libraries/frameworks.
- Upgrade dependencies.
- Introduce unnecessary dependencies.
- Perform unrelated refactoring.
- Change existing API contracts.
- Change the application's overall design pattern.
