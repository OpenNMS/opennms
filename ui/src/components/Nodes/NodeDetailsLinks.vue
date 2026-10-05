<template>
  <nav
    class="node-details-links"
    aria-label="Node links"
    data-test="node-details-links"
  >
    <OnmsMenubar :items="items" />
  </nav>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue'
import { OnmsMenubar, OnmsMenuItem } from '@opennms/onms-ui'
import useRole from '@/composables/useRole'
import { Node } from '@/types'
import { createLinkGroups, INFO_ITEM } from './nodeActionLinks'

// The node's links, grouped under top-level menus in a row beneath the header. The same links as
// the title row's actions menu (NodeActionsDropdown), from the same nodeActionLinks, so the two
// cannot disagree; that menu is to be retired once this has settled.
const props = defineProps({
  baseHref: {
    required: true,
    type: String
  },
  node: {
    required: true,
    type: Object as PropType<Node>
  },
  // As for NodeActionsDropdown: data the node payload does not carry, which the page fetches.
  snmpPrimaryIpAddress: {
    required: false,
    type: String,
    default: undefined
  },
  existsInRequisition: {
    required: false,
    type: Boolean,
    default: false
  },
  triggerNodeInfo: {
    required: false,
    type: Function as PropType<() => void>,
    default: undefined
  }
})

const { adminRole, provisionRole } = useRole()

const items = computed<OnmsMenuItem[]>(() =>
  createLinkGroups(props.node, {
    snmpPrimaryIpAddress: props.snmpPrimaryIpAddress,
    isAdmin: adminRole.value,
    canEditRequisitions: provisionRole.value,
    existsInRequisition: props.existsInRequisition
  })
    .map(group => ({
      label: group.label,
      items: group.items
        // Info needs a handler to open the dialog; without one there is nothing to offer.
        .filter(item => item.name !== INFO_ITEM || props.triggerNodeInfo)
        .map(item => ({
          label: item.label,
          command: item.name === INFO_ITEM
            ? () => props.triggerNodeInfo?.()
            : () => window.location.assign(`${props.baseHref}${item.link}`)
        }))
    }))
    .filter(group => group.items.length > 0))

defineExpose({ items })
</script>

<style lang="scss" scoped>
// A plain row of menus under the header chips, not PrimeVue's bordered, filled strip.
.node-details-links {
  margin-bottom: 1rem;

  :deep(.p-menubar) {
    background: transparent;
    border: none;
    padding: 0;
  }
}
</style>
