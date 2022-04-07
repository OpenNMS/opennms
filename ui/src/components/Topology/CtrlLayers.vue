<template>
  <FeatherButton :disabled="!hasPowerGridGraphs" icon="Layers" @click="openLayersSidebar">
    <FeatherIcon :icon="Menu"></FeatherIcon>
  </FeatherButton>
</template>

<script setup lang="ts">
import { useStore } from 'vuex'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import Menu from '@featherds/icon/navigation/Menu'

const store = useStore()
const hasPowerGridGraphs = computed<boolean>(() => store.getters['topologyModule/hasPowerGridGraphs'])

const openLayersSidebar = () => store.dispatch('topologyModule/openRightDrawer')
</script>
