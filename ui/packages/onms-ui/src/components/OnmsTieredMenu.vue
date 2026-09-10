<template>
  <TieredMenu
    ref="menuRef"
    :model="items as never"
    :popup="popup"
    :appendTo="appendTo"
    :pt="unsafePt as never"
  >
    <template
      v-if="$slots.item"
      #item="slotProps"
    >
      <slot
        name="item"
        v-bind="slotProps"
      />
    </template>
  </TieredMenu>
</template>

<script setup lang="ts">
import TieredMenu from 'primevue/tieredmenu'
import { ref } from 'vue'
import { OnmsMenuItem } from '../types'

// Seam wrapper (NMS-20029) around PrimeVue TieredMenu. Distinct from OnmsMenu:
// OnmsMenu renders a nested `items` array as a flat section with its parent as
// a heading, while this renders it as a hover-opened submenu, so it is the
// wrapper for menus more than one level deep. Popup by default, matching
// OnmsMenu; open via the exposed toggle(event).
//
// The #item slot forwards the underlying slot props ({ item, props }) — same
// accepted seam leakage as OnmsMenu, documented in the README.
// `items as never` on :model — see the note in OnmsMenu.vue.
withDefaults(defineProps<{
  items?: OnmsMenuItem[]
  popup?: boolean
  appendTo?: string
  unsafePt?: unknown
}>(), {
  items: undefined,
  popup: true,
  appendTo: 'body',
  unsafePt: undefined
})

const menuRef = ref<InstanceType<typeof TieredMenu>>()

defineExpose({
  toggle: (event: Event) => menuRef.value?.toggle(event),
  hide: () => menuRef.value?.hide()
})
</script>
