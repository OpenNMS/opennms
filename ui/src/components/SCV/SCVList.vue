<template>
  <FeatherList>
    <FeatherListHeader>
      Aliases
    </FeatherListHeader>
    <FeatherListItem
      v-for="alias of aliases" 
      :selected="selectedAlias === alias && isEditing" 
      :key="alias"
      @click="onAliasClick(alias)">
      <span class="alias">{{ alias }}</span>
    </FeatherListItem>
    <FeatherListItem selected v-if="!aliases.length">
      No aliases available. Please create one.
    </FeatherListItem>
  </FeatherList>
</template>

<script setup lang="ts">
import { FeatherList, FeatherListHeader, FeatherListItem } from '@featherds/list'
import { useStore } from 'vuex'

const store = useStore()
const selectedAlias = ref()
const aliases = computed<string[]>(() => store.state.scvModule.aliases)
const isEditing = computed<boolean>(() => store.state.scvModule.isEditing)

const onAliasClick = (alias: string) => {
  selectedAlias.value = alias
  store.dispatch('scvModule/getCredentialsByAlias', alias)
}
</script>

<style lang="scss" scoped>
.alias {
  text-transform: capitalize;
}
</style>
