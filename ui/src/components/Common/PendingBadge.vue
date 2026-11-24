<template>
  <OnmsTag
    v-onms-tooltip.top="PENDING_TEXT"
    value="Pending"
    class="pending-badge"
    data-test="pending-badge"
  />
</template>

<script setup lang="ts">
import { OnmsTag } from '@opennms/onms-ui'

// Marks an entry added in an editor but not saved yet (a new schedule row, a picked member, ...),
// so the user can tell it apart from what is stored. Goes right after the entry's remove control.
const PENDING_TEXT = 'Added but not saved yet. Save to keep it.'
</script>

<style lang="scss" scoped>
.pending-badge {
  background: var(--p-surface-500, #757575);
  color: #fff;
  font-style: italic;
  font-weight: normal;
}
</style>
