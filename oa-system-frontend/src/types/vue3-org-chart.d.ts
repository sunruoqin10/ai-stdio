declare module 'vue3-org-chart' {
  import { DefineComponent } from 'vue'

  export interface OrgChartNode {
    id: string | number
    label: string
    children?: OrgChartNode[]
    employeeCount?: number
    level?: number
    [key: string]: any
  }

  export interface Vue3OrgChartProps {
    datas: OrgChartNode[]
    pan?: boolean
    zoom?: boolean
    'node-click'?: (node: OrgChartNode) => void
  }

  const Vue3OrgChart: DefineComponent<Vue3OrgChartProps>

  export default Vue3OrgChart
}
