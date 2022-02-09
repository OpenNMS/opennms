<template>
  <div class="white-bg" v-if="convertedItem?.url !== '://?=' && convertedItem?.url !== ''">
    <div class="title">URL:</div>
    <div class="url">{{ convertedItem?.url }}</div>
  </div>
</template>

<script lang="ts" setup>
import { computed, PropType } from 'vue'
import { LocalConfiguration } from './configuration.types'
import { ConfigurationHelper } from './ConfigurationHelper'

/**
 * Props
 */
const props = defineProps({
  item: {type:Object as PropType<LocalConfiguration>, required:true}
})

/**
 * Local State
 */
const convertedItem = computed(() => {
  const converted = props.item?.type ?
    ConfigurationHelper.convertLocalToServer(props.item) :
    { 'import-url-resource': '' }
  return {
    item: converted,
    url: converted['import-url-resource']
  }
})

</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
.white-bg {
  background-color: var(--feather-surface);
  display: flex;
  align-items: flex-start;
  padding: 16px 24px;
  @include elevation(2);
  margin-top:16px;
}
.title {
  @include headline4();
  color: var(--feather-primary);
}
.url {
  margin-top: 6px;
  margin-left: 3px;
  @include subtitle2();
  color: #a0a1a4;
}
</style>