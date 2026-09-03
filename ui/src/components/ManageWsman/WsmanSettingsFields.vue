<template>
  <div class="settings-grid">
    <FormField label="Username" :for="`${idPrefix}-username`">
      <OnmsInputText :id="`${idPrefix}-username`" :modelValue="modelValue.username" fluid data-test="username-input" @update:modelValue="set('username', $event ?? '')" />
    </FormField>
    <FormField
      :label="hasPassword ? 'New password' : 'Password'"
      :for="`${idPrefix}-password`"
      :error="errors.password"
      :hint="hasPassword ? 'A password is stored. Leave this blank to keep it.' : undefined"
    >
      <OnmsPassword :inputId="`${idPrefix}-password`" :modelValue="modelValue.password" toggleMask fluid :invalid="!!errors.password" data-test="password-input" @update:modelValue="set('password', $event ?? '')" />
    </FormField>
    <label v-if="hasPassword" class="check-row" data-test="clear-password-row">
      <OnmsCheckbox :modelValue="modelValue.clearPassword" binary data-test="clear-password" @update:modelValue="set('clearPassword', $event)" />
      <span>Remove the stored password</span>
    </label>
    <FormField label="Use SSL" :for="`${idPrefix}-ssl`">
      <OnmsSelect :inputId="`${idPrefix}-ssl`" :modelValue="modelValue.ssl" :options="triOptions" optionLabel="label" optionValue="value" fluid data-test="ssl-select" @update:modelValue="set('ssl', $event as TriState)" />
    </FormField>
    <div v-if="collapsibleAdvanced" class="advanced-toggle">
      <OnmsButton
        variant="text"
        :label="advancedOpen ? 'Hide advanced settings' : 'Show advanced settings'"
        :aria-expanded="advancedOpen"
        data-test="toggle-advanced"
        @click="advancedOpen = !advancedOpen"
      />
      <span v-if="!advancedOpen && advancedSummary" class="advanced-summary" data-test="advanced-summary">{{ advancedSummary }}</span>
    </div>
    <template v-if="!collapsibleAdvanced || advancedOpen">
    <FormField label="Strict SSL" :for="`${idPrefix}-strict-ssl`">
      <OnmsSelect :inputId="`${idPrefix}-strict-ssl`" :modelValue="modelValue.strictSsl" :options="triOptions" optionLabel="label" optionValue="value" fluid data-test="strict-ssl-select" @update:modelValue="set('strictSsl', $event as TriState)" />
    </FormField>
    <FormField label="GSS authentication" :for="`${idPrefix}-gss`">
      <OnmsSelect :inputId="`${idPrefix}-gss`" :modelValue="modelValue.gssAuth" :options="triOptions" optionLabel="label" optionValue="value" fluid data-test="gss-select" @update:modelValue="set('gssAuth', $event as TriState)" />
    </FormField>
    <FormField label="Port" :for="`${idPrefix}-port`" :error="errors.port">
      <OnmsInputNumber :inputId="`${idPrefix}-port`" :modelValue="modelValue.port" :min="1" :max="65535" :useGrouping="false" :invalid="!!errors.port" fluid data-test="port-input" @update:modelValue="set('port', $event ?? null)" />
    </FormField>
    <FormField label="Path" :for="`${idPrefix}-path`" :error="errors.path">
      <OnmsInputText :id="`${idPrefix}-path`" :modelValue="modelValue.path" placeholder="/wsman" :invalid="!!errors.path" fluid data-test="path-input" @update:modelValue="set('path', $event ?? '')" />
    </FormField>
    <FormField label="Timeout (ms)" :for="`${idPrefix}-timeout`" :error="errors.timeout">
      <OnmsInputNumber :inputId="`${idPrefix}-timeout`" :modelValue="modelValue.timeout" :min="1" :useGrouping="false" :invalid="!!errors.timeout" fluid data-test="timeout-input" @update:modelValue="set('timeout', $event ?? null)" />
    </FormField>
    <FormField label="Retries" :for="`${idPrefix}-retry`" :error="errors.retry">
      <OnmsInputNumber :inputId="`${idPrefix}-retry`" :modelValue="modelValue.retry" :min="0" :useGrouping="false" :invalid="!!errors.retry" fluid data-test="retry-input" @update:modelValue="set('retry', $event ?? null)" />
    </FormField>
    <FormField label="Max elements" :for="`${idPrefix}-max-elements`" :error="errors.maxElements">
      <OnmsInputNumber :inputId="`${idPrefix}-max-elements`" :modelValue="modelValue.maxElements" :min="1" :useGrouping="false" :invalid="!!errors.maxElements" fluid data-test="max-elements-input" @update:modelValue="set('maxElements', $event ?? null)" />
    </FormField>
    <FormField label="Product vendor" :for="`${idPrefix}-vendor`">
      <OnmsInputText :id="`${idPrefix}-vendor`" :modelValue="modelValue.productVendor" fluid data-test="vendor-input" @update:modelValue="set('productVendor', $event ?? '')" />
    </FormField>
    <FormField label="Product version" :for="`${idPrefix}-version`">
      <OnmsInputText :id="`${idPrefix}-version`" :modelValue="modelValue.productVersion" fluid data-test="version-input" @update:modelValue="set('productVersion', $event ?? '')" />
    </FormField>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsButton, OnmsCheckbox, OnmsInputNumber, OnmsInputText, OnmsPassword, OnmsSelect } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { TRI_STATE_OPTIONS, TriState, WsmanSettingsForm } from './wsmanForm'

