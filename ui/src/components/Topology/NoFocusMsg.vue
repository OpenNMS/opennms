<template>
  <div class="no-focus-container">
    <div class="title">No Focus Defined</div>

    <div class="content">
      <p>This means</p>
      <ul>
        <li>the last vertex was removed from focus or</li>
        <li>no default focus is available.</li>
      </ul>

      <p>To add a vertex to focus</p>
      <ul>
        <li>manually add a vertex to focus via the search box</li>
        <li>use the default focus</li>
      </ul>

      <FeatherButton @click="useDefaultFocus" primary class="btn" :disabled="!defaultObjects">
        <span v-if="defaultObjects">Use Default Focus</span>
        <span v-else>No Nodes Available</span>
      </FeatherButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { PropType } from 'vue'
import { useStore } from 'vuex'

const store = useStore()
const defaultObjects = computed<Node>(() => store.state.topologyModule.defaultObjects)

defineProps({
  useDefaultFocus: {
    required: true,
    type: Function as PropType<(payload: MouseEvent) => void>
  }
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";

.no-focus-container {
  @include body-small;
  @include elevation(1);
  background: var($surface);
  color: var($primary-text-on-surface);
  height: 400px;
  width: 600px;
  position: absolute;
  margin-left: auto;
  margin-right: auto;
  left: 0;
  right: 0;
  z-index: 99999;
  top: 7rem;
  .title {
    @include headline3;
    height: auto;
    background: var($secondary);
    color: var($primary-text-on-color);
    padding: 15px;
  }

  .content {
    margin-left: 2rem;

    .btn {
      margin-left: 20px;
      margin-top: 20px;
    }
  }
}
</style>
