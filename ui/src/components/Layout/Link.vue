<template>
  <h3 class="link">
    <router-link :to="to">
      <slot></slot>
    </router-link>
  </h3>
</template>

<script setup lang="ts">
  import { toRefs } from "vue"

  const props = defineProps({
    to: {
      required: true,
      type: String
    }
  })

  const { to } = toRefs(props)
</script>

<style lang="scss" scoped>
$linkColor: #14d1df;
.link {
  width: 100%;
  a, a:link, a:visited {
    text-decoration: none;
    color: $linkColor;
    margin-left: 50px;
    font-family: "Segoe UI", Roboto, Helvetica;
  }
}
</style>
