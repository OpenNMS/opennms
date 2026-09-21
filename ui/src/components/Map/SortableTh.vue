<template>
  <th
    scope="col"
    class="sortable-th"
    tabindex="0"
    :aria-sort="sort === SORT.ASCENDING ? 'ascending' : sort === SORT.DESCENDING ? 'descending' : 'none'"
    @click="onClick"
    @keydown.enter.prevent="onClick"
    @keydown.space.prevent="onClick"
  >
    <slot />
    <span class="sort-indicator" aria-hidden="true">{{ indicator }}</span>
  </th>
</template>

<!--
  Drop-in replacement for @featherds/table's FeatherSortHeader: a clickable
  column header that cycles ASC <-> DESC and emits `sort-changed` with the same
  { property, value } shape (value is the FeatherDS SORT enum), so the existing
  sortChanged handlers/stores work unchanged.
-->
<script setup lang="ts">
import { computed } from 'vue'
import { SORT } from '@/types'

const props = defineProps<{
  property: string
  sort: SORT
}>()

const emit = defineEmits<{
  (e: 'sort-changed', obj: { property: string, value: SORT }): void
}>()

const indicator = computed(() => {
  if (props.sort === SORT.ASCENDING) {
    return '▲'
  }
  if (props.sort === SORT.DESCENDING) {
    return '▼'
  }
  return ''
})

const onClick = () => {
  const value = props.sort === SORT.ASCENDING ? SORT.DESCENDING : SORT.ASCENDING
  emit('sort-changed', { property: props.property, value })
}
</script>

<style scoped lang="scss">
.sortable-th {
  cursor: pointer;
  user-select: none;

  .sort-indicator {
    margin-left: 4px;
    font-size: 0.75em;
  }
}
</style>
