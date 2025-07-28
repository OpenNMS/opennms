<template>
  <FeatherDialog :modelValue="visible" relative :labels="labels" @update:modelValue="$emit('close')">
    <div class="content">
      <FeatherTabContainer>
        <template v-slot:tabs>
          <FeatherTab>Columns</FeatherTab>
        </template>
        <FeatherTabPanel>
          <ColumnSelectionPanel></ColumnSelectionPanel>
        </FeatherTabPanel>
      </FeatherTabContainer>
      <div class="button-panel">
        <FeatherButton
          primary
          @click="savePreferences"
        >Save and Close</FeatherButton>
      </div>
    </div>
  </FeatherDialog>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import {
  FeatherTab,
  FeatherTabContainer,
  FeatherTabPanel
} from '@featherds/tabs'
import ColumnSelectionPanel from './ColumnSelectionPanel.vue'
import { saveNodePreferences } from '@/services/localStorageService'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'

defineProps({
  visible: {
    required: true,
    type: Boolean
  }
})

const emit = defineEmits(['close'])

const nodeStructureStore = useNodeStructureStore()

const labels = reactive({
  title: 'Node Preferences',
  close: 'Close'
})

const savePreferences = async () => {
  const nodePrefs = await nodeStructureStore.getNodePreferences()
  if (nodePrefs.nodeFilter) {
    nodePrefs.nodeFilter.searchTerm = ''
  }
  saveNodePreferences(nodePrefs)
  emit('close', true)
}
</script>

<style scoped lang="scss">
.content {
  min-height: 300px;
  max-height: 600px;
  min-width: 550px;
  overflow-x: hidden;
  overflow-y: auto;
  position: relative;
}

.button-panel {
  margin-top: 0.5em;
}
</style>
