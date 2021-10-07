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
@import "@featherds/styles/mixins/typography";
.link {
  @include headline3();
  width: 100%;
  a {
    color: var(--feather-primary-text-on-color);
    margin-left: 50px;
  }
}
</style>
