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
    <!--
      Requires custom CSS for FeatherCheckbox to behave as radio
        - add 'disabled' attribute to checkbox element: true when checked, hence preventing it from being deselected
        - set disabled checkbox element to 'feather-primary' color (see style section at bottom)
      i.e. FeatherRadioGroup/FeatherRadio is not working well with FeatherDropdown (@0.10.12)
    -->
    <FeatherDropdownItem
      v-for="({type, label}) in Views"
      :key="type"
      class="view-select-layout-dropdown-item"
    >
      <FeatherCheckbox
        @update:modelValue="selectView(type)"
        v-model="views[type]"
        :disabled="views[type]"
        >{{label}}</FeatherCheckbox
      >
    </FeatherDropdownItem>
    <!-- Displays -->
    <div v-if="isTopologyView && graphs.length">
      <hr />
      <!-- <FeatherDropdownItem>
              <FeatherCheckbox
                @update:modelValue="selectDisplay('nodes')"
                v-model="displays['nodes']"
              >Nodes (Enhanced Linkd)</FeatherCheckbox>
            </FeatherDropdownItem> -->
      <FeatherDropdownItem
        v-for="({id = '', label}) in graphs"
        :key="id"
        class="view-select-display-dropdown-item"
      >
        <FeatherCheckbox
          @update:modelValue="selectDisplay(id)"
          v-model="displays[DisplayType[id]]"
          :disabled="displays[DisplayType[id]]"
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

const views = computed<Record<string, boolean>>(() => ({[store.state.topologyModule.selectedView]: true}))
const displays = computed<Record<string, boolean>>(() => ({[store.state.topologyModule.selectedDisplay]: true}))

const isTopologyView = computed<boolean>(() => store.state.topologyModule.isTopologyView)

const graphs = computed<TopologyGraphList[]>(() => store.getters['topologyModule/getGraphs'])

const selectView = (view: string) => {
  store.dispatch('topologyModule/setSelectedView', view) // save to state
}

const selectDisplay = (display: string) => {
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
@import "@featherds/styles/themes/variables";

body > .feather-menu-dropdown > .feather-dropdown {
  @include dropdown-menu-height(8); // to have the view dropdown list of 8 items
  // custom CSS for FeatherCheckbox to behave as radio; the disabled checked item (checkbox and label) have same color than the other checkable items
  .view-select-layout-dropdown-item,
  .view-select-display-dropdown-item {
    .layout-container {
      .feather-checkbox[aria-disabled="true"] {
        > .checkbox > .box {
          border-color: var(--feather-primary);
          background-color: var(--feather-primary);
        }
        > label {
          color: var(--feather-primary-text-on-surface);
        }
      }
    }
  }
}
</style>

