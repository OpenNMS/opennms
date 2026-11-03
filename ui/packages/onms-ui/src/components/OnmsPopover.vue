<template>
  <Popover
    ref="popoverRef"
    :appendTo="appendTo"
    :pt="unsafePt as never"
    @hide="emit('hide')"
  >
    <slot />
  </Popover>
</template>

<script setup lang="ts">
import Popover from 'primevue/popover'
import { ref } from 'vue'

// Seam wrapper (NMS-20081) around PrimeVue Popover. Anchored overlay panel;
// open programmatically via the exposed show/toggle from a trigger element.
// class falls through (panel styling hooks).
withDefaults(defineProps<{
  appendTo?: string
  unsafePt?: unknown
}>(), {
  appendTo: 'body',
  unsafePt: undefined
})

const emit = defineEmits<{
  hide: []
}>()

const popoverRef = ref<InstanceType<typeof Popover>>()

defineExpose({
  show: (event: unknown, target?: unknown) => popoverRef.value?.show(event as Event, target),
  hide: () => popoverRef.value?.hide(),
  toggle: (event: unknown) => popoverRef.value?.toggle(event as Event)
})
</script>
