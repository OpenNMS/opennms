<template>
	<Fieldset>		
		{{props.content}}
	</Fieldset>
</template>
<script setup lang="ts">	
	import FieldSet from 'primevue/fieldset'
	const props = defineProps({
		 content: {
			type: String,
			required: false
		}
  })
</script>

<style lang="scss">
</style>