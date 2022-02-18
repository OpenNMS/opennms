<template>
  <FeatherDrawer
    id="map-left-drawer"
    :left="true"
    :modelValue="isOpen" 
    @update:modelValue="closeDrawer"
    :labels="{ close: 'close', title: 'View and Search' }"
    width="20em"
  >
    <div class="container">
      <slot name="search"></slot>
      <slot name="view"></slot>
    </div>
  </FeatherDrawer>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { FeatherDrawer } from '@featherds/drawer'

const store = useStore()
const isOpen = computed<boolean>(() => store.state.topologyModule.isLeftDrawerOpen)
const closeDrawer = () => store.dispatch('topologyModule/closeLeftDrawer')

onMounted(() => {
  // remove feather default focus
  const focusTrap = document.querySelector('#map-left-drawer > .content > .focus-trap-content')
  if (focusTrap) {

    var fragment = document.createDocumentFragment()
    while (focusTrap.firstChild) {
      fragment.appendChild(focusTrap.firstChild)
    }

    if (focusTrap.parentNode) {
      focusTrap.parentNode.replaceChild(fragment, focusTrap)
    }
  }
})
</script>

<style lang="scss">
#map-left-drawer {
  .container {
    padding: 20px;
  }
  .content {
    height: auto;
  }
  .greyedOut {
    display: none;
  }
}
</style>
