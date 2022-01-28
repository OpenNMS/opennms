<template>
  <FeatherDialog v-model="visible" :labels="labels" :hide-close="true">
    <p class="my-content">This will delete the definition titled: {{ title }}</p>
    <template v-slot:footer>
      <FeatherButton primary @click="props?.doubleCheckSelected(false)">No</FeatherButton>
      <FeatherButton error @click="props?.doubleCheckSelected(true)">Yes</FeatherButton>
    </template>
  </FeatherDialog>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

import { FeatherDialog } from '@featherds/dialog'
import { FeatherButton } from '@featherds/button'

/**
 * Props
 */
const props = defineProps({
  title: String,
  optionSelected: Object,
  doubleCheckSelected: {
    type: Function,
    required: true
  }
})

/**
 * Local State
 */
const visible = computed(() => props?.optionSelected?.active)
const title = computed(() => props?.optionSelected?.title)
const labels = computed(() => ({ title: 'Are you sure?', close: 'Close' }))
</script>