<template>
  <div class="wrapper">
    <div class="wrapper-inner">
      <div class="title">No external requisition configured.</div>
      <div class="paragraph">Add an external requisition to synchronize inventory.</div>
      <div class="flex button-wrapper">
        <FeatherButton
          data-test="external-req-btn"
          class="button"
          primary
          @click="newDefinition"
          >Add External Requisition</FeatherButton
        >
      </div>
    </div>
  </div>
</template>

<script
  lang="ts"
  setup
>
import { PropType } from 'vue'
import { FeatherButton } from '@featherds/button'

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
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.wrapper {
  background-color: var($background);
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
  @include headline3();
  margin-bottom: 16px;
  color: var($primary);
}
.paragraph {
  @include body-small();
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

