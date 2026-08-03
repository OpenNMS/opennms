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
@import '@/styles/onms-elevation';

#card {
  @include onms-elevation(2);
  background: var(--p-content-background);
  padding: 15px;
  height: calc(100vh - 110px);
  overflow-y: scroll;
}
</style>
