<template>
  <FeatherButton
    v-if="!focusIsHighlighted"
    icon="Highlight focused nodes."
    @click="highlightFocusedNodes"
  >
    <FeatherIcon :icon="View"></FeatherIcon>
  </FeatherButton>

  <FeatherButton
    v-else
    icon="Remove highlight from focused nodes."
    @click="removeHighlightFromFocusedNodes"
  >
    <FeatherIcon :icon="Hide"></FeatherIcon>
  </FeatherButton>
</template>

<script setup lang="ts">
import { useStore } from 'vuex'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import View from '@featherds/icon/action/View'
import Hide from '@featherds/icon/action/Hide'

const store = useStore()
const focusIsHighlighted = computed<boolean>(() => store.state.topologyModule.highlightFocusedNodes)

const highlightFocusedNodes = () => store.dispatch('topologyModule/highlightFocusedNodes', true)
const removeHighlightFromFocusedNodes = () => store.dispatch('topologyModule/highlightFocusedNodes', false)
</script>
