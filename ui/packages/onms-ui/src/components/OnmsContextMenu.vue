<template>
  <ContextMenu
    ref="menuRef"
    :model="items as never"
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
  </ContextMenu>
</template>

<script setup lang="ts">
import ContextMenu from 'primevue/contextmenu'
import { ref } from 'vue'
import { OnmsMenuItem } from '../types'

// Seam wrapper (NMS-20029) around PrimeVue ContextMenu. Distinct from
// OnmsMenu: a popup OnmsMenu positions itself against the trigger *element*,
// while this positions at the pointer coordinates carried by the event, which
// is what a right-click on a canvas or table row needs. Open it from a
// @contextmenu handler via the exposed show(event).
//
// The #item slot forwards the underlying slot props ({ item, props }) — same
// accepted seam leakage as OnmsMenu, documented in the README.
// `items as never` on :model — see the note in OnmsMenu.vue.
withDefaults(defineProps<{
  items?: OnmsMenuItem[]
  appendTo?: string
  unsafePt?: unknown
}>(), {
  items: undefined,
  appendTo: 'body',
  unsafePt: undefined
})

const menuRef = ref<InstanceType<typeof ContextMenu>>()

defineExpose({
  show: (event: Event) => menuRef.value?.show(event),
  hide: () => menuRef.value?.hide()
})
</script>
