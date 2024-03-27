<template>
  <FeatherList class="scv-list">
    <FeatherListHeader class="title">
      Aliases
    </FeatherListHeader>
    <FeatherListItem
      v-for="alias of aliases" 
      :selected="selectedAlias === alias && isEditing" 
      :key="alias"
      @click="onAliasClick(alias)">
      {{ alias }}
    </FeatherListItem>
  </FeatherList>
</template>

<script setup lang="ts">
import { FeatherList, FeatherListHeader, FeatherListItem } from '@featherds/list'
import { useScvStore } from '@/stores/scvStore'

const scvStore = useScvStore()
const selectedAlias = ref()
const aliases = computed<string[]>(() => scvStore.aliases)
const isEditing = computed<boolean>(() => scvStore.isEditing)

const onAliasClick = (alias: string) => {
  selectedAlias.value = alias
  scvStore.getCredentialsByAlias(alias)
}
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";

.scv-list {
  @include elevation(2);
  background: var($surface);
  height: calc(100vh - 150px);
  overflow-y: auto;

  .title {
    @include headline3
  }
}
</style>

<style lang="scss">
.scv-list {
  .feather-list-item-text {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
