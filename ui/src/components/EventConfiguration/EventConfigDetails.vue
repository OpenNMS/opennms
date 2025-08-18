<template>
  <div class="event-config-details">
    <div class="heading"> 
      <h1>Event Configuration Details</h1>
    </div>

    <div v-if="config">
      <EventConfigFileEditor/>
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
import EventConfigFileEditor from './EventConfigFileEditor.vue'

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
