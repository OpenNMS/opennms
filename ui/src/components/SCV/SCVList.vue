<template>
  <div class="scv-list">
    <h3 class="title">Aliases</h3>
    <OnmsListbox
      v-model="selectedAlias"
      :options="aliases"
      class="alias-listbox"
      :listStyle="listStyle"
      @change="onChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsListbox } from '@opennms/onms-ui'
import { useScvStore } from '@/stores/scvStore'

const scvStore = useScvStore()
const selectedAlias = ref<string | null>(null)
const aliases = computed<string[]>(() => scvStore.aliases)
const isEditing = computed<boolean>(() => scvStore.isEditing)
const listStyle = 'max-height: calc(100vh - 210px)'

// PrimeVue Listbox single-select treats a click on the already-selected option
// as a toggle: it emits update:modelValue with null (clearing selectedAlias)
// before emitting @change. We track the loaded alias separately so a re-click
// reloads the same alias and keeps the highlight, matching the old
// FeatherListItem behavior that reloaded on every click.
let loadedAlias: string | null = null

const onChange = (newValue: unknown) => {
  const value = newValue as string | null
  const alias = value ?? loadedAlias
  if (!alias) {
    return
  }
  loadedAlias = alias
  selectedAlias.value = alias
  scvStore.getCredentialsByAlias(alias)
}

// The highlight only applies while an alias is being edited; clear it when the
// form stops editing (e.g. after Clear Form / adding new credentials).
watch(isEditing, (editing) => {
  if (!editing) {
    selectedAlias.value = null
    loadedAlias = null
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/onms-typography';

.scv-list {
  .title {
    @include onms-headline3;
    margin: 0 0 0.5rem 0;
  }

  .alias-listbox {
    width: 100%;

    :deep(.p-listbox-option) {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}
</style>