const props = defineProps<{
  modelValue: WsmanSettingsForm
  idPrefix: string
  errors: Partial<Record<keyof WsmanSettingsForm, string>>
  hasPassword: boolean
  // a definition inherits an unset value from the defaults; the root falls back to built-ins
  unsetLabel: string
  // fold everything past username, password and SSL behind a toggle
  collapsibleAdvanced?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: WsmanSettingsForm): void
}>()

const triOptions = computed(() => TRI_STATE_OPTIONS(props.unsetLabel))

const ADVANCED_KEYS: (keyof WsmanSettingsForm)[] = ['strictSsl', 'gssAuth', 'port', 'path', 'timeout', 'retry', 'maxElements', 'productVendor', 'productVersion']
const ADVANCED_LABELS: Record<string, string> = {
  strictSsl: 'Strict SSL', gssAuth: 'GSS', port: 'Port', path: 'Path', timeout: 'Timeout', retry: 'Retries',
  maxElements: 'Max elements', productVendor: 'Vendor', productVersion: 'Version'
}
const isSet = (value: unknown) => value !== null && value !== undefined && value !== '' && value !== 'unset'
const advancedSet = computed(() => ADVANCED_KEYS.filter(key => isSet(props.modelValue[key])))
const advancedSummary = computed(() =>
  advancedSet.value.length ? `Set: ${advancedSet.value.map(key => ADVANCED_LABELS[key]).join(', ')}` : ''
)
const hasAdvancedError = computed(() => ADVANCED_KEYS.some(key => !!props.errors[key]))
// open when something in there is already set, and never hide a field with an error
const advancedOpen = ref(advancedSet.value.length > 0)
watch(hasAdvancedError, (value) => {
  if (value) {
    advancedOpen.value = true
  }
})

const set = <K extends keyof WsmanSettingsForm>(key: K, value: WsmanSettingsForm[K]) => {
  emit('update:modelValue', { ...props.modelValue, [key]: value })
}
</script>

<style lang="scss" scoped>
.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0.75rem 1.25rem;
}

.advanced-toggle {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.advanced-summary {
  color: var(--p-text-muted-color);
  font-size: 0.85rem;
}

.check-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  align-self: end;
}
</style>
