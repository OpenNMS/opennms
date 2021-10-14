<template>
  <div class="headline3">Do you have any managed or preconfigured information about your network?</div>

  <Content />

  <div class="feather-row">
    <div class="feather-col-11">
      <FeatherButton
        class="btn-next"
        primary
        @click="$emit('next-page', { pageIndex: 0 })"
        v-if="showAddStepNextButton"
      >This looks good</FeatherButton>
    </div>
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
.btn-next {
  float: right;
}
</style>
