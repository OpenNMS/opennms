<template>
  <FeatherDrawer
    id="map-left-drawer"
    :left="true"
    :modelValue="isOpen"
    @update:modelValue="closeDrawer"
    :labels="{ close: 'close', title: 'View and Search' }"
  >
    <div class="container">
      <slot name="search"></slot>
      <slot name="view"></slot>
    </div>
  </FeatherDrawer>
</template>

<script
  setup
  lang="ts"
>
import { useStore } from 'vuex'
import { FeatherDrawer } from '@featherds/drawer'

const store = useStore()
const isOpen = computed<boolean>(() => store.state.topologyModule.isLeftDrawerOpen)

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
@import "@featherds/styles/themes/variables";

#map-left-drawer {
  .container {
    padding: 20px 50px 20px 20px;
    .search-bar {
      width: 15rem;
    }
  }
  .content {
    height: auto;
    top: unset;
    left: unset;
  }
  .greyedOut {
    display: none;
  }
}

body > .feather-menu-dropdown {
    z-index: calc(var(--feather-zindex-modal) + 1) !important; // to have the list diplayed above search content element
  }
</style>

