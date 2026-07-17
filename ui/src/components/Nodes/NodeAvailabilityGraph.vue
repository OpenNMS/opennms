<template>
  <div class="card">
    <div class="title headline3">Availability</div>

    <div class="flex-container availability-header headline4">
      <div class="onms-row">
          <div class="onms-col-12">
            <div>Availability (last 24 hours)</div>
        </div>
      </div>
      <div class="onms-row">
        <div class="onms-col-10">
        </div>
        <div class="onms-col-2">
          <div class="timeline" ref="timeline">{{ Math.round(100 * availability.availability) / 100 }}%</div>
        </div>
      </div>
    </div>

    <template v-for="ipinterface of availability.ipinterfaces" :key="ipinterface.id">
      <div v-if="ipinterface.services.length">
        <hr class="divider" />
        <div class="onms-row">
          <div class="onms-col-1">
            XXX
          </div>
          <div class="onms-col-3">
            <div class="subtitle2">{{ ipinterface.address }}</div>
          </div>
          <div class="onms-col-6 timeline-header">
            <img
              :src="`${baseHref}rest/timeline/header/${startTime}/${endTime}/${width}`"
              :data-imgsrc="`${baseHref}rest/timeline/header/${startTime}/${endTime}/`"
            />
          </div>
          <div class="onms-col-2">
            <div class="percentage subtitle2">{{ Math.round(100 * ipinterface.availability) / 100 }}%</div>
          </div>
        </div>
      </div>

      <template v-for="service of ipinterface.services" :key="service.name">
        <div class="onms-row">
          <div class="onms-col-1">
            XXX
          </div>
          <div class="onms-col-3">
            <div class="service subtitle2">
              <a :href="getServiceLink(ipinterface, service)">{{ service.name }}</a>
            </div>
          </div>
          <div class="onms-col-6">
            <img
              :src="getServiceAvailabilityImageLink(ipinterface, service)"
            />
          </div>
          <div class="onms-col-2">
            <div class="percentage subtitle2">{{ Math.round(100 * service.availability) / 100 }}%</div>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, PropType, watch } from 'vue'
import { useNodeStore } from '@/stores/nodeStore'
import { useNodeListStore } from '@/stores/nodeListStore'

import { debounce } from 'lodash'
import { sub, getUnixTime } from 'date-fns'
import { Node } from '@/types'

const props = defineProps({
  baseHref: {
    required: true,
    type: String
  },
  node: {
    required: true,
    type: Object as PropType<Node>
  }
})

// const baseUrl = ref(import.meta.env.VITE_BASE_URL || '')
const nodeStore = useNodeStore()
const nodeListStore = useNodeListStore()
const now = new Date()
const startTime = ref(getUnixTime(sub(now, { days: 1 })))
const endTime = ref(getUnixTime(now))
// const width = ref(200)
const width = ref(400)
const timeline = ref<any>(null)

const recalculateWidth = debounce(() => {
  if (!timeline.value) {
    return
  }
  // width.value = timeline.value.clientWidth - 60
  // width.value = Math.max(timeline.value.clientWidth - 60, 300)
  width.value = 400
}, 500)

const availability = computed(() => nodeStore.availability)

const getServiceLink = (ipinterface: any, service: any) => {
  const serviceId = nodeListStore.getServiceTypeByName(service.name)?.id
  if (!serviceId) {
    return '#'
  }
  return `${props.baseHref}element/service.jsp?node=${props.node.id}&intf=${encodeURIComponent(ipinterface.address)}&service=${serviceId}`
}

const getServiceAvailabilityImageLink = (ipinterface: any, service: any) => {
  const serviceId = nodeListStore.getServiceTypeByName(service.name)?.id
  if (!serviceId) {
    return '#'
  }

  return `${props.baseHref}rest/timeline/image/${props.node.id}/${ipinterface.address}/${serviceId}/${startTime.value}/${endTime.value}/${width.value}`
}

watch([() => props.node?.id], () => {
  nodeStore.getNodeAvailabilityPercentage(props.node.id)
})

onMounted(async () => {
  recalculateWidth()
  window.addEventListener('resize', recalculateWidth)
})

onUnmounted(() => {
  recalculateWidth.cancel()
  window.removeEventListener('resize', recalculateWidth)
})
</script>

<style lang="scss" scoped>
.card {
  background: var(--p-content-background);
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.08);
  padding: 15px;
  margin-bottom: 15px;

  .title {
    padding: 5px 10px 0px 10px;
  }
}
.service {
  min-width: 103px;
  margin-left: 8px;
}

.timeline {
  flex-grow: 1;
  text-align: end;
}

.timeline-header {
  background-color: lightgray;
}

.availability-header {
  padding: 0px 0px 0px 10px;
}

.percentage {
  margin-left: 3px;
}

.divider {
  width: 98%;
}

.flex-container {
  padding: 0;
  margin: 0;
  list-style: none;
  display: flex;
}
</style>
