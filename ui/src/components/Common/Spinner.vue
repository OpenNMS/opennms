<template>
	<ProgressSpinner v-if="spinnerState" id="spinner" />
</template>

<script setup lang="ts">
	import { computed } from 'vue'
	import { useStore } from 'vuex'
	import ProgressSpinner from 'primevue/progressspinner'

	const store = useStore()
	const spinnerState = computed(() => store.state.spinnerModule.spinnerState)
</script>

<style lang="scss">
	#spinner {
		position: absolute;
		top: 40%;
		left: 50%;
	}
</style>
