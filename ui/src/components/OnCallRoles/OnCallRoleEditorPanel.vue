<template>
  <TableCard
    class="oncall-role-editor-panel"
    data-test="oncall-role-editor-panel"
  >
    <div class="card-title">{{ role ? `Edit On-Call Role: ${role.name}` : 'Add New On-Call Role' }}</div>
    <div>
      <OnmsButton
        variant="text"
        label="Back"
        data-test="back-button"
        @click="emit('close')"
      />
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { OnmsButton } from '@opennms/onms-ui'

import TableCard from '@/components/Common/TableCard.vue'
import { ManagedOnCallRole } from '@/types/onCallRoleAdmin'

// placeholder: the editor itself replaces this component in a later step
defineProps<{
  role: ManagedOnCallRole | null
}>()

const emit = defineEmits<{ (e: 'close'): void }>()
</script>

<style lang="scss" scoped>
.oncall-role-editor-panel {
  padding: 25px;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 1rem;
}
</style>
