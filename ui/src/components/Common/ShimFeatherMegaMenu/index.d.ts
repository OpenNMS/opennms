import { ShimFeatherMegaMenu } from './index.d'
import { DefineComponent } from 'vue'

// @featherds/megamenu index.d.ts does not follow same export convention as all other feather
// components. This is a temporary proxy/shim until feather is updated

interface ShimFeatherMegaMenuProps {
  name: {
    type: string,
    required: true
  },
  closeText: {
    type: string,
    required: true
  },
  role: {
    type: string,
    default: 'menu'
  }
}

export const ShimFeatherMegaMenu: DefineComponent<
 ShimFeatherMegaMenuProps
>
 