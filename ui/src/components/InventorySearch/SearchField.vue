<template>
  <form
    class="search-field"
    :data-test="`criterion-${testId}`"
    novalidate
    @submit.prevent="emit('search')"
  >
    <FormField
      :label="label"
      :for="props.for"
      :info="help"
      :error="error"
    >
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
    </FormField>
  </form>
</template>

<script setup lang="ts">
import { OnmsIconButton } from '@opennms/onms-ui'
import SearchIcon from '@opennms/onms-ui/icons/action/Search.vue'

import FormField from '@/components/Common/FormField.vue'

const props = defineProps<{
  label: string
  for?: string
  // hover help describing what the field searches
  help?: string
  error?: string
  testId: string
}>()

const emit = defineEmits<{
  search: []
}>()
</script>

<style scoped lang="scss">
.search-field {
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
