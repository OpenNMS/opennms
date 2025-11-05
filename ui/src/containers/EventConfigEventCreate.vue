<template>
  <div
    class="feather-drawer-custom-padding"
    v-if="detailStore.selectedSource && store.selectedSource"
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
      @click="router.push({ name: 'Event Configuration' })"
    >
      Go Back
    </FeatherButton>
  </div>
</template>

<script setup lang="ts">
import BasicInformation from '@/components/EventConfigEventCreate/BasicInformation.vue'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { FeatherButton } from '@featherds/button'

const router = useRouter()
const store = useEventModificationStore()
const detailStore = useEventConfigDetailStore()
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

