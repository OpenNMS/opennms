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
            <Checkbox :inputId="pluginId(plugin.name)" v-model="selectedPlugins" :value="plugin.name" />
            <label :for="pluginId(plugin.name)"><strong>{{ plugin.name }}</strong>: {{ plugin.description }}</label>
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
import useRole from '@/composables/useRole'
import useSnackbar from '@/composables/useSnackbar'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { SystemReportFormatter, SystemReportPlugin } from '@/types/systemReport'

const menuStore = useMenuStore()
const { showSnackBar } = useSnackbar()
const { adminRole } = useRole()

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Generate System Report', to: '/system-report' }
])

const downloadFrame = ref<HTMLIFrameElement | null>(null)

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
  // baseHref is the deployed context path; fall back to it from the current URL
  // (strip /ui/...) so a non-default context path still posts to the right place
  const base = menuStore.mainMenu?.baseHref || window.location.pathname.replace(/\/ui\/.*$/, '/') || '/opennms/'
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
  // Mirror the server's filename sanitising (basename, then strip to word chars)
  // so a punctuation-only entry doesn't collapse to an empty download name — omit
  // it entirely in that case and let the server default apply.
  const cleanedName = (filename.value.trim().split(/[\\/]/).pop() ?? '').replace(/[^\w.]/g, '')
  if (cleanedName) {
    addField('output', cleanedName)
  }

  // A successful report streams as an attachment and never loads the iframe; an
  // error (500, expired session -> login, bad formatter) renders a page into it,
  // so a load event here means generation failed.
  const frame = downloadFrame.value
  if (frame) {
    const onLoad = () => {
      frame.removeEventListener('load', onLoad)
      showSnackBar({
        msg: 'The system report could not be generated. Your session may have expired, or a report plugin failed — please try again.',
        error: true
      })
    }
    frame.addEventListener('load', onLoad)
  }

  document.body.appendChild(form)
  form.submit()
  document.body.removeChild(form)

  // The report is built on demand — plugins run live and logs/config are gathered
  // and compressed before the download begins — so it can take a while on a large
  // system. The browser can't reliably tell us when the streamed download finishes,
  // so at least let the user know it's working.
  showSnackBar({ msg: 'Generating the system report — this can take a while on a large system. Your download will start when it is ready.' })
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
  selectedPlugins.value = loadedPlugins.map((p) => p.name)
  // default to the plain-text report (the legacy form's default), else the first
  selectedFormatter.value = (loadedFormatters.find((f) => f.name === 'text') ?? loadedFormatters[0])?.name ?? ''
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
