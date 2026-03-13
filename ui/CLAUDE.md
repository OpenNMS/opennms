# OpenNMS UI — Claude Instructions

## Overview

Vue 3 SPA embedded within OpenNMS (Java-based network monitoring platform). Two separate Vite applications share the same source tree:

1. **Main SPA** (`src/main/`) — full-featured app served at `/opennms/ui`
2. **Menu component** (`src/menu/`) — embeds into legacy JSP/Vaadin pages at `/opennms/ui-components`

**Package manager:** pnpm (required, enforced by preinstall hook)
**Node:** 18+ (22+ recommended)

---

## Commands

```bash
# Development
pnpm dev                  # Vite dev server
pnpm test                 # Run unit tests (vitest)
pnpm test:watch           # Tests in watch mode
pnpm lint                 # Check for lint errors
pnpm lint:fix             # Auto-fix lint errors
pnpm format               # Format with Prettier

# Building
pnpm run build:all        # Build both main + menu (minified)
pnpm run build:dev:all    # Build both non-minified (for debugging)
pnpm run build            # Build main SPA only
pnpm run build:menu       # Build menu component only
```

All build scripts run `vue-tsc --noEmit` to type-check first.

---

## Architecture

### Project Structure

```
src/
├── main/           # Main SPA entry (main.ts, App.vue, router/)
├── menu/           # Menu component entry
├── components/     # Feature components (organized by area, e.g. SnmpConfiguration/)
│   └── Common/     # Shared reusable components
├── containers/     # Page-level route components
├── stores/         # Pinia stores (one per feature area)
├── services/       # API service functions + axiosInstances.ts
├── composables/    # Shared reactive logic (useSnackbar, useSpinner, useRole, etc.)
├── types/          # TypeScript type definitions
├── lib/            # Pure utility functions and constants
└── styles/         # Global SCSS styles

tests/              # Vitest unit tests mirroring src/ structure
```

### Service Layer

Three pre-configured axios instances in `services/axiosInstances.ts`:
- `v2` — REST API v2 (`/opennms/api/v2`)
- `rest` — Legacy REST API (`/opennms/rest`)
- `restFile` — Multipart file uploads

Services return the response data on success or `false` on error. Always check `if (resp)` before using a response. All service functions are aggregated and re-exported via `services/index.ts`.

```typescript
// services/exampleService.ts
import { v2 } from './axiosInstances'

export const getItems = async (): Promise<Item[] | false> => {
  try {
    const resp = await v2.get('/items')
    return resp.data
  } catch (err) {
    console.error(err)
    return false
  }
}
```

### Pinia Stores

Use the Composition API setup pattern — never the Options API:

```typescript
export const useExampleStore = defineStore('exampleStore', () => {
  const items = ref<Item[]>([])
  const isLoading = ref(false)

  const fetchItems = async () => {
    isLoading.value = true
    try {
      const resp = await getItems()
      if (resp) {
        items.value = resp
      }
    } finally {
      isLoading.value = false
    }
  }

  return { items, isLoading, fetchItems }
})
```

When binding a store ref to a component's `v-model`, use a computed with getter/setter rather than binding to the store ref directly:

```typescript
// Correct — routes mutations through the store action
const activeTab = computed({
  get: () => store.activeTab,
  set: (val) => store.setActiveTab(val)
})

// Wrong — bypasses the store action
// v-model="store.activeTab"
```

### Components

All components use `<script setup lang="ts">`. Never use the Options API.

```vue
<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import useSnackbar from '@/composables/useSnackbar'

const props = defineProps<{
  label: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  action: []
}>()

const { showSnackBar } = useSnackbar()

const handleClick = () => {
  if (props.disabled) {
    return
  }

  emit('action')
}
</script>
```

Use `data-test` attributes on interactive elements for test targeting.

### Auto-Imports

Vue, Vue Router, and VueUse composables are auto-imported — no import statement needed for:
- `ref`, `computed`, `reactive`, `watch`, `watchEffect`, `onMounted`, etc.
- `useRouter`, `useRoute`
- VueUse helpers

**Custom composables must be imported manually:**
```typescript
import useSnackbar from '@/composables/useSnackbar'
import useSpinner from '@/composables/useSpinner'
import useRole from '@/composables/useRole'
import useDownload from '@/composables/useDownload'
```

### UI Components

All UI components come from the Feather Design System (`@featherds/*` v0.12.43). Do not introduce other component libraries. Key packages:
- `@featherds/button` — `FeatherButton` (`primary` / `secondary` props)
- `@featherds/input` — `FeatherInput`, `FeatherSelect`, `FeatherCheckbox`
- `@featherds/table` — `FeatherSortHeader`, `SORT`
- `@featherds/tabs` — `FeatherTabContainer`, `FeatherTab`, `FeatherTabPanel`
- `@featherds/dialog` — `FeatherDialog`
- `@featherds/dropdown` — `FeatherDropdown`, `FeatherDropdownItem`
- `@featherds/pagination` — `FeatherPagination`
- `@featherds/badge` — `FeatherTextBadge`
- `@featherds/icon` — `FeatherIcon` + individual icon imports

### Role-Based Access Control

Use the `useRole` composable to gate UI and routes:

```typescript
const { adminRole, rolesAreLoaded } = useRole()
```

---

## Testing

**Framework:** Vitest + Vue Test Utils + happy-dom
**Test location:** `tests/` — mirrors `src/` structure

```typescript
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { describe, it, expect, beforeEach, vi } from 'vitest'

describe('MyComponent', () => {
  let wrapper: any

  beforeEach(() => {
    wrapper = mount(MyComponent, {
      global: {
        plugins: [createTestingPinia({ stubActions: false })],
        stubs: ['router-link']
      }
    })
  })

  it('does something', async () => {
    await wrapper.find('[data-test="my-button"]').trigger('click')
    expect(wrapper.text()).toContain('Expected')
  })
})
```

For store-only tests, use `setActivePinia(createPinia())` in `beforeEach`. Avoid `vi.useFakeTimers()` in `beforeEach` at the describe level — scope fake timers to individual tests that need them to avoid CI failures.

---

## Environment Configuration

`.env` files live under `src/main/` (Vite `envDir` setting):
- `VITE_BASE_V2_URL` — REST v2 endpoint (default: `/opennms/api/v2`)
- `VITE_BASE_REST_URL` — Legacy REST endpoint (default: `/opennms/rest`)
- `VITE_BASE_URL` — Absolute base URL (default: `http://localhost:8980`)
- `VITE_APP_LOGO_NAME` — Product logo component name (e.g. `LogoHorizon`)

---

## Deployment

After building, copy assets to your local OpenNMS instance manually:

```bash
# Main SPA
cp ui/src/main/dist/assets/*.* <opennms>/jetty-webapps/opennms/ui/assets/
cp ui/src/main/dist/index.html <opennms>/jetty-webapps/opennms/ui/

# Menu component
cp ui/src/menu/dist-menu/assets/*.* <opennms>/jetty-webapps/opennms/ui-components/assets/
cp ui/src/menu/dist-menu/index.html <opennms>/jetty-webapps/opennms/ui-components/
```

The Java `SpaRoutingFilter` serves `index.html` for non-asset URLs. Vue Router uses `createWebHashHistory('/opennms/ui')`.

---

## UI Coding Guidelines

### Braces and formatting

Always use braces around `if`/`else`/`for`/`while` blocks, even single-line ones, with the body on its own line. This is enforced by ESLint (`curly: all`).

```ts
// correct
if (!value) {
  return []
}

// wrong
if (!value) return []
if (!value) { return [] }
```
