<template>
  <div
    class="event-config-drawer-custom-padding"
    v-if="store.eventModificationState.isEditMode !== CreateEditMode.None"
  >
    <BasicInformation />
  </div>
  <div
    v-else
    class="not-found-container"
  >
    <p>No event configuration found.</p>
    <OnmsButton
      label="Go Back"
      @click="goBack()"
    />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

import BasicInformation from '@/components/EventConfigEventCreate/BasicInformation.vue'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { OnmsButton } from '@opennms/onms-ui'

const router = useRouter()
const store = useEventModificationStore()

const goBack = () => {
  if (store.selectedSource?.id) {
    router.push({ name: 'Event Configuration Detail', params: { id: store.selectedSource.id }})
  } else {
    router.push({ name: 'Event Configuration' })
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/onms-typography' as *;

.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 25px;

  p {
    @include onms-headline3;
    margin: 0;
  }
}
</style>
