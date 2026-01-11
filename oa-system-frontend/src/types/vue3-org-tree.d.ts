declare module 'vue3-org-tree' {
  import { DefineComponent } from 'vue'

  export interface OrgTreeNode {
    id: string | number
    label: string
    children?: OrgTreeNode[]
    expanded?: boolean
    employeeCount?: number
    [key: string]: any
  }

  export interface Vue3OrgTreeProps {
    data: OrgTreeNode
    horizontal?: boolean
    collapsable?: boolean
    renderContent?: (h: any, node: OrgTreeNode) => any
    'on-node-click'?: (node: OrgTreeNode, event: Event) => void
    'on-expand'?: (node: OrgTreeNode, event: Event) => void
  }

  const Vue3OrgTree: DefineComponent<Vue3OrgTreeProps>

  export default Vue3OrgTree
}
