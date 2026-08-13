<template>
  <OnmsIconButton
    :icon="InfoIcon"
    iconSize="1.25rem"
    class="about-dialog-button"
    :title="`About ${title}`"
    :aria-label="`About ${title}`"
    data-test="about-button"
    @click="visible = true"
  />
  <OnmsDialog
    :visible="visible"
    modal
    :header="`About ${title}`"
    width="min(760px, 95vw)"
    data-test="about-dialog"
    @update:visible="(value: boolean) => (visible = value)"
  >
    <div class="about-content">
      <slot />
    </div>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { OnmsDialog, OnmsIconButton } from '@opennms/onms-ui'
import InfoIcon from '@/components/icons/action/Info.vue'

// An Info affordance for an admin page: the icon lives in the card header and
// opens the page's help copy in a modal, replacing the older inline panel.
defineProps<{ title: string }>()

const visible = ref(false)
</script>

<style scoped lang="scss">
.about-content {
  display: flex;
  gap: 2.5rem;
  flex-wrap: wrap;

  :deep(.help-section) {
    flex: 1;
    min-width: 320px;
  }

  :deep(.section-title) {
    font-size: 1rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
  }

  :deep(p) {
    margin: 0 0 0.75rem 0;
    font-size: 0.9rem;
    line-height: 1.5;
  }
}
</style>
