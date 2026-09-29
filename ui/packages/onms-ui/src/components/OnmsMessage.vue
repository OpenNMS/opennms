<template>
  <Message
    :severity="severity"
    :closable="false"
    :pt="unsafePt as never"
  >
    <slot />
  </Message>
</template>

<script setup lang="ts">
import Message from 'primevue/message'
import { OnmsMessageSeverity } from '../types'

// Seam wrapper (NMS-20029) around PrimeVue Message for an inline callout box.
// Content comes from the default slot. Never closable: a callout states a fact
// the reader must see, and dismissal would only hide it until the next render.
// class / data-* / aria-* fall through.
withDefaults(defineProps<{
  severity?: OnmsMessageSeverity
  unsafePt?: unknown
}>(), {
  severity: 'info',
  unsafePt: undefined
})
</script>
