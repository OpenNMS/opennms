<template>
  <div class="column-select-container">
    <div class="feather-row node-actions-reset">
      <div class="feather-col-9">
      </div>
      <div class="feather-col-3 centered">
        <FeatherButton secondary @click="resetToDefault">Default</FeatherButton>
      </div>
    </div>
  </div>
  <div
    v-for="(col, index) in columns"
    :key="col.id"
    >
      <div class="feather-row column-select-item-wrapper">
        <div class="feather-col-9">
          <FeatherCheckbox
            class="checkbox"
            @update:modelValue="selectColumn(col)"
            :modelValue="col.selected"
          >{{ col.label }}</FeatherCheckbox>
        </div>
        <div class="feather-col-3 centered">
          <FeatherIcon :icon="upIcon" title="Move Up" @click="columnMove(true, index)" :class="getOrderIconCssClasses(true, index)" />
          <FeatherIcon :icon="downIcon" title="Move Down" @click="columnMove(false, index)" :class="getOrderIconCssClasses(false, index)" />
        </div>
      </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherCheckbox } from '@featherds/checkbox'
import { FeatherIcon } from '@featherds/icon'
import KeyboardArrowUp from '@featherds/icon/hardware/KeyboardArrowUp'
import KeyboardArrowDown from '@featherds/icon/hardware/KeyboardArrowDown'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { NodeColumnSelectionItem } from '@/types'

const nodeStructureStore = useNodeStructureStore()

const upIcon = markRaw(KeyboardArrowUp)
const downIcon = markRaw(KeyboardArrowDown)

const columns = computed<NodeColumnSelectionItem[]>(() => nodeStructureStore.columns)

const selectColumn = (col: NodeColumnSelectionItem) => {
  const newItem = {
    ...col,
    selected: !col.selected
  }

  nodeStructureStore.updateNodeColumnSelection(newItem)
}

const columnMove = (isUp: boolean, index: number) => {
  if ((isUp && index === 0) || (!isUp && index >= columns.value.length - 1)) {
    return
  }

  const newCols = [...columns.value]
  const movingValue = newCols.splice(index, 1)

  if (isUp) {
    newCols.splice(index - 1, 0, movingValue[0])
  } else {
    newCols.splice(index + 1, 0, movingValue[0])
  }

  newCols.forEach((col, i) => col.order = i)

  nodeStructureStore.setNodeColumnSelection(newCols)
}

const getOrderIconCssClasses = (isUp: boolean, index: number) => {
  const classes = ['column-order-icon']

  if (!isUp) {
    classes.push('column-order-icon-down')
  }

  if (isUp && index > 0 || !isUp && index < (columns.value.length - 1)) {
    classes.push('column-order-icon-active')
  } else {
    classes.push('column-order-icon-inactive')
  }

  return classes
}

const resetToDefault = () => {
  nodeStructureStore.resetColumnSelectionToDefault()
}
</script>

<style lang="scss" scoped>
@import "@featherds/table/scss/table";

.focus-icon {
  cursor: pointer;
}

button.btn.btn-icon .node-actions-icon {
  font-size: 1.1rem;
}

.column-select-container {
  padding: 4px;
}

.column-select-item-wrapper {
  padding-left: 0.5em;
}

.node-actions-reset {
  margin-bottom: 1em;
}
.column-order-icon {
  font-size: 1.75em;

  &-active {
    cursor: pointer;
  }

  &-inactive {
    color: #ccc;
  }

  &-down {
    margin-left: 4px;
  }
}

.feather-col-3.centered {
  text-align: center;
}
</style>
