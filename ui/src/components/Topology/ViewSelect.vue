<template>
  <FeatherDropdown>
    <template v-slot:trigger>
      <FeatherButton primary link href="#" menu-trigger>View</FeatherButton>
    </template>

    <!-- Views -->
    <FeatherDropdownItem>
      <FeatherCheckbox @update:modelValue="selectView('map')" v-model="views['map']">Map Layout</FeatherCheckbox>
    </FeatherDropdownItem>

    <FeatherDropdownItem>
      <FeatherCheckbox @update:modelValue="selectView('d3')" v-model="views['d3']">D3 Layout</FeatherCheckbox>
    </FeatherDropdownItem>

    <FeatherDropdownItem>
      <FeatherCheckbox
        @update:modelValue="selectView('circle')"
        v-model="views['circle']"
      >Circle Layout</FeatherCheckbox>
    </FeatherDropdownItem>

    <div v-if="isTopologyView">
      <hr />

      <!-- Displays -->
      <FeatherDropdownItem>
        <FeatherCheckbox
          @update:modelValue="selectDisplay('linkd')"
          v-model="displays['linkd']"
        >Enhanced Linkd</FeatherCheckbox>
      </FeatherDropdownItem>

      <FeatherDropdownItem v-if="hasPowerGridGraphs">
        <FeatherCheckbox
          @update:modelValue="selectDisplay('powerGrid')"
          v-model="displays['powerGrid']"
        >PowerGrid</FeatherCheckbox>
      </FeatherDropdownItem>
    </div>
  </FeatherDropdown>
</template>

<script setup lang="ts">
import { useStore } from 'vuex'
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherCheckbox } from '@featherds/checkbox'

const store = useStore()

const views = ref<Record<string, boolean>>({ map: true }) //default view
const displays = ref<Record<string, boolean>>({ linkd: true }) //default display

const hasPowerGridGraphs = computed<boolean>(() => store.getters['topologyModule/hasPowerGridGraphs'])
const isTopologyView = computed<boolean>(() => store.state.topologyModule.isTopologyView)

const selectView = (view: string) => {
  views.value = {} // reset
  views.value[view] = true // set selected
  store.dispatch('topologyModule/setSelectedView', view) // save to state
}

const selectDisplay = (display: string) => {
  displays.value = {} // reset
  displays.value[display] = true // set selected
  store.dispatch('topologyModule/setSelectedDisplay', display) // save to state
}
</script>

<style scoped lang="scss">
.view-select {
  width: 15rem;
}
</style>
