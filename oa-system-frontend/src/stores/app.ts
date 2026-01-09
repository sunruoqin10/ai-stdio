import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const isSidebarCollapsed = ref(false)

  // 加载状态
  const isLoading = ref(false)

  // 当前激活的菜单
  const activeMenu = ref('/employee')

  // 面包屑
  const breadcrumb = ref<Array<{ name: string; path: string }>>([])

  // 切换侧边栏
  const toggleSidebar = () => {
    isSidebarCollapsed.value = !isSidebarCollapsed.value
  }

  // 设置加载状态
  const setLoading = (loading: boolean) => {
    isLoading.value = loading
  }

  // 设置当前菜单
  const setActiveMenu = (menu: string) => {
    activeMenu.value = menu
  }

  // 设置面包屑
  const setBreadcrumb = (items: Array<{ name: string; path: string }>) => {
    breadcrumb.value = items
  }

  return {
    isSidebarCollapsed,
    isLoading,
    activeMenu,
    breadcrumb,
    toggleSidebar,
    setLoading,
    setActiveMenu,
    setBreadcrumb,
  }
})
