<template>
  <FeatherTabContainer class="tabs">
    <template v-slot:tabs>
      <FeatherTab ref="alarmTab" @click="goToAlarms">Alarms({{ alarms.length }})</FeatherTab>
      <FeatherTab ref="nodesTab" @click="goToNodes">Nodes({{ nodes.length }})</FeatherTab>
    </template>
  </FeatherTabContainer>
  <router-view />
</template>
<script setup lang="ts">
import { useMapStore } from '@/stores/mapStore'
import { FeatherTab, FeatherTabContainer } from '@featherds/tabs'
import { Alarm, Node } from '@/types'

const mapStore = useMapStore()
const router = useRouter()
const route = useRoute()
const nodes = computed<Node[]>(() => mapStore.getNodes())
const alarms = computed<Alarm[]>(() => mapStore.getAlarms())
const alarmTab = ref()
const nodesTab = ref()

const goToAlarms = () => router.push(`/map${route.query.nodeid ? '?nodeid=' + route.query.nodeid : ''}`)
const goToNodes = () => router.push('/map/nodes')

onActivated(() => {
  if (router.currentRoute.value.name === 'MapAlarms') {
    alarmTab.value.tab.click()
  } else {
    nodesTab.value.tab.click()
  }
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/themes/variables";
.tabs {
  z-index: 2;
  padding-bottom: 10px;
  margin-bottom: -29px;
  background: var($surface);
}
</style>
