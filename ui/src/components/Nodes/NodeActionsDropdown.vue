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
  <AssetEditConfirmDialog
    :visible="!!pendingAssetHref"
    :foreignSource="node.foreignSource"
    @ok="onAssetEditConfirmed"
    @cancel="pendingAssetHref = undefined"
  />
</template>

<script setup lang="ts">
import MoreVert from '@opennms/onms-ui/icons/navigation/MoreVert.vue'
import { OnmsIconButton, OnmsMenu, OnmsMenuItem } from '@opennms/onms-ui'
import { markRaw, computed, ref, PropType } from 'vue'
import AssetEditConfirmDialog from './AssetEditConfirmDialog.vue'
import { createLinkItemsList, needsAssetEditConfirm } from './nodeActionLinks'
import useRole from '@/composables/useRole'
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
  // Supplied by the Node Details page, which asks once for its node; the node list cannot tell
  // without a request per row, so it omits this and the Edit in Requisition action with it.
  existsInRequisition: {
    required: false,
    type: Boolean,
    default: false
  },
  triggerNodeInfo: {
    required: false,
    type: Function as PropType<(node: Node) => void>,
    default: undefined
  }
})

const { adminRole, provisionRole, readOnlyRole } = useRole()

// The asset editor waits for the confirmation dialog when the node is from a requisition, as on
// the Node Details links row.
const pendingAssetHref = ref<string | undefined>(undefined)

const navigate = (name: string, href: string) => {
  if (name === 'assets' && needsAssetEditConfirm(props.node, readOnlyRole.value)) {
    pendingAssetHref.value = href

    return
  }

  window.location.assign(href)
}

const onAssetEditConfirmed = () => {
  const href = pendingAssetHref.value
  pendingAssetHref.value = undefined

  if (href) {
    window.location.assign(href)
  }
}

const menuIcon = markRaw(MoreVert)
const menu = ref()
const menuId = computed(() => `node-actions-menu-${props.node.id}`)

// Info... opens a dialog describing the node. Optional: a call site that omits the handler gets
// the navigation links alone. Both current call sites supply it -- the node list, and the node
// details page, which used to show the same attributes in a panel of its own.
const items = computed<OnmsMenuItem[]>(() => {
  const infoItem = props.triggerNodeInfo
    ? [{ label: 'Info...', command: () => props.triggerNodeInfo?.(props.node) }]
    : []

  // createLinkItemsList drops any link the node cannot supply the data for, such as Site
  // Status for a node with no building.
  return [
    ...infoItem,
    ...createLinkItemsList(props.node, {
      snmpPrimaryIpAddress: props.snmpPrimaryIpAddress,
      isAdmin: adminRole.value,
      canEditRequisitions: provisionRole.value,
      existsInRequisition: props.existsInRequisition
    }).map(li => ({
      label: li.label,
      command: () => navigate(li.name, `${props.baseHref}${li.link}`)
    }))
  ]
})

const toggle = (event: Event) => {
  menu.value?.toggle(event)
}

defineExpose({ items })
</script>
