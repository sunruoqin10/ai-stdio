<template>
  <el-config-provider :locale="zhCn">
    <div class="app-container">
      <!-- 顶部导航栏 -->
      <el-header class="app-header">
        <div class="header-left">
          <h1 class="app-title">OA系统</h1>
        </div>
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :ellipsis="false"
          class="header-menu"
          router
        >
          <el-menu-item index="/employee">
            <el-icon><User /></el-icon>
            <span>员工管理</span>
          </el-menu-item>
          <el-menu-item index="/department/org">
            <el-icon><Office-building /></el-icon>
            <span>部门架构</span>
          </el-menu-item>
          <el-menu-item index="/asset">
            <el-icon><Box /></el-icon>
            <span>资产管理</span>
          </el-menu-item>

          <!-- 设置菜单 -->
          <el-sub-menu index="settings">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>设置</span>
            </template>
            <el-menu-item index="/permission/role">
              <el-icon><User-filled /></el-icon>
              <span>角色管理</span>
            </el-menu-item>
            <el-menu-item index="/permission/permission">
              <el-icon><Key /></el-icon>
              <span>权限管理</span>
            </el-menu-item>
            <el-menu-item index="/dict">
              <el-icon><Notebook /></el-icon>
              <span>数据字典</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  User,
  OfficeBuilding,
  Box,
  Setting,
  UserFilled,
  Key,
  Notebook
} from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const route = useRoute()
const activeMenu = ref(route.path)

watch(
  () => route.path,
  (newPath) => {
    // 设置当前激活的菜单
    if (newPath.startsWith('/employee')) {
      activeMenu.value = '/employee'
    } else if (newPath.startsWith('/department')) {
      activeMenu.value = '/department/org'
    } else if (newPath.startsWith('/asset')) {
      activeMenu.value = '/asset'
    } else if (newPath.startsWith('/permission')) {
      // 权限管理页面，设置菜单保持激活状态
      activeMenu.value = 'settings'
    } else if (newPath.startsWith('/dict')) {
      // 数据字典页面，设置菜单保持激活状态
      activeMenu.value = 'settings'
    }
  }
)
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;
@use '@/assets/styles/mixins.scss' as *;

.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: $background-color;
}

.app-header {
  display: flex;
  align-items: center;
  background-color: $white;
  box-shadow: $shadow-sm;
  padding: 0 $page-padding;
  height: $layout-header-height;
  z-index: $z-index-fixed;

  .header-left {
    margin-right: $spacing-xxxl;

    .app-title {
      font-size: 20px;
      font-weight: $font-weight-bold;
      color: $primary-color;
      margin: 0;
    }
  }

  .header-menu {
    flex: 1;
    border-bottom: none;

    .el-menu-item {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
    }

    .el-sub-menu {
      .el-sub-menu__title {
        display: flex;
        align-items: center;
        gap: $spacing-sm;
      }

      .el-menu-item {
        display: flex;
        align-items: center;
        gap: $spacing-sm;
      }
    }
  }
}

.app-main {
  flex: 1;
  padding: $page-padding;
  overflow-y: auto;
}
</style>
