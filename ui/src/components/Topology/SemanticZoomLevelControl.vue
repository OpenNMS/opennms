<template>
  <FeatherButton icon="ExpandLess" @click="increaseSemanticZoomLevel">
    <FeatherIcon :icon="ExpandLess"></FeatherIcon>
  </FeatherButton>
  <FeatherChip>{{ semanticZoomlevel }}</FeatherChip>
  <FeatherButton icon="ExpandMore" @click="decreaseSemanticZoomLevel">
    <FeatherIcon :icon="ExpandMore"></FeatherIcon>
  </FeatherButton>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useStore } from 'vuex'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import { FeatherChip } from '@featherds/chips'
import ExpandLess from '@featherds/icon/navigation/ExpandLess'
import ExpandMore from '@featherds/icon/navigation/ExpandMore'

const store = useStore()
const semanticZoomlevel = computed<number>(() => store.state.topologyModule.semanticZoomLevel)

const increaseSemanticZoomLevel = () => {
  const updatedSemanticZoomLevel = semanticZoomlevel.value + 1
  store.dispatch('topologyModule/setSemanticZoomLevel', updatedSemanticZoomLevel)
}

const decreaseSemanticZoomLevel = () => {
  if (semanticZoomlevel.value === 0) return
  const updatedSemanticZoomLevel = semanticZoomlevel.value - 1
  store.dispatch('topologyModule/setSemanticZoomLevel', updatedSemanticZoomLevel)
}
</script>
