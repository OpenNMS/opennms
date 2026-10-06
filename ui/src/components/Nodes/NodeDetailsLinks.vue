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
import { buildServiceLinks, NodeLinkService } from './nodeServiceLinks'

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
  // The node's remote-access and web services, for the Services menu.
  services: {
    required: false,
    type: Array as PropType<NodeLinkService[]>,
    default: () => []
  },
  triggerNodeInfo: {
    required: false,
    type: Function as PropType<() => void>,
    default: undefined
  }
})

const { adminRole, provisionRole } = useRole()

// Real links rather than commands, so they can be opened in a new tab or copied like any other
// link; the web pages open in a new tab of their own accord.
const servicesMenu = computed<OnmsMenuItem | undefined>(() => {
  const links = buildServiceLinks(props.services)

  return links.length > 0
    ? { label: 'Services', items: links.map(link => ({ label: link.label, url: link.url, ...(link.newTab ? { target: '_blank' } : {}) })) }
    : undefined
})

const groupMenus = computed<OnmsMenuItem[]>(() =>
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

// Services sits just before Admin, or last when there is no Admin menu.
const items = computed<OnmsMenuItem[]>(() => {
  const menus = [...groupMenus.value]

  if (servicesMenu.value) {
    const admin = menus.findIndex(menu => menu.label === 'Admin')
    menus.splice(admin === -1 ? menus.length : admin, 0, servicesMenu.value)
  }

  return menus
})

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
