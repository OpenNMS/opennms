<template>
  <div class="topo-layers-drawer">
    <div class="close-btn">
      <FeatherButton
        icon="close"
        @click="closeDrawer"
      >
        <FeatherIcon :icon="Close" />
      </FeatherButton>
    </div>
    <FeatherList>
      <FeatherListItem
        v-for="graph in graphsDisplay.graphs"
        :key="graph.label"
        @click="selectTopologyGraph(graph.namespace)"
        :class="{ 'selected' : graph.namespace === selectedNamespace }"
        >{{ graph.label }}</FeatherListItem
      >
      <!-- <FeatherListItem
        v-for="graph in powergridGraphs.graphs"
        :key="graph.label"
        @click="selectTopologyGraph(graph.namespace)"
        :class="{ 'selected' : graph.namespace === selectedNamespace }"
        >{{ graph.label }}</FeatherListItem
      > -->
    </FeatherList>
  </div>
</template>

<script
  setup
  lang="ts"
>
import { useStore } from 'vuex'
import { FeatherListItem, FeatherList } from '@featherds/list'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Close from '@featherds/icon/navigation/Cancel'

const store = useStore()
const graphsDisplay = computed(() => store.state.topologyModule.graphsDisplay)
const selectedNamespace = ref()

const selectTopologyGraph = (namespace: string) => {
  store.dispatch('topologyModule/getTopologyGraphByContainerAndNamespace', { containerId: store.state.topologyModule.graphsDisplay.id, namespace })
}

const namespace = computed(() => store.state.topologyModule.namespace)
watch(namespace, (ns) => {
  selectedNamespace.value = ns
})

const closeDrawer = () => store.dispatch('topologyModule/setRightDrawerState', false)
</script>

<style
  scoped
  lang="scss"
>
.topo-layers-drawer {
  width: 100%;
  height: 100%;
  padding: 10px;

  .close-btn {
    display: flex;
    width: 100%;
    justify-content: flex-end;
  }
}
</style>

