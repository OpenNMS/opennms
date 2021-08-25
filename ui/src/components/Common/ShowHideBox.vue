<template>
  <Row><h4 class="pointer" @click="show = !show">{{ label }}</h4></Row>
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
    const show = ref(false);

    return {
      show
    }
  }
})

</script>

<style scoped lang="scss"></style>
