<template>
  <h3 class="link headline3">
    <router-link :to="to">
      <slot></slot>
    </router-link>
  </h3>
</template>

<script setup lang="ts">
defineProps({
  to: {
    required: true,
    type: String
  }
})
</script>

<style lang="scss" scoped>
.link {
  width: 100%;
  a, a:link, a:visited {
    color: var(--feather-primary-text-on-color);
    margin-left: 50px;
  }
}
</style>
