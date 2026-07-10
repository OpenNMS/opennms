<template>
  <Button
    text
    title="Node Actions"
    aria-label="Node Actions"
    aria-haspopup="true"
    :aria-controls="menuId"
    data-test="node-actions-button"
    @click="toggle"
  >
    <FeatherIcon
      :icon="menuIcon"
      class="node-actions-icon"
    />
  </Button>
  <Menu
    :id="menuId"
    ref="menu"
    :model="items"
    popup
  />
</template>

<script setup lang="ts">
import Button from 'primevue/button'
import Menu from 'primevue/menu'
import type { MenuItem } from 'primevue/menuitem'
import { FeatherIcon } from '@featherds/icon'
import MoreVert from '@featherds/icon/navigation/MoreVert'
import { markRaw, computed, ref, PropType } from 'vue'
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
  triggerNodeInfo: {
    required: true,
    type: Function as PropType<(node: Node) => void>
  }
})

const menuIcon = markRaw(MoreVert)
const menu = ref()
const menuId = computed(() => `node-actions-menu-${props.node.id}`)

const linkItems = [
  { name: 'events', label: 'Events' },
  { name: 'alarms', label: 'Alarms' },
  { name: 'view-outages', label: 'Outages' },
  { name: 'assets', label: 'Assets' },
  { name: 'metadata', label: 'Metadata' },
  { name: 'hardware', label: 'Hardware Inventory' },
  { name: 'availability', label: 'Availability' },
  { name: 'graphs', label: 'Resource Graphs' },
  { name: 'rescan', label: 'Node Rescan' },
  { name: 'admin', label: 'Admin / Node Management' },
  { name: 'updateSnmp', label: 'Update SNMP Information' },
  { name: 'schedule-outage', label: 'Schedule an Outage' },
  { name: 'topology', label: 'View Topology Map' }
]

const items = computed<MenuItem[]>(() => [
  { label: 'Info...', command: () => props.triggerNodeInfo(props.node) },
  ...linkItems.map(li => ({
    label: li.label,
    command: () => onNodeLink(li.name, props.node)
  }))
])

const toggle = (event: Event) => {
  menu.value?.toggle(event)
}

const onNodeLink = (name: string, node: Node) => {
  const link = mapLink(name, node)
  window.location.assign(`${props.baseHref}${link}`)
}

const mapLink = (name: string, node: Node) => {
  switch (name) {
    case 'events':
      return `event/list?filter=node%3D${node.id}`
    case 'alarms':
      return `alarm/list.htm?filter=node%3D${node.id}`
    case 'view-outages':
      return `outage/list.htm?filter=node%3D${node.id}`
    case 'assets':
      return `asset/modify.jsp?node=${node.id}`
    case 'metadata':
      return `element/node-metadata.jsp?node=${node.id}`
    case 'hardware':
      return `hardware/list.jsp?node=${node.id}`
    case 'availability':
      return `element/availability.jsp?node=${node.id}`
    case 'graphs':
      return `graph/chooseresource.jsp?node=${node.id}&reports=all`
    case 'rescan':
      return `element/rescan.jsp?node=${node.id}`
    case 'admin':
      return `admin/nodemanagement/index.jsp?node=${node.id}`
    case 'updateSnmp':
      // TODO: Get IP Address
      return `admin/updateSnmp.jsp?node=${node.id}&ipaddr=0.0.0.0`
    case 'schedule-outage':
      return `admin/sched-outages/editoutage.jsp?newName=${node.label}&addNew=true&nodeID=${node.id}`
    case 'topology':
      return `topology?provider=Enhanced+Linkd&szl=1&focus-vertices=${node.id}`
    default: return ''
  }
}

defineExpose({ items })
</script>

<style lang="scss" scoped>
.node-actions-icon {
  font-size: 1.1rem;
}
</style>
