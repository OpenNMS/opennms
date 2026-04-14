# OpenNMS Vue 3 UI - AI Coding Assistant Instructions

## Architecture Overview

This is a Vue 3 SPA embedded within OpenNMS (a Java-based network monitoring platform). The UI consists of **two separate Vite applications**:
1. **Main SPA** (`src/main/`) - Full-featured application at `/opennms/ui`
2. **Menu component** (`src/menu/`) - Embeds into legacy JSP/Vaadin pages at `/opennms/ui-components`

Both apps share services, components, stores, and composables but have separate entry points and build configs.

## Critical Build & Deploy Workflow

**Package Manager:** pnpm (required, enforced by preinstall hook)  
**Node:** 18+ (22+ recommended)

### Development Cycle
```bash
pnpm install                    # After dependency changes
pnpm run build:all              # Builds both main + menu apps
pnpm run build:dev:all          # Non-minified for debugging
pnpm test                       # Run vitest unit tests
pnpm lint --fix                 # Auto-fix linting issues
```

### Fast Deploy to Local OpenNMS Instance
The built assets must be manually copied to the running OpenNMS instance:
```bash
# Main SPA
cd ~/projects/opennms/target/opennms-XX.X.X-SNAPSHOT/jetty-webapps/opennms/ui
cp ~/projects/opennms/ui/src/main/dist/assets/*.* assets
cp ~/projects/opennms/ui/src/main/dist/index.html .

# Menu component
cd ~/projects/opennms/target/opennms-XX.X.X-SNAPSHOT/jetty-webapps/opennms/ui-components
cp ~/projects/opennms/ui/src/menu/dist-menu/assets/*.* assets
cp ~/projects/opennms/ui/src/menu/dist-menu/index.html .
```

**Routing:** The Java `SpaRoutingFilter` serves `index.html` for non-asset URLs. The app uses Vue Router with `createWebHashHistory('/opennms/ui')`.

## Project-Specific Patterns

### Component Structure: `<script setup>` Only
All Vue components use Composition API with `<script setup>` and TypeScript:
```vue
<script setup lang="ts">
import { FeatherButton } from '@featherds/button'

const props = defineProps<{
  definition: SnmpDefinition
}>()

const emit = defineEmits<{
  save: [definition: SnmpDefinition]
}>()
</script>
```

### State Management: Pinia Composition API
Stores use Pinia's setup store pattern, **not Options API**:
```typescript
// stores/exampleStore.ts
import { defineStore } from 'pinia'

export const useExampleStore = defineStore('exampleStore', () => {
  const data = ref<MyType[]>([])
  
  const fetchData = async () => {
    const resp = await API.getData()
    if (resp) data.value = resp
  }

  return { data, fetchData }
})
```

### Service Layer Architecture
Services use pre-configured axios instances from `services/axiosInstances.ts`:
- `v2` - OpenNMS REST API v2 (`/opennms/api/v2`)
- `rest` - Legacy REST API (`/opennms/rest`)
- `restFile` - Multipart file uploads

All services export individual functions, aggregated in `services/index.ts`:
```typescript
// services/exampleService.ts
import { v2 } from './axiosInstances'

export const getItems = async (): Promise<Item[]> => {
  const resp = await v2.get('/items')
  return resp.data
}

// services/index.ts - Central export point
import { getItems } from './exampleService'
export default {
  getItems,
  // ... all other service methods
}
```

### Auto-Imported Composables
Vue, Vue Router, and VueUse composables are auto-imported via `unplugin-auto-import`:
- `ref`, `computed`, `watch`, `onMounted` - No imports needed
- `useRouter`, `useRoute` - Available globally
- VueUse helpers (`isDefined`, `whenever`, etc.)

**Custom composables must be manually imported:**
```typescript
import useSnackbar from '@/composables/useSnackbar'
import useSpinner from '@/composables/useSpinner'
import useRole from '@/composables/useRole'
```

### Composable Patterns
Composables provide shared, non-reactive functionality:
```typescript
// composables/useSnackbar.ts - Global state via module-level refs
const isDisplayed = ref(false)
const message = ref('')

const useSnackbar = () => {
  const showSnackBar = (props: SnackbarProps) => {
    isDisplayed.value = true
    message.value = props.msg
  }
  return { showSnackBar, isDisplayed, message }
}
```

### UI Components: FeatherDS Design System
All UI components come from `@featherds/*` packages (v0.12.43):
- Buttons: `FeatherButton` with `primary`/`secondary` props
- Forms: `FeatherInput`, `FeatherSelect`, `FeatherCheckbox`
- Layout: `FeatherAppLayout`, `FeatherExpansionPanel`
- Typography: Use CSS classes `.headline3`, `.headline4`, `.subtitle1`

Custom elements (e.g., `<rapi-doc>`) must be registered in `vite.config.ts` under `vue.template.compilerOptions.isCustomElement`.

### Role-Based Access Control
Routes and UI elements check roles via `useRole` composable:
```typescript
const { adminRole, filesystemEditorRole, rolesAreLoaded } = useRole()

// In route guards
beforeEnter: (to, from) => {
  if (rolesAreLoaded.value && !filesystemEditorRole.value) {
    showSnackBar({ msg: 'No role access' })
    return false
  }
}
```

### Testing with Vitest + Happy-DOM
Tests use Vitest with `@vue/test-utils` and `@pinia/testing`:
```typescript
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { describe, expect, test, beforeEach } from 'vitest'

describe('Component test', () => {
  let wrapper: any

  beforeEach(() => {
    wrapper = mount(Component, {
      global: {
        plugins: [createTestingPinia({ stubActions: false })],
        stubs: ['router-link']
      }
    })
  })

  test('behavior description', async () => {
    await wrapper.find('[data-test="button-id"]').trigger('click')
    expect(wrapper.text()).toContain('Expected text')
  })
})
```

## Environment Configuration

Configuration via `.env` files (relative to `src/main/` due to Vite `envDir` setting):
- `VITE_BASE_V2_URL` - REST v2 API endpoint (default: `/opennms/api/v2`)
- `VITE_BASE_REST_URL` - Legacy REST endpoint (default: `/opennms/rest`)
- `VITE_BASE_URL` - Absolute base URL (default: `http://localhost:8980`)
- `VITE_APP_LOGO_NAME` - Product logo component name (e.g., `LogoHorizon`)

Logo aliasing in `vite.config.ts` allows product-specific branding:
```typescript
'./src/assets/ProductLogo.vue': `./src/assets/${process.env.VITE_APP_LOGO_NAME}.vue`
```

## Key Directories

- `src/components/` - Reusable Vue components (organized by feature)
- `src/containers/` - Top-level page components (route targets)
- `src/composables/` - Shared reactive logic and utilities
- `src/stores/` - Pinia state management stores
- `src/services/` - Backend API integration layer
- `src/types/` - TypeScript type definitions
- `tests/` - Vitest unit tests mirroring `src/` structure

## Debugging Tips

- **Browser DevTools F12** preferred over IDE debugging
- Use `console.log()` and `console.dir()` liberally for inspection
- Check Pinia stores via Vue DevTools
- Test role access by checking `useAuthStore().whoAmI.roles`
- Manually clear old assets: `rm assets/*.*` in deployment directories
