<template>
  <div
    v-if="notFoundMessage"
    class="not-found-container"
    data-test="threshold-edit-not-found"
  >
    <p>{{ notFoundMessage }}</p>
    <OnmsButton
      label="Go Back"
      data-test="threshold-edit-not-found-back"
      @click="emit('back')"
    />
  </div>
  <div
    v-else
    class="main-content"
  >
    <div class="title">
      <div class="header">
        <div>
          <OnmsButton
            variant="text"
            data-test="threshold-edit-back"
            @click="emit('back')"
          >
            <OnmsIcon :icon="ArrowBack" />
            Go Back
          </OnmsButton>
        </div>
        <div>
          <h3 data-test="threshold-edit-title">{{ title }}</h3>
        </div>
      </div>
    </div>
    <div class="spacer"></div>
    <div class="spacer"></div>
    <div class="basic-info">
      <div
        class="section-content"
        :class="{ wide }"
      >
        <div class="section-title">
          <h3>{{ sectionTitle }}</h3>
          <slot name="section-actions" />
        </div>
        <slot />
        <div class="spacer"></div>
        <div class="action-container">
          <OnmsButton
            variant="outlined"
            label="Cancel"
            data-test="threshold-edit-cancel"
            @click="emit('cancel')"
          />
          <OnmsButton
            :label="saveLabel"
            :disabled="saveDisabled"
            data-test="threshold-edit-save"
            @click="emit('save')"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { OnmsButton, OnmsIcon } from '@opennms/onms-ui'
import ArrowBack from '@opennms/onms-ui/icons/navigation/ArrowBack.vue'

// Page shell shared by the threshold create/edit pages; mirrors the event configuration edit page.
defineProps<{
  title: string
  sectionTitle: string
  saveLabel: string
  saveDisabled?: boolean
  wide?: boolean
  notFoundMessage?: string
}>()

const emit = defineEmits<{
  back: []
  cancel: []
  save: []
}>()
</script>

<style scoped lang="scss">
@use '@/styles/onms-typography' as *;

.main-content {
  padding: 30px;
  margin: 30px;

  border-radius: 8px;
  background: var(--p-content-background);

  .title {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .header {
      display: flex;
      align-items: center;
      gap: 20px;
    }
  }

  .basic-info {
    border-width: 1px;
    border-style: solid;
    border-color: var(--p-content-border-color);
    padding: 10px;
    border-radius: 8px;

    .section-content {
      width: 50%;

      &.wide {
        width: 100%;
      }
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 0.75em;
    }
  }

  .spacer,
  :slotted(.spacer) {
    min-height: 0.5em;
  }

  .action-container {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}

.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 25px;

  p {
    @include onms-headline3;
    margin: 0;
  }
}
</style>
