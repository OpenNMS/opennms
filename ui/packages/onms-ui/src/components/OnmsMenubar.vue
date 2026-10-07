<template>
  <Menubar
    :model="items as never"
    :pt="pt as never"
  >
    <template
      v-if="$slots.start"
      #start
    >
      <slot name="start" />
    </template>
    <template
      v-if="$slots.end"
      #end
    >
      <slot name="end" />
    </template>
    <template
      v-if="$slots.item"
      #item="slotProps"
    >
      <slot
        name="item"
        v-bind="slotProps"
      />
    </template>
  </Menubar>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Menubar from 'primevue/menubar'
import { OnmsMenuItem } from '../types'

// Seam wrapper (NMS-20303) around PrimeVue Menubar: a horizontal row of top-level items, each
// opening its nested `items` as a dropdown. Always inline, unlike the popup OnmsMenu and
// OnmsTieredMenu; an item's `command` runs on selection, as in those. Reuses OnmsMenuItem, which
// already nests.
//
// The #item slot forwards the underlying slot props ({ item, props, ... }) -- the same accepted
// seam leakage as OnmsMenu and OnmsTieredMenu, documented in the README. #start and #end place
// content before and after the items. `items as never` on :model -- see the note in OnmsMenu.vue.
//
// An item with `target: '_blank'` gets rel="noopener noreferrer" on its link: PrimeVue renders the
// target but has no rel of its own. An `itemLink` in unsafePt replaces this.
const props = withDefaults(defineProps<{
  items?: OnmsMenuItem[]
  unsafePt?: unknown
}>(), {
  items: undefined,
  unsafePt: undefined
})

const relForNewTab = ({ context }: { context?: { item?: OnmsMenuItem }}) =>
  (context?.item?.target === '_blank' ? { rel: 'noopener noreferrer' } : {})

const pt = computed(() => ({ itemLink: relForNewTab, ...(props.unsafePt as object | undefined) }))
</script>
