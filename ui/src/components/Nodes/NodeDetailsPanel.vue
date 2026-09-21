<template>
  <div class="card">
    <div class="title-row">
      <div class="title headline3">{{ title }}</div>
      <div v-if="$slots.actions" class="action-buttons-container">
        <slot name="actions" />
      </div>
    </div>
    <slot />
  </div>
</template>

<script setup lang="ts">
// The card shell shared by the Node Details panels: a title, an optional row of right-aligned
// action buttons beside it, and the panel body. Scoped to components/Nodes for now; promoting
// icon-button actions into OnmsCard itself is a separate piece of work.
defineProps<{
  title: string
}>()
</script>

<style lang="scss" scoped>
.card {
  background: var(--p-content-background);
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.08);
  padding: 15px;
  margin-bottom: 15px;

  .title-row {
    display: flex;
    align-items: center;
    gap: 5px;
  }

  .action-buttons-container {
    display: flex;
    flex: 1;
    align-items: center;
    justify-content: flex-end;
    gap: 0.5rem;
  }
}
</style>
