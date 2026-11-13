<template>
  <nav
    class="node-details-links"
    aria-label="Node links"
    data-test="node-details-links"
  >
    <OnmsMenubar
      :items="items"
      :collapsed="collapsed"
    />
    <!--
      The full row, never collapsed and never seen, so its width can be measured even while the
      bar above is showing its hamburger button.
    -->
    <div
      ref="fullRow"
      class="node-details-links-measure"
      aria-hidden="true"
      inert
    >
      <OnmsMenubar
        :items="items"
        :collapsed="false"
      />
    </div>
    <AssetEditConfirmDialog
      :visible="!!pendingAssetHref"
      :foreignSource="node.foreignSource"
      @ok="onAssetEditConfirmed"
      @cancel="pendingAssetHref = undefined"
    />
  </nav>
</template>

<script setup lang="ts">
import { computed, PropType, ref, useTemplateRef } from 'vue'
import { useElementSize } from '@vueuse/core'
import { OnmsMenubar, OnmsMenuItem } from '@opennms/onms-ui'
import useRole from '@/composables/useRole'
import { Node } from '@/types'
import AssetEditConfirmDialog from './AssetEditConfirmDialog.vue'
import { createLinkGroups, INFO_ITEM, needsAssetEditConfirm } from './nodeActionLinks'
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
  },
  // The room, in pixels, the page has for the menus. Narrower than they need, they collapse to the
  // hamburger button. Left out, the menubar decides by the viewport width instead.
  availableWidth: {
    required: false,
    type: Number,
    default: undefined
  }
})

const { adminRole, provisionRole, readOnlyRole } = useRole()

// The asset editor waits for the confirmation dialog when the node is from a requisition.
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
            : () => navigate(item.name, `${props.baseHref}${item.link}`)
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

const { width: fullRowWidth } = useElementSize(useTemplateRef<HTMLElement>('fullRow'), undefined, { box: 'border-box' })

const collapsed = computed<boolean | undefined>(() =>
  props.availableWidth === undefined ? undefined : fullRowWidth.value > props.availableWidth)

defineExpose({ items })
</script>

<style lang="scss" scoped>
// A plain row of menus beside the header chips, not PrimeVue's bordered, filled strip.
.node-details-links {
  // The measuring copy hangs from the right edge, inside the page's scroll area, so it can never
  // widen what the page scrolls.
  position: relative;

  :deep(.p-menubar) {
    background: transparent;
    border: none;
    padding: 0;
  }

  // Collapsed to the hamburger, the menubar is only as wide as its button, and PrimeVue sizes the
  // dropdown to the menubar and hangs it from the left edge. The page puts the button at the right,
  // so hang the dropdown from the right edge instead and give it a width of its own.
  :deep(.p-menubar-mobile .p-menubar-root-list) {
    left: auto;
    right: 0;
    width: max-content;
    min-width: 12.5rem;
  }
}

.node-details-links-measure {
  position: absolute;
  top: 0;
  right: 0;
  width: max-content;
  height: 0;
  overflow: hidden;
  visibility: hidden;
  pointer-events: none;
}
</style>
