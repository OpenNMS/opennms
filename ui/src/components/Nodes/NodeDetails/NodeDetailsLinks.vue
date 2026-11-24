<template>
  <div
    class="node-details-links"
    :class="$attrs.class"
    :style="$attrs.style as StyleValue"
  >
    <!--
      class and style go on this wrapper, which the page lays out; every other attribute (data-*,
      aria-*) goes on the nav. The measuring copy is outside the nav, so a query for the links, such
      as the smoke tests' "//nav[@data-test='node-details-links']//...", finds each menu only once.
      (A single root element: a comment above it would make the template a fragment in dev builds.)
    -->
    <nav
      v-bind="navAttrs($attrs)"
      aria-label="Node links"
      data-test="node-details-links"
    >
      <!-- Not shown until there is an answer for it, so it does not mount once more when one comes. -->
      <OnmsMenubar
        v-if="showBar"
        :items="items"
        :collapsed="collapsed"
      />
    </nav>
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
  </div>
</template>

<script setup lang="ts">
import { computed, PropType, ref, StyleValue, useTemplateRef, watch } from 'vue'
import { useDebounceFn, useElementSize } from '@vueuse/core'
import { OnmsMenubar, OnmsMenuItem } from '@opennms/onms-ui'
import useRole from '@/composables/useRole'
import { Node } from '@/types/index'
import AssetEditConfirmDialog from '@/components/Nodes/AssetEditConfirmDialog.vue'
import { createLinkGroups, INFO_ITEM, needsAssetEditConfirm } from '@/components/Nodes/nodeActionLinks'
import { buildServiceLinks, NodeLinkService } from '@/components/Nodes/nodeServiceLinks'

defineOptions({ inheritAttrs: false })

// Every attribute but class and style, for the nav (see the template).
const navAttrs = (attrs: Record<string, unknown>) => {
  const { class: _class, style: _style, ...rest } = attrs

  return rest
}

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
  // hamburger button. null: the page has not measured it yet, so the menus wait for it. Left out,
  // the menubar decides by the viewport width instead.
  availableWidth: {
    required: false,
    type: Number as PropType<number | null>,
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

/**
 * Whether the menus fit, decided once both widths are known and then held. OnmsMenubar mounts again
 * each time `collapsed` changes, so a guess made before the measurements (a width of 0 for the full
 * row fits anywhere) would mount it once for the guess and again for the answer. A width that goes
 * away (null, as while the page is hidden) keeps the last answer.
 */
const fits = ref<boolean>()

/**
 * How long a new answer must last before the bar changes to it. Each change mounts the bar again,
 * which also closes an open dropdown, so a window edge dragged back and forth across the point
 * where the menus fit changes the bar once it stops, not at every crossing. The first answer is
 * not delayed.
 */
const SETTLE_MS = 200

const settle = useDebounceFn((value: boolean) => {
  fits.value = value
}, SETTLE_MS)

const measuredFit = computed<boolean | undefined>(() =>
  (typeof props.availableWidth === 'number' && fullRowWidth.value > 0 ? fullRowWidth.value <= props.availableWidth : undefined))

watch(measuredFit, (value) => {
  if (value === undefined) {
    return
  }

  if (fits.value === undefined) {
    fits.value = value
  } else {
    // Called for a change back to the current answer too, so the latest call always wins.
    settle(value)
  }
}, { immediate: true })

// Left to the viewport only for a page that gives no width and never has.
const collapsed = computed<boolean | undefined>(() => (fits.value === undefined ? undefined : !fits.value))
const showBar = computed<boolean>(() => fits.value !== undefined || props.availableWidth === undefined)

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

  // PrimeVue hangs each menu's dropdown from the left edge of the menu. The row ends at the page's
  // right edge, so the last menu's dropdown would run past the window: hang it from the menu's
  // right edge instead. The others have room to their right.
  :deep(.p-menubar:not(.p-menubar-mobile) .p-menubar-root-list > .p-menubar-item:last-child) {
    position: relative;

    > .p-menubar-submenu {
      left: auto;
      right: 0;
    }
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
