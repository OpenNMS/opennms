<template>
  <OnmsIconButton
    title="Node Actions"
    aria-label="Node Actions"
    aria-haspopup="true"
    :aria-controls="menuId"
    data-test="node-actions-button"
    :icon="menuIcon"
    @click="toggle"
  />
  <OnmsMenu
    :id="menuId"
    ref="menu"
    :items="items"
  />
</template>

<script setup lang="ts">
import MoreVert from '@opennms/onms-ui/icons/navigation/MoreVert.vue'
import { OnmsIconButton, OnmsMenu, OnmsMenuItem } from '@opennms/onms-ui'
import { markRaw, computed, ref, PropType } from 'vue'
import { createLinkItemsList } from './nodeActionLinks'
import { Node } from '@/types'

const props = defineProps({
  baseHref: {
    required: true,
    type: String
  },
  node: {
    required: true,
    type: Object as PropType<Node>
  },
  // Supplied by the call site from its interface data; without it the Update SNMP action is
  // omitted rather than pointed at an address that is not the node's.
  snmpPrimaryIpAddress: {
    required: false,
    type: String,
    default: undefined
  },
  triggerNodeInfo: {
    required: false,
    type: Function as PropType<(node: Node) => void>,
    default: undefined
  }
})

const menuIcon = markRaw(MoreVert)
const menu = ref()
const menuId = computed(() => `node-actions-menu-${props.node.id}`)

// Info... opens a dialog describing the node, which is redundant on a page already showing it:
// a call site that omits the handler gets the navigation links alone.
const items = computed<OnmsMenuItem[]>(() => {
  const infoItem = props.triggerNodeInfo
    ? [{ label: 'Info...', command: () => props.triggerNodeInfo?.(props.node) }]
    : []

  // createLinkItemsList drops any link the node cannot supply the data for, such as Site
  // Status for a node with no building.
  return [
    ...infoItem,
    ...createLinkItemsList(props.node, { snmpPrimaryIpAddress: props.snmpPrimaryIpAddress }).map(li => ({
      label: li.label,
      command: () => window.location.assign(`${props.baseHref}${li.link}`)
    }))
  ]
})

const toggle = (event: Event) => {
  menu.value?.toggle(event)
}

defineExpose({ items })
</script>
