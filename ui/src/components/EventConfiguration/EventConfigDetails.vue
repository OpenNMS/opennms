<template>
  <div class="event-config-details">
    <div class="heading"> 
      <h1>Event Configuration Details</h1>
    </div>

    <div v-if="config">
      <p><strong>Name:</strong> {{ config.filename }}</p>
      <p><strong>Description:</strong> {{ config.description }}</p>
      <p><strong>File Order:</strong> {{ config.fileOrder }}</p>
      <p><strong>Vendor:</strong> {{ config.vendor }}</p>
      <p><strong>Event Count:</strong> {{ config.eventCount }}</p>
    </div>

    <div v-else>
      <p>No details found.</p>
    </div>
  </div>
</template>
    
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfSourceMetadata } from '@/types/eventConfig'

const route = useRoute()
const store = useEventConfigStore()
const id = route.params.id

const config = ref<EventConfSourceMetadata | null>(null)

const loadEventConfigDetails = () => {
  config.value = store.eventConfigs.find(c => c.id === Number(id)) || null
}

onMounted(() => {
  loadEventConfigDetails()
})
</script>

<style lang="scss" scoped>
.event-config-details {
  padding: 20px;

  .heading {
    margin-bottom: 10px;
  }
}
</style>
