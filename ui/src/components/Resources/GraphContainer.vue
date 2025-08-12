<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="container" v-if="displayContainer">
        <div class="header">{{ resource.label }}</div>
        <div v-for="definition in resource.definitions" :key="definition">
          <Graph
            v-if="definitionsToDisplay.includes(definition)"
            :definition="definition"
            :resourceId="resource.id"
            :time="time"
            :label="resource.label"
            :isSingleGraph="isSingleGraph"
            @addGraphDefinition="$emit('addGraphDefinition')"
          />
        </div>
      </div>
    </div>
  </div>
</template>
  
<script setup lang="ts">
import { StartEndTime } from '@/types'
import { PropType } from 'vue'
import Graph from './Graph.vue'

defineEmits(['addGraphDefinition'])

const props = defineProps({
  resource: {
    required: true,
    type: Object as PropType<{ id: string, definitions: string[], label: string }>
  },
  time: {
    required: true,
    type: Object as PropType<StartEndTime>
  },
  definitionsToDisplay: {
    required: true,
    type: Array as PropType<string[]>
  },
  isSingleGraph: {
    required: true,
    type: Boolean
  }
})

const displayContainer = computed(() => {
  for (const definition of props.resource.definitions) {
    if (props.definitionsToDisplay.includes(definition)) {
      return true
    }
  }
  return false
})
</script>
  
<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.container {
  border: 2px solid var($shade-4);
  margin-bottom: 15px;

  .header {
    @include headline3();
    text-align: center;
    padding: 6px;
    background: var($shade-4);
  }
}
</style>
  