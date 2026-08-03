<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="onms-row">
    <div class="onms-col-12 system-report">
      <div v-if="loadError" class="sr-error" data-test="load-error">{{ loadError }}</div>

      <Card class="sr-card">
        <template #title>
          <div class="section-title">
            <span>Plugins</span>
            <div class="all-toggle">
              <Checkbox inputId="sr-all" :modelValue="allSelected" binary data-test="all-toggle" @update:modelValue="toggleAll" />
              <label for="sr-all">All</label>
            </div>
          </div>
        </template>
        <template #content>
          <p>Choose which plugins to enable:</p>
          <div v-for="plugin in plugins" :key="plugin.name" class="plugin-row">
            <Checkbox :inputId="`sr-plugin-${plugin.name}`" v-model="selectedPlugins" :value="plugin.name" />
            <label :for="`sr-plugin-${plugin.name}`"><strong>{{ plugin.name }}</strong>: {{ plugin.description }}</label>
          </div>
          <p v-if="!plugins.length && !loadError" class="muted" data-test="no-plugins">No report plugins are available.</p>
        </template>
      </Card>

      <Card class="sr-card">
        <template #title><span>Report Type</span></template>
        <template #content>
          <div class="field">
            <label for="sr-formatter">Choose which report to use</label>
            <Select
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
            <InputText id="sr-filename" v-model="filename" data-test="filename" />
          </div>
        </template>
      </Card>

      <div class="actions">
        <OnmsButton :disabled="!canGenerate" data-test="generate-btn" @click="generate">Generate System Report</OnmsButton>
      </div>
    </div>
  </div>

  <!-- The report streams as a file attachment; target a hidden iframe so the page
       itself never navigates away. -->
  <iframe ref="downloadFrame" name="sr-download-frame" class="sr-download-frame" title="report download"></iframe>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import { OnmsButton } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import API from '@/services'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { SystemReportFormatter, SystemReportPlugin } from '@/types/systemReport'

const menuStore = useMenuStore()

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Generate System Report', to: '/system-report' }
])

const plugins = ref<SystemReportPlugin[]>([])
const formatters = ref<SystemReportFormatter[]>([])
const selectedPlugins = ref<string[]>([])
const selectedFormatter = ref<string>('')
const filename = ref<string>('')
const loadError = ref<string | null>(null)

const formatterOptions = computed(() =>
  formatters.value.map((f) => ({ label: `${f.name}: ${f.description}`, value: f.name })))

const allSelected = computed<boolean>(() =>
  plugins.value.length > 0 && selectedPlugins.value.length === plugins.value.length)

const canGenerate = computed<boolean>(() => selectedPlugins.value.length > 0 && !!selectedFormatter.value)

const toggleAll = () => {
  selectedPlugins.value = allSelected.value ? [] : plugins.value.map((p) => p.name)
}

const generate = () => {
  if (!canGenerate.value) {
    return
  }
  const base = menuStore.mainMenu?.baseHref || '/opennms/'
  const form = document.createElement('form')
  form.method = 'post'
  form.action = `${base}admin/support/systemReport.htm`
  form.target = 'sr-download-frame'

  const addField = (name: string, value: string) => {
    const input = document.createElement('input')
    input.type = 'hidden'
    input.name = name
    input.value = value
    form.appendChild(input)
  }
  addField('operation', 'run')
  addField('formatter', selectedFormatter.value)
  selectedPlugins.value.forEach((p) => addField('plugins', p))
  if (filename.value.trim()) {
    addField('output', filename.value.trim())
  }

  document.body.appendChild(form)
  form.submit()
  document.body.removeChild(form)
}

onMounted(async () => {
  const [loadedPlugins, loadedFormatters] = await Promise.all([
    API.getSystemReportPlugins(),
    API.getSystemReportFormatters()
  ])
  if (loadedPlugins === null || loadedFormatters === null) {
    loadError.value = 'Failed to load the system report options.'
    return
  }
  plugins.value = loadedPlugins
  formatters.value = loadedFormatters
  // default to every plugin enabled, matching the legacy form
  selectedPlugins.value = loadedPlugins.map((p) => p.name)
  // prefer the zip (full) report if present, else the first formatter
  selectedFormatter.value = (loadedFormatters.find((f) => f.name === 'zip') ?? loadedFormatters[0])?.name ?? ''
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
.sr-download-frame {
  display: none;
}
</style>
