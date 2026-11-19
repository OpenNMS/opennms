<!--
  A link whose whole face is a tag, for the banners at the top of the Monitoring tab (the status
  banner and the notifications box). The default slot is the tag's face: a count and an icon.

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
    </OnmsTag>
  </component>
</template>

<script setup lang="ts">
import { RouteLocationRaw, RouterLink } from 'vue-router'
import { OnmsTag, OnmsTagSeverity } from '@opennms/onms-ui'

// `to` for a page of this app, `href` for a page outside it (the legacy alarm list).
withDefaults(defineProps<{
  label: string
  to?: RouteLocationRaw
  href?: string
  severity?: OnmsTagSeverity
}>(), {
  to: undefined,
  href: undefined,
  severity: 'secondary'
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

  // A slotted icon. Only the tag's direct children: an icon inside a slotted component, such as
  // SeverityIcon's, keeps that component's size.
  > :deep(.onms-icon) {
    font-size: 1.5rem;
  }
}
</style>
