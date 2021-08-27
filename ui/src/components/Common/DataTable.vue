<template>
<p>Data Table Comp</p>

</template>

<script setup lang="ts">

import nodeData from './Demo/apiService';

const nodeDetails = nodeData.then((response) => {
       console.log("nodeData ::",response.data.response)
    })
    .catch((err) => {
        console.error(err);
    });

</script>