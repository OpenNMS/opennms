<template>
  <Card :pt="unsafePt as never">
    <template
      v-if="$slots.title"
      #title
    >
      <slot name="title" />
    </template>
    <template
      v-if="$slots.content"
      #content
    >
      <slot name="content" />
    </template>
  </Card>
</template>

<script setup lang="ts">
import Card from 'primevue/card'

// Seam wrapper (NMS-20081) around PrimeVue Card. Structured content via
// title/content slots. class falls through.
withDefaults(defineProps<{
  unsafePt?: unknown
}>(), {
  unsafePt: undefined
})
</script>
