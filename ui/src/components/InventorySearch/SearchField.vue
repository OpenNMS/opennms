<template>
  <form class="search-field" :data-test="`criterion-${testId}`" @submit.prevent="emit('search')">
    <div class="field-label">
      <label :for="props.for">{{ label }}</label>
      <OnmsIconButton
        v-if="help"
        :icon="InfoIcon"
        iconSize="1rem"
        variant="text"
        :tooltip="help"
        :title="help"
        :aria-label="`Help: ${label}`"
        class="field-help"
        :data-test="`${testId}-help`"
      />
    </div>
    <div class="field-control">
      <slot />
      <OnmsIconButton
        :icon="SearchIcon"
        variant="filled"
        type="submit"
        :aria-label="`Search by ${label.toLowerCase()}`"
        class="field-search"
        :data-test="`${testId}-search`"
      />
    </div>
  </form>
</template>

<script setup lang="ts">
import { OnmsIconButton } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'
import SearchIcon from '@opennms/onms-ui/icons/action/Search.vue'

const props = defineProps<{
  label: string
  for?: string
  // hover help describing what the field searches
  help?: string
  testId: string
}>()

const emit = defineEmits<{
  search: []
}>()
</script>

<style scoped lang="scss">
.search-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;

  .field-label {
    display: flex;
    align-items: center;
    gap: 0.25rem;

    label {
      font-weight: 600;
    }

    .field-help {
      // pull the small info icon tight against the label
      margin: 0;
    }
  }

  .field-control {
    display: flex;
    align-items: stretch;
    gap: 0.5rem;

    // share the row evenly so a fluid input doesn't crowd out the selects
    // (which otherwise truncate to "sysD…"); the search button keeps its size
    :deep(> *:not(.field-search)) {
      flex: 1 1 0;
      min-width: 0;
    }

    .field-search {
      flex: 0 0 auto;
    }
  }
}
</style>
