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
    <FeatherDropdownItem>
      <FeatherCheckbox
        @update:modelValue="selectView(ViewType.map)"
        v-model="views[ViewType.map]"
        >Map Layout</FeatherCheckbox
      >
    </FeatherDropdownItem>

    <FeatherDropdownItem>
      <FeatherCheckbox
        @update:modelValue="selectView(ViewType.d3)"
        v-model="views[ViewType.d3]"
        >D3 Layout</FeatherCheckbox
      >
    </FeatherDropdownItem>

    <FeatherDropdownItem>
      <FeatherCheckbox
        @update:modelValue="selectView(ViewType.circle)"
        v-model="views[ViewType.circle]"
        >Circle Layout</FeatherCheckbox
      >
    </FeatherDropdownItem>

    <div v-if="isTopologyView">
      <hr />
      <!-- Displays -->
      <!-- <FeatherDropdownItem v-if="hasPowergridGraphs">
        <FeatherCheckbox
          @update:modelValue="selectDisplay(DisplayType.powergrip)"
          v-model="displays[DisplayType.powergrip]"
          >PowerGrid</FeatherCheckbox
        >
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
import { ViewType, DisplayType } from './topology.constants'
import { TopologyGraphList } from '@/types/topology'

const store = useStore()

const views = ref<Record<string, boolean>>({ map: true }) //default view
const displays = ref<Record<string, boolean>>({ linkd: true }) //default display

const isTopologyView = computed<boolean>(() => store.state.topologyModule.isTopologyView)

const graphs = computed<TopologyGraphList[]>(() => {
  return store.getters['topologyModule/getGraphs']
})

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

