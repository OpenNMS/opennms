<template>
  <Tag
    :value="value"
    :severity="severity"
    :pt="unsafePt as never"
  />
</template>

<script setup lang="ts">
import Tag from 'primevue/tag'
import { OnmsTagSeverity } from '../types'

// Seam wrapper (NMS-20029) around PrimeVue Tag. Content comes from `value`
// only. class / data-* / aria-* fall through.
withDefaults(defineProps<{
  value?: string
  severity?: OnmsTagSeverity
  unsafePt?: unknown
}>(), {
  value: undefined,
  severity: undefined,
  unsafePt: undefined
})
</script>
