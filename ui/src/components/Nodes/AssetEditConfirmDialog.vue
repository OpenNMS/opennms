<template>
  <OnmsConfirmationDialog
    :visible="visible"
    title="Edit Asset Fields"
    actionButtonText="Edit Assets"
    data-test="asset-edit-confirm"
    @ok="emit('ok')"
    @cancel="emit('cancel')"
  >
    <template #content>
      <p>
        You are about to edit asset fields for a node that was provisioned through a requisition.
        Any edits made here will be rolled back the next time the requisition
        "{{ foreignSource }}" is synchronized (typically every 24 hours) or the node manually
        rescanned.
      </p>
      <p>To learn the best way to make permanent asset changes, talk to your OpenNMS administrator.</p>
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { OnmsConfirmationDialog } from '@opennms/onms-ui'

// The legacy node page's confirmAssetEdit(): asked before the asset editor opens for a node from a
// requisition, since a sync or rescan overwrites what is edited there. See needsAssetEditConfirm.
defineProps<{
  visible: boolean
  foreignSource?: string | null
}>()

const emit = defineEmits<{
  ok: []
  cancel: []
}>()
</script>

<style lang="scss" scoped>
p {
  margin: 0 0 0.75rem;

  &:last-child {
    margin-bottom: 0;
  }
}
</style>
