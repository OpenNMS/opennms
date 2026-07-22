<template>
  <div class="wrapper">
    <div class="wrapper-inner">
      <div class="title">No external requisition configured.</div>
      <div class="paragraph">Add an external requisition to synchronize inventory.</div>
      <div class="flex button-wrapper">
        <PButton
          data-test="external-req-btn"
          class="button"
          @click="newDefinition"
        >Add External Requisition</PButton>
      </div>
    </div>
  </div>
</template>

<script
  lang="ts"
  setup
>
import { PropType } from 'vue'
import Button from 'primevue/button'

const PButton = Button

/**
 * Props
 */
defineProps({
  newDefinition: {
    type: Function as PropType<(payload: MouseEvent) => void>,
    required: true
  }
})
</script>

<style
  lang="scss"
  scoped
>
@import '@/styles/onms-typography';

.wrapper {
  background-color: var(--p-content-background);
}
.wrapper-inner {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 80px 0;
  text-align: center;
}
.title {
  @include onms-headline3();
  margin-bottom: 16px;
  color: var(--p-primary-color);
}
.paragraph {
  @include onms-body-small();
  max-width: 400px;
}
.button {
  margin-top: 32px;
}
.button-icon {
  font-size: 18px;
}
.flex {
  margin-top: 20px;
}
</style>
