<template>
  <FeatherButton :disabled="disable" icon="Layers" @click="openLayersSidebar">
    <FeatherIcon :icon="Menu"></FeatherIcon>
  </FeatherButton>
</template>

<script setup lang="ts">
import { useStore } from 'vuex'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import Menu from '@featherds/icon/navigation/Menu'
import { PowerGrid } from './topology.constants'

const store = useStore()
const disable = computed<boolean>(() => store.state.topologyModule.selectedDisplay !== PowerGrid)
const isOpen = computed<boolean>(() => store.state.topologyModule.isRightDrawerOpen)

const openLayersSidebar = () => store.dispatch('topologyModule/setRightDrawerState', !isOpen.value)
</script>
