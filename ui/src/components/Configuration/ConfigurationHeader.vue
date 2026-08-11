<template>
  <div>
    <h1 class="title">{{ props.title }}</h1>
    <h2 class="headline">{{ props.headline }}</h2>
    <p class="body">
      An external requisition imports the nodes, interfaces, and services from outside inventory sources.

      Thread pool sizes impact the performance of the provisioning subsystem.
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
@import '@/styles/onms-typography';

.title {
  @include onms-overline();
  color: #4b5ad6;
  margin-top: 28px;
  margin-bottom: 8px;
}

.headline {
  @include onms-headline1();
}

.body {
  @include onms-body-small();
  margin-top: 20px;
  max-width: 760px;
  margin-bottom: 32px;
  pre {
    display: inline;
  }
}
</style>
