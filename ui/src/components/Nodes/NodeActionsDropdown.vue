<template>
  <FeatherDropdown>
    <template v-slot:trigger="{ attrs, on }">
      <FeatherButton
        icon="Node Actions"
        v-bind="attrs"
        v-on="on"
      >
        <FeatherIcon :icon="menu" class="node-actions-icon" />
      </FeatherButton>
    </template>
    <FeatherDropdownItem @click="triggerNodeInfo(node)">
      <span class="node-menu-item">Info...</span>
    </FeatherDropdownItem>
    <FeatherDropdownItem
      v-for="linkItem in linkItems"
      :key="linkItem.name"
      @click="onNodeLink(linkItem.name, node)">
      <span class="node-menu-item">{{ linkItem.label }}</span>
    </FeatherDropdownItem>
  </FeatherDropdown>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import MoreVert from '@featherds/icon/navigation/MoreVert'
import { markRaw, PropType } from 'vue'
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

const menu = markRaw(MoreVert)

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
</script>

<style lang="scss" scoped>
.node-menu-item {
  padding: 1em;
}

button.btn.btn-icon .node-actions-icon {
  font-size: 1.1rem;
}
</style>
