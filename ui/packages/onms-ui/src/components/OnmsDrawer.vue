<template>
  <Drawer
    :visible="visible"
    :position="position"
    :header="header"
    :style="width ? { width } : undefined"
    :block-scroll="true"
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
//
// `blockScroll` is forced on (PrimeVue defaults it to false): the drawer is
// always modal, and without it the wheel over an open drawer scrolls the page
// behind, so the page's scrollbar looked like the drawer's (NMS-20182). See the
// `html:has(body.p-overflow-hidden)` rule in main/App.vue for why the body class
// PrimeVue sets is not enough on its own.
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
