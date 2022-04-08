<template>
  <FeatherDrawer
    v-if="isOpen"
    :modelValue="isOpen"
    @update:modelValue="closeDrawer"
    :labels="{ close: 'close', title: 'Layers' }"
    width="15em"
  >
    <FeatherList class="right-drawer-list">
      <FeatherListItem
        v-for="graph in powerGridGraphs.graphs"
        :key="graph.label"
        @click="selectTopologyGraph(graph.namespace)"
      >{{ graph.label }}</FeatherListItem>
    </FeatherList>
  </FeatherDrawer>
</template>

<script setup lang="ts">
import { useStore } from 'vuex'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherListItem, FeatherList } from '@featherds/list'
import { TopologyGraphList } from '@/types/topology'

const store = useStore()
const isOpen = computed<boolean>(() => store.state.topologyModule.isRightDrawerOpen)
const powerGridGraphs = computed<TopologyGraphList>(() => store.getters['topologyModule/getPowerGridGraphs'])

const selectTopologyGraph = (namespace: string) => store.dispatch('topologyModule/getTopologyGraphByContainerAndNamespace', { containerId: powerGridGraphs.value.id, namespace })
const closeDrawer = () => store.dispatch('topologyModule/closeRightDrawer')
</script>

<style scoped lang="scss">
.right-drawer-list {
  margin-top: 30px;
}
</style>
