<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="onms-row">
    <div class="onms-col-12 system-report">
      <div v-if="loadError" class="sr-error" data-test="load-error">{{ loadError }}</div>

      <OnmsCard class="sr-card">
        <template #title>
          <div class="section-title">
            <span>Plugins</span>
            <div class="all-toggle">
              <OnmsCheckbox inputId="sr-all" :modelValue="allSelected" data-test="all-toggle" @update:modelValue="toggleAll" />
              <label for="sr-all">All</label>
            </div>
          </div>
        </template>
        <template #content>
          <p>Choose which plugins to enable:</p>
          <div v-for="plugin in plugins" :key="plugin.name" class="plugin-row">
            <OnmsCheckbox
              :inputId="pluginId(plugin.name)"
              :modelValue="selectedPlugins.includes(plugin.name)"
              @update:modelValue="(checked) => togglePlugin(plugin.name, checked)"
            />
            <label :for="pluginId(plugin.name)"><strong>{{ plugin.name }}</strong>: {{ plugin.description }}</label>
          </div>
          <p v-if="!plugins.length && !loadError" class="muted" data-test="no-plugins">No report plugins are available.</p>
        </template>
      </OnmsCard>

      <OnmsCard class="sr-card">
        <template #title><span>Report Type</span></template>
        <template #content>
          <div class="field">
            <label for="sr-formatter">Choose which report to use</label>
            <OnmsSelect
              inputId="sr-formatter"
              v-model="selectedFormatter"
              :options="formatterOptions"
              optionLabel="label"
              optionValue="value"
              data-test="formatter-select"
            />
          </div>
          <div class="field">
            <label for="sr-filename">File name <span class="muted">(optional)</span></label>
            <OnmsInputText id="sr-filename" v-model="filename" data-test="filename" />
          </div>
        </template>
      </OnmsCard>

      <div class="actions">
        <OnmsButton :disabled="!canGenerate" data-test="generate-btn" @click="generate">Generate System Report</OnmsButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import { OnmsButton, OnmsCard, OnmsCheckbox, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import API from '@/services'
import useDownload from '@/composables/useDownload'
import useRole from '@/composables/useRole'
import useSnackbar from '@/composables/useSnackbar'
import { BreadCrumb } from '@/types'
import { SystemReportFormatter, SystemReportPlugin } from '@/types/systemReport'

const { showSnackBar } = useSnackbar()
const { downloadFile } = useDownload()
const { adminRole } = useRole()

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Generate System Report', to: '/system-report' }
])

// Plugin names can contain spaces (e.g. "Hard Drive Stats"); an HTML id may not,
// so derive a safe, stable id for the checkbox/label association.
const pluginId = (name: string) => `sr-plugin-${name.replace(/\W+/g, '-')}`

const plugins = ref<SystemReportPlugin[]>([])
const formatters = ref<SystemReportFormatter[]>([])
const selectedPlugins = ref<string[]>([])
const selectedFormatter = ref<string>('')
const filename = ref<string>('')
const loadError = ref<string | null>(null)

const formatterOptions = computed(() =>
  formatters.value.map(f => ({ label: `${f.name}: ${f.description}`, value: f.name })))

const allSelected = computed<boolean>(() =>
  plugins.value.length > 0 && selectedPlugins.value.length === plugins.value.length)

const canGenerate = computed<boolean>(() => selectedPlugins.value.length > 0 && !!selectedFormatter.value)

const toggleAll = () => {
  selectedPlugins.value = allSelected.value ? [] : plugins.value.map(p => p.name)
}

const togglePlugin = (name: string, checked: boolean) => {
  selectedPlugins.value = checked
    ? [...new Set([...selectedPlugins.value, name])]
    : selectedPlugins.value.filter(p => p !== name)
}

const generate = async () => {
  if (!canGenerate.value) {
    return
  }
  // Mirror the server's filename sanitising (basename, then strip to word chars)
  // so a punctuation-only entry doesn't collapse to an empty download name — omit
  // it entirely in that case and let the server default apply.
  const cleanedName = (filename.value.trim().split(/[\\/]/).pop() ?? '').replace(/[^\w.]/g, '')

  // The report is built on demand — plugins run live and logs/config are gathered
  // and compressed — so it can take a while on a large system.
  showSnackBar({ msg: 'Generating the system report — this can take a while on a large system. Your download will start when it is ready.' })

  const response = await API.generateSystemReport({
    formatter: selectedFormatter.value,
    plugins: [...selectedPlugins.value],
    output: cleanedName || undefined
  })

  if (!response) {
    showSnackBar({
      msg: 'The system report could not be generated. Your session may have expired, or a report plugin failed — please try again.',
      error: true
    })
    return
  }

  // force blob so the report is saved as-is rather than JSON-stringified
  downloadFile(response, true)
}

onMounted(async () => {
  const [loadedPlugins, loadedFormatters] = await Promise.all([
    API.getSystemReportPlugins(),
    API.getSystemReportFormatters()
  ])
  if (loadedPlugins === null || loadedFormatters === null) {
    // the endpoint is admin-only; a non-admin who deep-links here is being
    // redirected by the route guard, so don't flash a load error at them
    if (adminRole.value) {
      loadError.value = 'Failed to load the system report options.'
    }
    return
  }
  plugins.value = loadedPlugins
  formatters.value = loadedFormatters
  // default to every plugin enabled, matching the legacy form
  selectedPlugins.value = loadedPlugins.map(p => p.name)
  // default to the plain-text report (the legacy form's default), else the first
  selectedFormatter.value = (loadedFormatters.find(f => f.name === 'text') ?? loadedFormatters[0])?.name ?? ''
})
</script>

<style scoped lang="scss">
.system-report {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.sr-card {
  width: 100%;
}
.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.all-toggle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}
.plugin-row {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
  padding: 2px 0;

  label {
    cursor: pointer;
  }
}
.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  margin-bottom: 1rem;

  :deep(.p-select),
  :deep(.p-inputtext) {
    width: 100%;
    max-width: 720px;
  }
}
.actions {
  margin-top: 0.5rem;
}
.muted {
  color: var(--onms-body-text-color-muted, #6c757d);
}
.sr-error {
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  border: 1px solid var(--onms-error-color, #e24c4c);
  color: var(--onms-error-color, #e24c4c);
}
</style>
