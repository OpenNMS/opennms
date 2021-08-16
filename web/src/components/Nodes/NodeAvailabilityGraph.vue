<template>
  <Panel header="Availability (last 24 hours)">
    <div class="p-grid">
      <div class="flex-container availability-header">
        <div class="service">Availability</div>
        <div class="timeline" ref="timeline">{{ availability.availability }}%</div>
      </div>

      <template v-for="ipinterface of availability.ipinterfaces">
        <div v-if="ipinterface.services.length">
          <Divider />
          <div class="flex-container">
            <div class="service">{{ ipinterface.address }}</div>
            <div class>
              <img
                :src="`${baseUrl}/opennms/rest/timeline/header/${startTime}/${endTime}/${width}`"
                :data-imgsrc="`/opennms/rest/timeline/header/${startTime}/${endTime}/`"
              />
            </div>
          </div>
        </div>

        <template v-for="service of ipinterface.services">
          <div class="flex-container">
            <div class="service">{{ service.name }}</div>
            <div>
              <img
                :src="`${baseUrl}/opennms/rest/timeline/image/${nodeId}/${ipinterface.address}/${service.name}/${startTime}/${endTime}/${width}`"
              />
            </div>
            <div class="percentage">{{ service.availability }}%</div>
          </div>
        </template>
      </template>
    </div>
  </Panel>
</template>
  
<script setup lang="ts">
import { onMounted, ref, onUnmounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import Panel from 'primevue/panel'
import Divider from 'primevue/divider'
import { debounce } from 'lodash'

// @ts-ignore
const baseUrl = ref(import.meta.env.VITE_BASE_URL || '')
const store = useStore()
const route = useRoute()
const nodeId = ref(route.params.id as string)
const now = dayjs()
const startTime = ref(now.subtract(1, 'day').unix())
const endTime = ref(now.unix())
const width = ref(200)
const timeline = ref(null)
const recalculateWidth = () => {
  // @ts-ignore
  width.value = timeline.value.clientWidth - 60
}

onMounted(async () => {
  store.dispatch('nodesModule/getNodeAvailabilityPercentage', nodeId.value)
  recalculateWidth()
  window.addEventListener("resize", debounce(recalculateWidth, 100))
})

const availability = computed(() => store.state.nodesModule.availability)

onUnmounted(() => window.removeEventListener("resize", recalculateWidth))
</script>

<style lang="scss" scoped>
.service {
  min-width: 120px;
}
.timeline {
  flex-grow: 1;
  text-align: end;
}
.availability-header {
  width: 100%;
}
.percentage {
  margin-left: 10px;
}
</style>
