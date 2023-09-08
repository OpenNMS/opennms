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
    </div>
  </FeatherDialog>
</template>

<script setup lang="ts">
import { FeatherDialog } from '@featherds/dialog'
import {
  FeatherTab,
  FeatherTabContainer,
  FeatherTabPanel,
} from '@featherds/tabs'
import ColumnSelectionPanel from './ColumnSelectionPanel.vue'

defineProps({
  visible: {
    required: true,
    type: Boolean
  }
})

defineEmits(['close'])

const labels = reactive({
  title: 'Node Preferences',
  close: 'Close'
})

</script>

<style scoped lang="scss">
.content {
  min-height: 300px;
  max-height: 600px;
  min-width: 550px;
  overflow-y: auto;
  position: relative;
}
</style>
