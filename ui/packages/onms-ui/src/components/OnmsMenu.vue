<template>
  <Menu
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
  </Menu>
</template>

<script setup lang="ts">
import Menu from 'primevue/menu'
import { ref } from 'vue'
import { OnmsMenuItem } from '../types'

// Seam wrapper (NMS-20081) around PrimeVue Menu. Popup by default (every
// OpenNMS usage is a dropdown; pass :popup="false" for an inline menu).
// Open via the exposed toggle(event) from a trigger's @click. The #item
// slot forwards the underlying slot props ({ item, props }) — `props.action`
// is a render-binding object and is accepted seam leakage, documented in
// the README. `items as never` on :model: PrimeVue's own MenuItem.label
// accepts a render function in addition to string, which our narrower
// OnmsMenuItem.label doesn't — that widens PrimeVue's command event type
// past what OnmsMenuItem promises, so TS rejects the plain assignment even
// though items is structurally what Menu expects at runtime.
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

const menuRef = ref<InstanceType<typeof Menu>>()

defineExpose({
  toggle: (event: Event) => menuRef.value?.toggle(event),
  hide: () => menuRef.value?.hide()
})
</script>
