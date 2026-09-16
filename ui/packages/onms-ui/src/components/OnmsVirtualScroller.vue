<template>
  <VirtualScroller
    :items="items"
    :itemSize="itemSize"
    :scrollHeight="scrollHeight"
    :pt="unsafePt as never"
  >
    <template #item="{ item, options }">
      <slot
        name="item"
        :item="item"
        :index="options.index"
      />
    </template>
  </VirtualScroller>
</template>

<script setup lang="ts">
import VirtualScroller from 'primevue/virtualscroller'

// Seam wrapper (NMS-20029) around PrimeVue VirtualScroller for long,
// uniform-height lists that are not tables (OnmsTable has its own
// virtualScrollItemSize for the table case).
//
// The #item slot is narrowed rather than forwarded: PrimeVue passes an
// `options` object of internal render bookkeeping, of which only `index` is
// meaningful to a consumer, so the slot exposes { item, index } in OpenNMS
// vocabulary instead of leaking that object.
withDefaults(defineProps<{
  items?: unknown[]
  itemSize?: number
  scrollHeight?: string
  unsafePt?: unknown
}>(), {
  items: () => [],
  itemSize: undefined,
  scrollHeight: undefined,
  unsafePt: undefined
})
</script>
