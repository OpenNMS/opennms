<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="`Monitoring location ${summary?.name ?? ''} deleted`"
    class="location-deleted-dialog"
    width="min(560px, 95vw)"
    data-test="location-deleted-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <ul v-if="summary" class="summary">
      <li data-test="perspective-line">{{ perspectiveLine }}</li>
      <li data-test="outages-line">{{ outagesLine }}</li>
      <li data-test="minions-line">Minions that still point at <code>{{ summary.name }}</code> keep the old name until they are re-registered.</li>
    </ul>
    <template #footer>
      <OnmsButton label="Close" data-test="close-button" @click="emit('update:visible', false)" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import { OnmsButton, OnmsDialog } from '@opennms/onms-ui'

import type { LocationDeletedSummary } from '@/components/ManageMonitoringLocations/LocationDeleteDialog.vue'

const props = defineProps<{
  visible: boolean
  summary: LocationDeletedSummary | null
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const perspectiveLine = computed(() => {
  const apps = props.summary?.applications
  if (apps === null || apps === undefined) {
    return 'Removed as a perspective from any application that used it.'
  }
  return apps.length ? `Removed as a perspective from: ${apps.map(app => app.name).join(', ')}` : 'Was not used as a perspective.'
})

const outagesLine = computed(() => {
  const outages = props.summary?.outageCount
  if (outages === null || outages === undefined) {
    return 'Any perspective outage history was deleted.'
  }
  return outages ? `${outages} perspective ${outages === 1 ? 'outage' : 'outages'} deleted.` : 'No perspective outage history.'
})
</script>

<style lang="scss" scoped>
.summary {
  margin: 0.25rem 0 0;
  padding-left: 1.25rem;
  line-height: 1.6;
}
</style>
