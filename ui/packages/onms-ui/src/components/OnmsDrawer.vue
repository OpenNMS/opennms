<template>
  <Drawer
    :visible="visible"
    :position="position"
    :header="header"
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
  </Drawer>
</template>

<script setup lang="ts">
import Drawer from 'primevue/drawer'

// Seam wrapper (NMS-20081) around PrimeVue Drawer. Opens from the right by
// design (every OpenNMS drawer does; PrimeVue's own default is left). Sizing
// via `width` (inline style) so consumers never touch styling APIs. Modal +
// dismissable behavior uses PrimeVue defaults (both true).
withDefaults(defineProps<{
  visible: boolean
  position?: 'left' | 'right' | 'top' | 'bottom' | 'full'
  header?: string
  width?: string
  unsafePt?: unknown
}>(), {
  position: 'right',
  header: undefined,
  width: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  hide: []
}>()
</script>
