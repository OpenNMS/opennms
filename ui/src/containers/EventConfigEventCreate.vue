<template>
  <div
    class="feather-drawer-custom-padding"
    v-if="store.selectedSource && store.eventModificationState.eventConfigEvent"
  >
    <BasicInformation />
  </div>
  <div
    v-else
    class="not-found-container"
  >
    <p>No event configuration found.</p>
    <FeatherButton
      primary
      @click="goBack()"
    >
      Go Back
    </FeatherButton>
  </div>
</template>

<script setup lang="ts">
import BasicInformation from '@/components/EventConfigEventCreate/BasicInformation.vue'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { FeatherButton } from '@featherds/button'

const router = useRouter()
const store = useEventModificationStore()

const goBack = () => {
  if (store.selectedSource?.id) {
    router.push({ name: 'Event Configuration Detail', params: { id: store.selectedSource.id } })
  } else {
    router.push({ name: 'Event Configuration' })
  }
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';

.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 25px;

  p {
    @include typography.headline3;
    margin: 0;
  }
}
</style>

