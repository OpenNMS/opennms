<template>
  <div class="scv-list">
    <h3 class="title">Aliases</h3>
    <PListbox
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

import Listbox from 'primevue/listbox'
import { useScvStore } from '@/stores/scvStore'

const PListbox = Listbox

const scvStore = useScvStore()
const selectedAlias = ref<string | null>(null)
const aliases = computed<string[]>(() => scvStore.aliases)
const isEditing = computed<boolean>(() => scvStore.isEditing)
const listStyle = 'max-height: calc(100vh - 210px)'

const onChange = (event: { value: string | null }) => {
  if (event.value) {
    scvStore.getCredentialsByAlias(event.value)
  }
}

// The highlight only applies while an alias is being edited; clear it when the
// form stops editing (e.g. after Clear Form / adding new credentials).
watch(isEditing, (editing) => {
  if (!editing) {
    selectedAlias.value = null
  }
})
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";

.scv-list {
  .title {
    @include headline3;
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
