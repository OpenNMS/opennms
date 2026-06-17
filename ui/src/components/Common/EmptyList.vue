<!--
  To display a message if the list is empty, an optional title, and an action (optional)

  Component props structure:
    {
      title: 'This is a title',
      msg: 'This is an empty list.',
      btn: { // optional
        label
        action
    }
}
 -->
<template>
  <div :class="['empty-list', bg ? 'bg' : '']">
    <h3 v-if="content.title" data-test="title">{{ content.title }}</h3>
    <div data-test="msg">{{ msg }}</div>
    <PButton
      v-if="content.btn"
      outlined
      :label="content.btn.label"
      data-test="btn"
      @click="content.btn.action"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Button from 'primevue/button'

const PButton = Button

type Content = {
  title?: string
  msg: string
  btn?: {
    label: string
    action: () => void
  }
}

const props = defineProps<{
  content: Content,
  bg?: boolean
}>()

const msg = computed(() => props.content.msg || '')
</script>

<style lang="scss" scoped>
@use '@/styles/vars.scss';

.empty-list {
  display: flex;
  flex-direction: column;
  width: 100%;
  justify-content: center;
  align-items: center;
  height: 200px;

  >button {
    margin-top: 1.25rem;
  }
}

.bg {
  background-color: var(--p-content-background);
  border-radius: vars.$border-radius-surface;
  border: 1px solid var(--p-content-border-color);
}
</style>
