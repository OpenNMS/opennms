<template>
  <div style="display: none">
    <slot v-if="ready"></slot>
  </div>
</template>

<script>
import 'leaflet.markercluster/dist/MarkerCluster.css'
import { propsBinder, remapEvents } from '@vue-leaflet/vue-leaflet/src/utils'
import {
  render,
  setup as layerSetup,
} from '@vue-leaflet/vue-leaflet/src/functions/layer'

const props = {
  options: {
    type: Object,
    default() {
      return {}
    }
  },
  onClusterClick: {
    type: Function
  }
}

export default {
  name: 'MarkerCluster',

  props,

  setup(props, context) {
    const leafletRef = ref({})
    const ready = ref(false)

    const addLayerToMainMap = inject('addLayer')
    const removeLayerFromMainMap = inject('removeLayer')

    provide('canSetParentHtml', () => !!leafletRef.value.getElement())
    provide(
      'setParentHtml',
      (html) => (leafletRef.value.getElement().innerHTML = html)
    )
    provide('addLayer', (layer) => {
      leafletRef.value.addLayer(layer.leafletObject)
    })
    provide('removeLayer', (layer) => {
      leafletRef.value.removeLayer(layer.leafletObject)
    })

    const { methods } = layerSetup(props, leafletRef, context)

    onMounted(async () => {
      const {
        bind,
        Browser,
        DivIcon,
        DomEvent,
        DomUtil,
        extend,
        FeatureGroup,
        featureGroup,
        Icon,
        icon,
        LatLng,
        LatLngBounds,
        LayerGroup,
        Map,
        Marker,
        marker,
        Path,
        Point,
        Polygon,
        Polyline,
        popup,
        Util,
      } = await import('leaflet/dist/leaflet-src.esm')

      /** create a fake window.L from just the bits we need to make markercluster load properly **/
      const L = {
        bind,
        Browser,
        DomEvent,
        DivIcon,
        DomUtil,
        extend,
        FeatureGroup,
        featureGroup,
        Icon,
        icon,
        LatLng,
        LatLngBounds,
        LayerGroup,
        Map,
        Marker,
        marker,
        Path,
        Point,
        Polygon,
        Polyline,
        popup,
        Util,
      }
      window['L'] = L

      const { MarkerClusterGroup } = await import(
        'leaflet.markercluster/dist/leaflet.markercluster-src.js'
      )
      leafletRef.value = new MarkerClusterGroup(props.options)

      const listeners = remapEvents(context.attrs)
      DomEvent.on(leafletRef.value, listeners)
      if (props.onClusterClick) {
        leafletRef.value.on('clusterclick', props.onClusterClick)
      }

      propsBinder(methods, leafletRef.value, props)

      addLayerToMainMap({
        ...props,
        ...methods,
        leafletObject: leafletRef.value,
      })

      ready.value = true
      nextTick(() => context.emit('ready', leafletRef.value))
    })

    onBeforeUnmount(
      () =>
        leafletRef.value &&
        leafletRef.value._leaflet_id &&
        removeLayerFromMainMap({ leafletObject: leafletRef.value })
    )

    return { ready, leafletObject: leafletRef }
  },
  render() {
    return render(this.ready, this.$slots)
  },
}
</script>
