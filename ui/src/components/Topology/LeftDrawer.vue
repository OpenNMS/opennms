<template>
  <FeatherDrawer
    v-if="!isRightDrawerOpen"
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
import { useStore } from 'vuex'
import { FeatherDrawer } from '@featherds/drawer'

const store = useStore()
const isOpen = computed<boolean>(() => store.state.topologyModule.isLeftDrawerOpen)
const isRightDrawerOpen = computed<boolean>(() => store.state.topologyModule.isRightDrawerOpen)
const closeDrawer = () => store.dispatch('topologyModule/closeLeftDrawer')

onMounted(() => {
  // remove feather default focus
  const focusTrap = document.querySelector('#map-left-drawer > .content > .focus-trap-content')
  if (focusTrap) {

    const fragment = document.createDocumentFragment()
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
    .search-bar {
      width: 15rem;
    }
  }
  .content {
    height: auto;
  }
  .greyedOut {
    display: none;
  }
}
</style>
