<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="system-report">
    <header class="page-header">
      <h1 class="page-title">Generate System Report</h1>
      <p class="page-subtitle">
        Collect information about this OpenNMS instance and its environment, for example to attach to a support ticket.
      </p>
    </header>

    <div v-if="loadError" class="sr-error" data-test="load-error">{{ loadError }}</div>

    <section class="sr-card">
      <div class="sr-toolbar">
        <div>
          <h2 class="section-title">Report contents</h2>
          <span class="muted" data-test="selected-count">{{ selectedPlugins.length }} of {{ plugins.length }} selected</span>
        </div>
      </div>

      <ul class="plugin-list">
        <li v-if="plugins.length" class="all-row">
          <label for="sr-all" class="plugin-row all-toggle">
            <OnmsCheckbox inputId="sr-all" :modelValue="allSelected" data-test="all-toggle" @update:modelValue="toggleAll" />
            <span>Select all</span>
          </label>
        </li>
        <li v-for="plugin in plugins" :key="plugin.name">
          <label :for="pluginId(plugin.name)" class="plugin-row">
            <OnmsCheckbox
              :inputId="pluginId(plugin.name)"
              :modelValue="selectedPlugins.includes(plugin.name)"
              @update:modelValue="(checked) => togglePlugin(plugin.name, checked)"
            />
            <span class="plugin-text">
              <span class="plugin-name">{{ plugin.name }}</span>
              <span class="plugin-description">{{ plugin.description }}</span>
            </span>
          </label>
        </li>
      </ul>
      <p v-if="!plugins.length && !loadError" class="muted" data-test="no-plugins">No report contents are available.</p>

      <div class="generate-row">
        <!-- only a choice when more than one format is offered; otherwise the single one is used -->
        <div v-if="formatters.length > 1" class="field" data-test="formatter-field">
          <label for="sr-formatter">Report format</label>
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
          <label for="sr-filename">File name</label>
          <OnmsInputText id="sr-filename" v-model="filename" :placeholder="defaultFilename" data-test="filename" />
        </div>
        <OnmsButton :disabled="!canGenerate" data-test="generate-btn" @click="generate">Generate System Report</OnmsButton>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import { OnmsButton, OnmsCheckbox, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import API from '@/services'
import useDownload from '@/composables/useDownload'
import useRole from '@/composables/useRole'
import useSnackbar from '@/composables/useSnackbar'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { SystemReportFormatter, SystemReportPlugin } from '@/types/systemReport'

const { showSnackBar } = useSnackbar()
const { downloadFile } = useDownload()
const { adminRole } = useRole()
const menuStore = useMenuStore()

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: menuStore.mainMenu?.homeUrl, isAbsoluteLink: true },
  { label: 'Generate System Report', to: '/system-report', position: 'last' }
])

// Plugin names can contain spaces (e.g. "Hard Drive Stats"); an HTML id may not,
// so derive a safe, stable id for the checkbox/label association.
const pluginId = (name: string) => `sr-plugin-${name.replace(/\W+/g, '-')}`

const plugins = ref<SystemReportPlugin[]>([])
const formatters = ref<SystemReportFormatter[]>([])
const selectedPlugins = ref<string[]>([])
const selectedFormatter = ref<string>('')
const filename = ref<string>('')
// the server's own default name, shown as the placeholder and used when the field is left empty
const defaultFilename = computed(() => {
  const formatter = formatters.value.find(f => f.name === selectedFormatter.value)
  return formatter ? `opennms-system-report.${formatter.extension}` : ''
})
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
  // Typing the default itself is treated as empty too: the sanitising would strip its hyphens.
  const cleanedName = filename.value.trim() === defaultFilename.value
    ? ''
    : (filename.value.trim().split(/[\\/]/).pop() ?? '').replace(/[^\w.]/g, '')

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
      msg: 'The system report could not be generated. Your session may have expired, or part of the report failed — please try again.',
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
  gap: 1.5rem;
  padding: 0.5rem 2px 2rem 2px;
}
.page-title {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 600;
}
.page-subtitle {
  margin: 0.35rem 0 0 0;
  color: var(--p-text-muted-color);
}
.sr-card {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding: 1.5rem;
  border: 1px solid var(--p-content-border-color);
  border-radius: 8px;
  background: var(--p-content-background);
}
.sr-toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1rem;
}
.section-title {
  margin: 0 0 0.25rem 0;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--p-text-muted-color);
}
.all-toggle {
  font-weight: 600;
}
.plugin-list {
  list-style: none;
  margin: 0;
  padding: 0;

  li + li {
    border-top: 1px solid var(--p-content-border-color);
  }
}
.plugin-row {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 0.65rem 0.5rem;
  border-radius: 6px;
  cursor: pointer;

  &:hover {
    background: color-mix(in srgb, var(--p-primary-color) 6%, transparent);
  }
}
.plugin-text {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  column-gap: 1rem;
  row-gap: 0.15rem;
  min-width: 0;
}
.plugin-name {
  flex: 0 0 12rem;
  font-weight: 600;
}
.plugin-description {
  color: var(--p-text-muted-color);
  font-size: 0.875rem;
  line-height: 1.35;
}
.generate-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 1rem;
  padding-top: 1.25rem;
  border-top: 1px solid var(--p-content-border-color);
}
.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  flex: 0 1 24rem;
  min-width: 14rem;

  :deep(.p-select),
  :deep(.p-inputtext) {
    width: 100%;
  }
}
.muted {
  color: var(--p-text-muted-color);
}
.sr-error {
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  border: 1px solid var(--onms-error);
  color: var(--onms-error);
}
</style>
