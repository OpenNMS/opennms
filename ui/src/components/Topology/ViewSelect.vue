<template>
  <FeatherDropdown>
    <template v-slot:trigger>
      <FeatherButton
        primary
        link
        href="#"
        menu-trigger
        >View</FeatherButton
      >
    </template>

    <!-- Views -->
    <FeatherDropdownItem
      v-for="({type, label}) in Views"
      :key="type"
    >
      <FeatherCheckbox
        @update:modelValue="selectView(type)"
        v-model="views[type]"
        >{{label}}</FeatherCheckbox
      >
    </FeatherDropdownItem>

    <!-- Displays -->
    <div v-if="isTopologyView && graphs.length">
      <hr />
      <!-- <FeatherDropdownItem>
        <FeatherCheckbox
          @update:modelValue="selectDisplay('linkd')"
          v-model="displays['linkd']"
        >Enhanced Linkd</FeatherCheckbox>
      </FeatherDropdownItem> -->

      <FeatherDropdownItem
        v-for="({id = '', label}) in graphs"
        :key="id"
      >
        <FeatherCheckbox
          @update:modelValue="selectDisplay(id)"
          v-model="displays[DisplayType[id]]"
          >{{label}}</FeatherCheckbox
        >
      </FeatherDropdownItem>
    </div>
  </FeatherDropdown>
</template>

<script
  setup
  lang="ts"
>
import { useStore } from 'vuex'
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherCheckbox } from '@featherds/checkbox'
import { Views, DisplayType } from './topology.constants'
import { TopologyGraphList } from '@/types/topology'

const store = useStore()

const views = ref<Record<string, boolean>>({ map: true }) //default view
const displays = ref<Record<string, boolean>>({ linkd: true }) //default display

const isTopologyView = computed<boolean>(() => store.state.topologyModule.isTopologyView)

const graphs = computed<TopologyGraphList[]>(() => store.getters['topologyModule/getGraphs'])

const selectView = (view: string) => {
  views.value = {} // reset
  views.value[view] = true // set selected
  store.dispatch('topologyModule/setSelectedView', view) // save to state
}

const selectDisplay = (display: string) => {
  displays.value = {} // reset
  displays.value[DisplayType[display]] = true // set selected
  store.dispatch('topologyModule/setSelectedDisplay', DisplayType[display]) // save to state
}
</script>

<style
  scoped
  lang="scss"
>
.view-select {
  width: 15rem;
}
</style>
<style lang="scss">
@import "@featherds/dropdown/scss/mixins";

body > .feather-menu-dropdown > .feather-dropdown {
  @include dropdown-menu-height(8); // to have the view dropdown list of 8 items
}
</style>

