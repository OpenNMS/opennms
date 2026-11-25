<!--
  An availability figure as a pill. With a level it takes that level's colour; without one it is
  plain text, for a status label or a figure the thresholds cannot class.
-->
<template>
  <span
    class="availability-badge"
    :class="level ? `availability-badge--${level}` : undefined"
    :data-test="level ? 'availability-badge' : undefined"
    :data-level="level"
  >{{ text }}</span>
</template>

<script setup lang="ts">
import { AvailabilityClass } from '@/types/availability'

defineProps<{
  text: string
  level?: AvailabilityClass
}>()
</script>

<style lang="scss" scoped>
.availability-badge {
  font-variant-numeric: tabular-nums;

  &--normal,
  &--warning,
  &--critical {
    display: inline-block;
    padding: 0.0625rem 0.5rem;
    border-radius: 0.75rem;
    font-weight: 600;
  }

  &--normal {
    background: var(--onms-availability-normal-bg);
    color: var(--onms-availability-normal-fg);
  }

  &--warning {
    background: var(--onms-severity-warning-bg);
    color: var(--onms-severity-warning-fg);
  }

  &--critical {
    background: var(--onms-severity-critical-bg);
    color: var(--onms-severity-critical-fg);
  }
}
</style>
