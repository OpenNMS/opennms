<template>
  <FeatherTabContainer class="tabs">
    <template v-slot:tabs>
      <FeatherTab ref="alarmTab" @click="goToAlarms">Alarms({{ alarms.length }})</FeatherTab>
      <FeatherTab ref="nodesTab" @click="goToNodes">Nodes({{ nodes.length }})</FeatherTab>
    </template>
  </FeatherTabContainer>
  <router-view />
</template>
<script setup lang=ts>
import { ref, onActivated } from 'vue'
import { computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { FeatherTab, FeatherTabContainer } from "@featherds/tabs"
import { Alarm, Node } from '@/types'

const store = useStore()
const router = useRouter()
const nodes = computed<Node[]>(() => store.getters['mapModule/getNodes'])
const alarms = computed<Alarm[]>(() => store.getters['mapModule/getAlarms'])
const alarmTab = ref()
const nodesTab = ref()

const goToAlarms = () => router.push('/map')
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
.tabs {
  padding-bottom: 10px;
  margin-bottom: -15px;
  background: var(--feather-surface);
}
</style>
