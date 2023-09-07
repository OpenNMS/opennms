<template>
  <FeatherDropdown>
    <template v-slot:trigger="{ attrs, on }">
      <FeatherButton
        icon="Select Columns"
        v-bind="attrs"
        v-on="on"
      >
        <FeatherIcon :icon="settingsIcon" class="node-actions-icon" />
      </FeatherButton>
    </template>
    <div class="node-actions-reset">
      <FeatherButton secondary @click="resetToDefault">Default</FeatherButton>
    </div>
    <FeatherDropdownItem
      v-for="col in columns"
      :key="col.id"
      >
        <div class="column-select-item-wrapper">
          <FeatherCheckbox
            class="checkbox"
            @update:modelValue="selectColumn(col)"
            :modelValue="col.selected"
          >{{ col.label }}</FeatherCheckbox>
        </div>
    </FeatherDropdownItem>
  </FeatherDropdown>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherCheckbox } from '@featherds/checkbox'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import Settings from '@featherds/icon/action/Settings'
import { markRaw } from 'vue'
import { useStore } from 'vuex'
import { NodeColumnSelectionItem } from '@/types'

const store = useStore()
const settingsIcon = markRaw(Settings)

const columns = computed<NodeColumnSelectionItem[]>(() => store.state.nodeStructureModule.columns)

const selectColumn = (col: NodeColumnSelectionItem) => {
  const newItem = {
    ...col,
    selected: !col.selected
  }

  store.dispatch('nodeStructureModule/updateNodeColumnSelection', newItem)
  return false
}

const resetToDefault = () => {
  store.dispatch('nodeStructureModule/resetColumnSelectionToDefault')
  return false
}
</script>

<style lang="scss" scoped>
.focus-icon {
  cursor: pointer;
}

button.btn.btn-icon .node-actions-icon {
  font-size: 1.1rem;
}

.column-select-item-wrapper {
  padding-left: 0.5em;
}

.node-actions-reset {
  padding-left: 0.5em;
  margin-bottom: 1em;
}
</style>
