<template>
  <OnmsTabs :value="activeTab" class="tabs">
    <OnmsTabList>
      <OnmsTab value="alarms" @click="goToAlarms">Alarms ({{ alarms.length }})</OnmsTab>
      <OnmsTab value="nodes" @click="goToNodes">Nodes ({{ nodes.length }})</OnmsTab>
    </OnmsTabList>
  </OnmsTabs>
  <router-view />
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useMapStore } from '@/stores/mapStore'
import { OnmsTab, OnmsTabList, OnmsTabs } from '@opennms/onms-ui'
import { Alarm, Node } from '@/types'

const mapStore = useMapStore()
const router = useRouter()
const route = useRoute()
const nodes = computed<Node[]>(() => mapStore.getNodes())
const alarms = computed<Alarm[]>(() => mapStore.getAlarms())

// Drive the active tab from the current route so the tabs are really router nav.
const activeTab = computed(() => (route.name === 'MapAlarms' ? 'alarms' : 'nodes'))

const goToAlarms = () => router.push(`/map${route.query.nodeid ? '?nodeid=' + route.query.nodeid : ''}`)
const goToNodes = () => router.push('/map/nodes')
</script>

<style scoped lang="scss">
.tabs {
  z-index: 2;
  padding-bottom: 10px;
  margin-bottom: -29px;
  background: var(--p-content-background);
}
</style>
