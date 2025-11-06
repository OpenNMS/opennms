<template>
  <div
    class="feather-drawer-custom-padding"
    v-if="store.selectedSource"
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
import { getEventConfSourceById } from '@/services/eventConfigService'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { FeatherButton } from '@featherds/button'

const router = useRouter()
const route = useRoute()
const store = useEventModificationStore()

const loadEventSource = async () => {
  if (route.params.id) {
    try {
      const source = await getEventConfSourceById(route.params.id as string)
      if (source) {
        store.selectedSource = source
      }
    } catch (error) {
      console.error('Failed to fetch event configuration source:', error)
    }
  }
}

onMounted(async () => {
  await loadEventSource()
})
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

