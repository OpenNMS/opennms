<template>
  <div>
    <h1 class="title">{{ props.title }}</h1>
    <h2 class="headline">{{ props.headline }}</h2>
    <p class="body">
      The provision daemon (provisiond) uses requisitions to track the nodes, interfaces, and services to monitor.
      Create a requisition to enable regular synchronization with outside inventory sources. Adjust the thread pool size to improve performance.
    </p>
  </div>
</template>

<script lang="ts" setup>

/**
 * Props
 */

const props = defineProps({
  title: String,
  headline: String,
  body: String
})

</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";

.title {
  @include overline();
  color: #4b5ad6;
  margin-top: 28px;
  margin-bottom: 8px;
}

.headline {
  @include headline1();
}

.body {
  @include body-small();
  margin-top: 20px;
  max-width: 760px;
  margin-bottom: 32px;
  pre {
    display: inline;
  }
}
</style>
