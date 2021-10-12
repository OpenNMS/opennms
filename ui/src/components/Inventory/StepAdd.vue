<template>
  <div class="card">
    <div class="title">Do you have any managed or preconfigured information about your network?</div>

    <Content />

    <FeatherButton
      primary
      @click="$emit('next-page', { pageIndex: 0 })"
      v-if="showAddStepNextButton"
    >This looks good</FeatherButton>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue'
import { FeatherButton } from '@featherds/button'
import Content from './StepAddContent.vue'
import { useStore } from 'vuex'

export default defineComponent({
  components: {
    FeatherButton,
    Content
  },
  emits: ['next-page'],
  setup() {
    const store = useStore()
    const showAddStepNextButton = computed(() => store.state.inventoryModule.showAddStepNextButton)

    return {
      showAddStepNextButton
    }
  }
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  padding: 15px;
  margin-bottom: 15px;
  .title {
    @include headline3();
  }
}
</style>
