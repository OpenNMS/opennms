<template>
  <Menubar
    :key="breakpoint ?? 'viewport'"
    :model="items as never"
    :breakpoint="breakpoint"
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
//
// `collapsed` decides between the full row and the hamburger button. Left out, PrimeVue decides by
// the viewport width (its 960px breakpoint). Given, the caller decides -- for a bar that shares its
// row with other content, whether it fits depends on that row, not on the viewport.
const props = withDefaults(defineProps<{
  items?: OnmsMenuItem[]
  collapsed?: boolean
  unsafePt?: unknown
}>(), {
  items: undefined,
  collapsed: undefined,
  unsafePt: undefined
})

// PrimeVue only has a viewport media query, (max-width: <breakpoint>), so `collapsed` picks one
// that always matches or never does. It reads the breakpoint once, when it mounts, and does not
// watch it, so the :key above remounts it when `collapsed` changes.
const ALWAYS = '1000000px'
const NEVER = '0px'

const breakpoint = computed<string | undefined>(() =>
  props.collapsed === undefined ? undefined : (props.collapsed ? ALWAYS : NEVER))

const relForNewTab = ({ context }: { context?: { item?: OnmsMenuItem }}) =>
  (context?.item?.target === '_blank' ? { rel: 'noopener noreferrer' } : {})

const pt = computed(() => ({ itemLink: relForNewTab, ...(props.unsafePt as object | undefined) }))
</script>
