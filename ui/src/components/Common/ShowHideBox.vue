<template>
  <Row><div class="pointer headline4" @click="show = !show">{{ label }}</div></Row>
  <div v-if="show">
    <slot></slot>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import Row from './Row.vue'

export default defineComponent({
  components: {
    Row
  },
  props: {
    label: {
      type: String
    }
  },
  setup() {
    const show = ref(false)

    return {
      show
    }
  }
})
</script>
