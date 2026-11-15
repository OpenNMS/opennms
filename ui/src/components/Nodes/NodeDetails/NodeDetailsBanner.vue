<template>
  <div
    class="node-details-banner"
    :class="[`node-details-banner--${severity}`, { 'node-details-banner--no-strip': !strip }]"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { BannerSeverity } from './nodeStatus'

// A full-width strip for node-wide notices at the top of the details page -- the node's status,
// scheduled outages in effect -- tinted by severity, as the legacy page's severity rows were.
// `none` is the neutral, untinted look, for while there is nothing to say yet.
//
// `strip` is the thick, severity-coloured left edge. A box that only borrows the banner's look,
// with no severity of its own to signal, turns it off and keeps just the tint.

withDefaults(defineProps<{
  severity?: BannerSeverity
  strip?: boolean
}>(), {
  severity: 'none',
  strip: true
})
</script>

<style lang="scss" scoped>
@use '@/styles/onms-tokens' as variables;
@use '@/styles/onms-color-utils' as utils;

.node-details-banner {
  border: 1px solid var(--p-content-border-color);
  border-left-width: 6px;
  border-radius: 5px;
  padding: 0.75em 1em;
  margin-bottom: 15px;
  background: var(--p-content-background);

  // Slotted content, so :deep -- the links belong to the component using the banner.
  :deep(a) {
    color: inherit;
    text-decoration: underline;
  }
}

$severity-tokens: (
  'normal': variables.$success,
  'warning': variables.$warning,
  'minor': variables.$minor,
  'major': variables.$major,
  'critical': variables.$error
);

@each $name, $token in $severity-tokens {
  .node-details-banner--#{$name} {
    border-left-color: var(#{$token});
    background: utils.alpha($token, 0.2);
  }
}

// After the tints, so it wins over their border-left-color at the same specificity.
.node-details-banner--no-strip {
  border-left-width: 1px;
  border-left-color: var(--p-content-border-color);
}
</style>
