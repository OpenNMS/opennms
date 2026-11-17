<!--
  A link whose whole face is a tag, for the banners at the top of the Monitoring tab (the status
  banner and the notifications box). The default slot is what leads the tag: a count, or an icon
  for whose items these are. `state` adds an icon for outstanding (red) or acknowledged (green).

  The tag carries no words, so `label` is its tooltip and its accessible name.
-->
<template>
  <component
    :is="to ? RouterLink : 'a'"
    v-onms-tooltip.top="label"
    :to="to"
    :href="to ? undefined : href"
    :aria-label="label"
    class="node-banner-tag-link"
  >
    <OnmsTag
      :severity="severity"
      class="node-banner-tag"
    >
      <slot />
      <OnmsIcon
        v-if="state"
        :icon="STATE_ICONS[state]"
        :class="`node-banner-tag-icon--${state}`"
      />
    </OnmsTag>
  </component>
</template>

<script setup lang="ts">
import { RouteLocationRaw, RouterLink } from 'vue-router'
import { OnmsIcon, OnmsTag, OnmsTagSeverity } from '@opennms/onms-ui'
import Cancel from '@opennms/onms-ui/icons/action/Cancel.vue'
import CheckCircle from '@opennms/onms-ui/icons/action/CheckCircle.vue'

export type NodeBannerTagState = 'outstanding' | 'acknowledged'

const STATE_ICONS: Record<NodeBannerTagState, object> = {
  outstanding: Cancel,
  acknowledged: CheckCircle
}

// `to` for a page of this app, `href` for a page outside it (the legacy alarm list).
withDefaults(defineProps<{
  label: string
  to?: RouteLocationRaw
  href?: string
  severity?: OnmsTagSeverity
  state?: NodeBannerTagState
}>(), {
  to: undefined,
  href: undefined,
  severity: 'secondary',
  state: undefined
})
</script>

<style lang="scss" scoped>
// The banner underlines its links; here the tag is the link's whole face, so no underline. The
// selector carries the banner's class to outrank its :deep(a) rule.
.node-details-banner .node-banner-tag-link {
  text-decoration: none;
  border-radius: 6px;

  &:focus-visible {
    outline: 2px solid var(--p-focus-ring-color, currentColor);
    outline-offset: 2px;
  }

  &:hover .node-banner-tag {
    filter: brightness(1.1);
  }
}

.node-banner-tag {
  gap: 0.3rem;

  // Every icon in the tag, the slotted one too.
  :deep(.onms-icon) {
    font-size: 1.5rem;
  }
}

.node-banner-tag-icon--outstanding {
  color: var(--onms-error-icon);
}

.node-banner-tag-icon--acknowledged {
  color: var(--onms-success);
}
</style>
