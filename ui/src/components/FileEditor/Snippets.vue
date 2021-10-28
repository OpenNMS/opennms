<template>
  <div v-html="snippets"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useStore } from 'vuex'

const store = useStore()
const snippets = computed(() => store.state.fileEditorModule.snippets)
</script>
