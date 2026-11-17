<template>
  <a
    class="resource-tile"
    :href="link.href"
    :target="external ? '_blank' : undefined"
    :rel="external ? 'noopener noreferrer' : undefined"
    data-test="help-link"
  >
    <span class="tile-icon">
      <OnmsIcon :icon="link.icon" />
    </span>
    <span class="tile-body">
      <span class="tile-title">
        <span class="tile-label">{{ link.label }}</span>
        <OnmsIcon v-if="external" :icon="ExitToApp" class="tile-external" aria-label="Opens in a new tab" />
      </span>
      <span class="tile-description">{{ link.description }}</span>
    </span>
  </a>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { OnmsIcon } from '@opennms/onms-ui'
import ExitToApp from '@opennms/onms-ui/icons/action/ExitToApp.vue'
import { HelpLink } from '@/types/help'

const props = defineProps<{
  link: HelpLink
}>()

const external = computed(() => /^https?:\/\//.test(props.link.href))
</script>

<style scoped lang="scss">
.resource-tile {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  height: 100%;
  padding: 1.25rem;
  border: 1px solid var(--p-content-border-color);
  border-radius: 8px;
  background: var(--p-content-background);
  color: var(--p-text-color);
  text-decoration: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, transform 0.15s ease;

  &:hover,
  &:focus-visible {
    border-color: var(--p-primary-color);
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
    transform: translateY(-1px);

    .tile-label {
      color: var(--p-primary-color);
    }
  }

  &:focus-visible {
    outline: 2px solid var(--p-primary-color);
    outline-offset: 2px;
  }
}
.tile-icon {
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2.75rem;
  height: 2.75rem;
  border-radius: 8px;
  font-size: 1.5rem;
  color: var(--p-primary-color);
  background: color-mix(in srgb, var(--p-primary-color) 12%, transparent);
}
.tile-body {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  min-width: 0;
}
.tile-title {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-weight: 600;
}
.tile-external {
  font-size: 0.85rem;
  color: var(--p-text-muted-color);
}
.tile-description {
  color: var(--p-text-muted-color);
  font-size: 0.9rem;
  line-height: 1.4;
}
</style>
