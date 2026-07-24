<template>
  <Dialog
    :visible="visible"
    :header="header"
    :modal="modal"
    :closable="closable"
    :draggable="draggable"
    :closeOnEscape="closeOnEscape"
    :appendTo="appendTo"
    :style="width ? { width } : undefined"
    :pt="unsafePt as never"
    @update:visible="emit('update:visible', $event)"
    @hide="emit('hide')"
  >
    <slot />
    <template
      v-if="$slots.footer"
      #footer
    >
      <slot name="footer" />
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import Dialog from 'primevue/dialog'

// Seam wrapper (NMS-20029) around PrimeVue Dialog. Sizing is expressed via the
// `width` prop (maps to an inline style) so consumers never touch PrimeVue
// styling APIs. Header content is text-only via the `header` prop.
withDefaults(defineProps<{
  visible: boolean
  header?: string
  width?: string
  modal?: boolean
  closable?: boolean
  draggable?: boolean
  closeOnEscape?: boolean
  appendTo?: string
  unsafePt?: unknown
}>(), {
  header: undefined,
  width: undefined,
  modal: true,
  closable: true,
  draggable: false,
  closeOnEscape: true,
  appendTo: 'body',
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  hide: []
}>()
</script>
