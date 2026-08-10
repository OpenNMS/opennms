import { includeIgnoreFile } from '@eslint/compat'
import eslint from '@eslint/js'
import stylistic from '@stylistic/eslint-plugin'
import globals from 'globals'
import { fileURLToPath } from 'url'
import path from 'path'
import pluginVue from 'eslint-plugin-vue'
import tseslint from 'typescript-eslint'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

export default tseslint.config(
  // Replaces --ignore-path ../.gitignore on the CLI
  includeIgnoreFile(path.resolve(__dirname, '../.gitignore')),
  { ignores: ['dist/**', 'src/main/dist/**', 'src/menu/dist-menu/**', 'packages/onms-ui-example-plugin/dist/**', '**/*.d.ts', 'scripts/**'] },

  // Base recommended rule sets
  eslint.configs.recommended,
  ...tseslint.configs.recommended,
  ...pluginVue.configs['flat/essential'],

  // Tell vue-eslint-parser (loaded by eslint-plugin-vue) to use @typescript-eslint/parser
  // for the <script> block inside .vue files
  {
    files: ['**/*.vue'],
    languageOptions: {
      parserOptions: {
        parser: tseslint.parser,
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
    },
  },

  // Shared rules for .ts and .vue files
  {
    files: ['**/*.ts', '**/*.vue'],
    plugins: {
      '@stylistic': stylistic,
    },
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.es2025,
        // Vue 3 <script setup> macros
        defineProps: 'readonly',
        defineEmits: 'readonly',
        defineExpose: 'readonly',
        withDefaults: 'readonly',
      },
    },
    rules: {
      // ── Stylistic formatting (replaces Prettier) ──────────────────────────
      '@stylistic/arrow-parens': ['error', 'as-needed', { requireForBlockBody: true }],
      '@stylistic/block-spacing': ['error', 'always'],
      '@stylistic/brace-style': ['error', '1tbs'],
      '@stylistic/comma-dangle': ['error', 'never'],
      '@stylistic/comma-spacing': ['error', { before: false, after: true }],
      '@stylistic/eol-last': ['error', 'always'],
      '@stylistic/indent': ['error', 2, { SwitchCase: 1 }],
      '@stylistic/keyword-spacing': ['error', { after: true, before: true }],
      '@stylistic/no-multiple-empty-lines': ['error', { max: 2, maxEOF: 0 }],
      '@stylistic/no-trailing-spaces': ['error'],
      '@stylistic/object-curly-spacing': ['error', 'always', { objectsInObjects: false, emptyObjects: 'never' }],
      '@stylistic/quotes': ['error', 'single'],
      '@stylistic/semi': ['error', 'never'],
      '@stylistic/space-before-blocks': ['error', 'always'],
      '@stylistic/space-before-function-paren': ['error', { anonymous: 'always', named: 'never', asyncArrow: 'always', catch: 'always' }],

      // ── Vue ───────────────────────────────────────────────────────────────
      'vue/html-quotes': ['error', 'double', { avoidEscape: false }],
      'vue/multi-word-component-names': 'off',

      // ── TypeScript ────────────────────────────────────────────────────────
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-unused-vars': ['error', {
        varsIgnorePattern: '^_',
        argsIgnorePattern: '^_',
        caughtErrorsIgnorePattern: '^_'
      }],

      // ── General ───────────────────────────────────────────────────────────
      curly: ['error', 'all'],
    },
  },

  // ── Seam layer ────────────────────────────────────────────────────────────
  // Direct PrimeVue imports are banned in app code once an Onms- wrapper
  // exists. The @opennms/onms-ui package itself (packages/onms-ui) and tests
  // are exempt. Entries are appended as each wrapper lands.
  {
    files: ['src/**/*.ts', 'src/**/*.vue'],
    rules: {
      'no-restricted-imports': ['error', {
        paths: [
          { name: 'primevue/autocomplete', message: 'Use OnmsAutoComplete from @opennms/onms-ui.' },
          { name: 'primevue/button', message: 'Use OnmsButton / OnmsIconButton from @opennms/onms-ui.' },
          { name: 'primevue/card', message: 'Use OnmsCard from @opennms/onms-ui.' },
          { name: 'primevue/checkbox', message: 'Use OnmsCheckbox from @opennms/onms-ui.' },
          { name: 'primevue/chip', message: 'Use OnmsChip from @opennms/onms-ui.' },
          { name: 'primevue/column', message: 'Use OnmsColumn from @opennms/onms-ui.' },
          { name: 'primevue/datatable', message: 'Use OnmsTable from @opennms/onms-ui.' },
          { name: 'primevue/datepicker', message: 'Use OnmsDatePicker from @opennms/onms-ui.' },
          { name: 'primevue/dialog', message: 'Use OnmsDialog from @opennms/onms-ui.' },
          { name: 'primevue/drawer', message: 'Use OnmsDrawer from @opennms/onms-ui.' },
          { name: 'primevue/iconfield', message: 'Use OnmsSearchInput from @opennms/onms-ui.' },
          { name: 'primevue/inputicon', message: 'Use OnmsSearchInput from @opennms/onms-ui.' },
          { name: 'primevue/inputnumber', message: 'Use OnmsInputNumber from @opennms/onms-ui.' },
          { name: 'primevue/inputtext', message: 'Use OnmsInputText from @opennms/onms-ui.' },
          { name: 'primevue/listbox', message: 'Use OnmsListbox from @opennms/onms-ui.' },
          { name: 'primevue/menu', message: 'Use OnmsMenu from @opennms/onms-ui.' },
          { name: 'primevue/menuitem', message: 'Use the OnmsMenuItem type from @opennms/onms-ui.' },
          { name: 'primevue/multiselect', message: 'Use OnmsMultiSelect from @opennms/onms-ui.' },
          { name: 'primevue/panel', message: 'Use OnmsPanel from @opennms/onms-ui.' },
          { name: 'primevue/password', message: 'Use OnmsPassword from @opennms/onms-ui.' },
          { name: 'primevue/popover', message: 'Use OnmsPopover from @opennms/onms-ui.' },
          { name: 'primevue/progressspinner', message: 'Use OnmsSpinner from @opennms/onms-ui.' },
          { name: 'primevue/radiobutton', message: 'Use OnmsRadioButton from @opennms/onms-ui.' },
          { name: 'primevue/select', message: 'Use OnmsSelect from @opennms/onms-ui.' },
          { name: 'primevue/tab', message: 'Use OnmsTab from @opennms/onms-ui.' },
          { name: 'primevue/tablist', message: 'Use OnmsTabList from @opennms/onms-ui.' },
          { name: 'primevue/tabpanel', message: 'Use OnmsTabPanel from @opennms/onms-ui.' },
          { name: 'primevue/tabpanels', message: 'Use OnmsTabPanels from @opennms/onms-ui.' },
          { name: 'primevue/tabs', message: 'Use OnmsTabs from @opennms/onms-ui.' },
          { name: 'primevue/tag', message: 'Use OnmsTag from @opennms/onms-ui.' },
          { name: 'primevue/textarea', message: 'Use OnmsTextarea from @opennms/onms-ui.' },
          { name: 'primevue/tieredmenu', message: 'No seam wrapper yet — SideMenu.vue is the only sanctioned use (inline-disabled). Talk to the UI team.' },
          { name: 'primevue/toast', message: 'Use OnmsToastHost / useOnmsToast from @opennms/onms-ui.' },
          { name: 'primevue/toasteventbus', message: 'Use useOnmsToast from @opennms/onms-ui.' },
          { name: 'primevue/toggleswitch', message: 'Use OnmsToggleSwitch from @opennms/onms-ui.' },
          { name: 'primevue/tooltip', message: 'Use the OnmsTooltip directive from @opennms/onms-ui, registered as v-onms-tooltip.' }
        ]
      }]
    }
  },

  // ── Plugin contract ───────────────────────────────────────────────────────
  // Plugins compile against the host's window globals (vue, pinia,
  // vue-router, @opennms/onms-ui). PrimeVue is not one of them, so ANY
  // primevue import in a plugin either bloats the bundle or breaks at
  // runtime — blanket ban, unlike the per-wrapper list above.
  {
    files: ['packages/onms-ui-example-plugin/src/**/*.ts', 'packages/onms-ui-example-plugin/src/**/*.vue'],
    rules: {
      'no-restricted-imports': ['error', {
        patterns: [
          { group: ['primevue', 'primevue/*'], message: 'Plugins must use @opennms/onms-ui — PrimeVue is not part of the host plugin contract.' }
        ]
      }]
    }
  }
)
