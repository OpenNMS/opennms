<template>
  <!-- Styling uses host CSS custom properties inline: it must follow the host
       theme + dark mode with no CSS of its own. (No <style> block: lib-mode
       extracts CSS to a file this example's dev harness doesn't serve; real
       plugins ship CSS via GET /rest/plugins/ui-extension/css/{id}.) -->
  <div :style="{ padding: '1.5rem', background: 'var(--p-content-background)', color: 'var(--p-text-color)' }">
    <h1 :style="{ marginBottom: '0.5rem' }">onms-ui example plugin</h1>
    <p :style="{ marginBottom: '1rem', color: 'var(--p-text-muted-color)' }">
      Rendered by the host from window.OnmsUI v{{ version }} — every component below resolves at runtime from the host bundle.
    </p>

    <OnmsTabs value="form">
      <OnmsTabList>
        <OnmsTab value="form">Form</OnmsTab>
        <OnmsTab value="table">Table</OnmsTab>
      </OnmsTabList>
      <OnmsTabPanels>
        <OnmsTabPanel value="form">
          <div :style="{ display: 'flex', gap: '0.75rem', alignItems: 'center', marginTop: '1rem', flexWrap: 'wrap' }">
            <OnmsInputText v-model="name" placeholder="Name" />
            <OnmsSelect v-model="flavor" :options="['vanilla', 'chocolate', 'pistachio']" placeholder="Flavor" />
            <OnmsButton @click="onToast">Toast (host outlet)</OnmsButton>
            <OnmsButton variant="outlined" @click="dialogVisible = true">Open dialog (z-index)</OnmsButton>
          </div>
        </OnmsTabPanel>
        <OnmsTabPanel value="table">
          <OnmsTable :value="nodes" :style="{ marginTop: '1rem' }">
            <OnmsColumn field="id" header="ID" />
            <OnmsColumn field="label" header="Label" />
            <OnmsColumn field="status" header="Status">
              <template #body="{ data }">
                <OnmsTag :value="data.status" :severity="data.status === 'up' ? 'success' : 'danger'" />
              </template>
            </OnmsColumn>
          </OnmsTable>
        </OnmsTabPanel>
      </OnmsTabPanels>
    </OnmsTabs>

    <OnmsDialog
      v-model:visible="dialogVisible"
      header="Plugin dialog"
      modal
    >
      <p>If this overlays the host chrome correctly, z-index layering holds.</p>
    </OnmsDialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  ONMS_UI_VERSION,
  OnmsButton,
  OnmsColumn,
  OnmsDialog,
  OnmsInputText,
  OnmsSelect,
  OnmsTab,
  OnmsTabList,
  OnmsTabPanel,
  OnmsTabPanels,
  OnmsTable,
  OnmsTabs,
  OnmsTag,
  useOnmsToast
} from '@opennms/onms-ui'

const version = ONMS_UI_VERSION
const name = ref('')
const flavor = ref<string>()
const dialogVisible = ref(false)
const { showToast } = useOnmsToast()

const nodes = [
  { id: 1, label: 'core-router', status: 'up' },
  { id: 2, label: 'edge-switch', status: 'down' },
  { id: 3, label: 'lab-server', status: 'up' }
]

const onToast = () => {
  showToast({ message: `Hello ${name.value || 'from the example plugin'} — toasted by the HOST's outlet` })
}
</script>
