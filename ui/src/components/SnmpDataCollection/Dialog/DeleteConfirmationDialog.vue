<template>
  <ConfirmationDialog
    class="delete-event-config-source-modal"
    :visible="props.visible"
    :title="label.title"
    action-button-text="Delete"
    @cancel="close"
    @ok="confirmDelete"
  >
    <template #content>
      <div
        class="modal-body"
        v-if="props.selected?.id && props.selected?.name"
      >
        <div v-if="props.type === 'source'">
          <p>
            This will delete the SNMP Data Collection Source:
            <strong>{{ props.selected.name }}</strong>
          </p>
          <p>
            <strong>Note:</strong> Deleting an SNMP Data Collection Source will also remove all associated MIB Groups,
            System Definitions, and Resource Types.
          </p>
        </div>
        <div v-if="props.type === 'mib-group'">
          <p>
            This will delete the MIB Group:
            <strong>{{ props.selected.name }}</strong>
          </p>
        </div>
        <div v-if="props.type === 'system-def'">
          <p>
            This will delete the System Definition:
            <strong>{{ props.selected.name }}</strong>
          </p>
        </div>
        <div v-if="props.type === 'resource-type'">
          <p>
            This will delete the Resource Type:
            <strong>{{ props.selected.name }}</strong>
          </p>
        </div>
        <p>This action can not be undone.</p>
        <p><strong>Are you sure you want to proceed?</strong></p>
      </div>
    </template>
  </ConfirmationDialog>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

import ConfirmationDialog from '@/components/Common/ConfirmationDialog.vue'

const props = defineProps<{
  visible: boolean
  selected: { id: number; name: string } | null
  type: 'source' | 'mib-group' | 'system-def' | 'resource-type'
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'confirm', selected: { id: number; name: string } | null, type: string): void
}>()

const label = computed(() => {
  let title = 'Delete'
  switch (props.type) {
    case 'source':
      title = 'Delete SNMP Data Collection Source'
      break
    case 'mib-group':
      title = 'Delete MIB Group'
      break
    case 'system-def':
      title = 'Delete System Definition'
      break
    case 'resource-type':
      title = 'Delete Resource Type'
      break
  }
  return { title }
})

const close = () => {
  emit('close')
}

const confirmDelete = () => {
  emit('confirm', props.selected, props.type)
}
</script>

<style lang="scss" scoped></style>
