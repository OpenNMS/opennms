<template>
  <FeatherTabContainer class="tabs">
    <template v-slot:tabs>
      <FeatherTab @click="goToNodes">Nodes({{ interestedNodesID.length }})</FeatherTab>
      <FeatherTab @click="goToAlarms">Alarms({{ alarms.length }})</FeatherTab>
    </template>
  </FeatherTabContainer>
  <router-view />
</template>
<script setup lang=ts>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import {
  FeatherTab,
  FeatherTabContainer,
} from "@featherds/tabs"

const store = useStore()
const router = useRouter()
const interestedNodesID = computed<string[]>(() => store.state.mapModule.interestedNodesID)
const alarms = computed(() => store.getters['mapModule/getAlarmsFromSelectedNodes'])

const goToNodes = () => router.push('/map')
const goToAlarms = () => router.push('/map/alarms')
</script>

<style scoped lang="scss">
.tabs {
  margin-bottom: -35px;
  background: var(--feather-background);
}
</style>
