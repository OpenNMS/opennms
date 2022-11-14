<template>
  <div id="card">
    <router-view />
  </div>
</template>

<script
  setup
  lang="ts"
></script>

<style
  scoped
  lang="scss"
>
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/themes/variables";

#card {
  @include elevation(2);
  background: var($surface);
  padding: 15px;
  height: calc(100vh - 110px);
  overflow-y: scroll;
}
</style>

