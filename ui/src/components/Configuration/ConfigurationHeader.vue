<template>
  <div>
    <h1 class="title">{{ props.title }}</h1>
    <h2 class="headline">{{ props.headline }}</h2>
    <p class="body">
      The
      <strong>Provisiond</strong> subsystem uses
      <em>requisitions</em> to keep track of the nodes, interfaces, and services to be monitored.
      Creating requisition definitions enables recurring synchronization with outside inventory sources.
      Adjusting thread pool sizes can improve performance.
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
