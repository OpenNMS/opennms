<template>
  <div id="adhoc-card">
    <AdhocGraphBuilder :viewOnly="viewOnly" />
  </div>
</template>

<script setup lang="ts">
import AdhocGraphBuilder from '@/components/AdhocGraphs/AdhocGraphBuilder.vue'

// Set by the /adhoc-graphs/view route, which renders the graph on its own.
withDefaults(defineProps<{ viewOnly?: boolean }>(), { viewOnly: false })
</script>

<style scoped lang="scss">
@import '@/styles/onms-elevation';

#adhoc-card {
  @include onms-elevation(2);
  background: var(--p-content-background);
  padding: 15px;
  height: calc(100vh - 110px);
  // The builder owns scrolling: it scrolls as a whole while building, and not at
  // all once expanded, where the chart is sized to the space that is left.
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
