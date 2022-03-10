<template>
  <div class="wrapper">
    <div class="wrapper-inner">
      <div class="title">No requisitions available.</div>
      <div class="paragraph">
        Add a requisition to synchronize external inventory.
      </div>
      <div class="flex button-wrapper">
        <FeatherButton class="button" primary @click="props.newDefinition">
          <FeatherIcon class="button-icon" :icon="Add" />New Definition
        </FeatherButton>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'

import Add from '@featherds/icon/action/Add'

/**
 * Props
 */
const props = defineProps({
  newDefinition: Function
})

</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
.wrapper {
  background-color: var(--feather-background);
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
  color: var(--feather-primary);
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