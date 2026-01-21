<template>
  <el-config-provider :locale="zhCn">
    <!-- 登录和重置密码页面独立布局 -->
    <div v-if="isAuthPage" class="auth-page-layout">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <!-- 其他页面带顶部菜单的布局 -->
    <div v-else class="app-container">
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
          <el-menu-item index="/department">
            <el-icon><Office-building /></el-icon>
            <span>部门管理</span>
          </el-menu-item>
          <el-menu-item index="/asset">
            <el-icon><Box /></el-icon>
            <span>资产管理</span>
          </el-menu-item>
          <el-menu-item index="/leave">
            <el-icon><Calendar /></el-icon>
            <span>请假管理</span>
          </el-menu-item>
          <el-menu-item index="/expense">
            <el-icon><Wallet /></el-icon>
            <span>费用报销</span>
          </el-menu-item>
          <el-menu-item index="/meeting">
            <el-icon><VideoCamera /></el-icon>
            <span>会议室预定</span>
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
            <el-menu-item index="/menu">
              <el-icon><Menu /></el-icon>
              <span>菜单管理</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>

        <div class="header-right">
          <el-dropdown 
            v-model="showUserMenu" 
            trigger="click" 
            class="user-dropdown"
            @command="handleUserMenuCommand"
          >
            <div class="user-info">
              <el-avatar 
                :size="32" 
                :src="authStore.userInfo?.avatar" 
                :icon="authStore.userInfo?.avatar ? undefined : UserFilled"
              />
              <span class="user-name">{{ authStore.userInfo?.name || '用户' }}</span>
              <el-icon class="arrow-icon" :class="{ 'rotated': showUserMenu }"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  <span>个人信息</span>
                </el-dropdown-item>
                <el-dropdown-item command="changePassword">
                  <el-icon><Lock /></el-icon>
                  <span>修改密码</span>
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>

      <!-- 修改密码模态框 -->
      <el-dialog
        v-model="showChangePasswordDialog"
        title="修改密码"
        width="400px"
        :close-on-click-modal="true"
        :close-on-press-escape="true"
      >
        <el-form
          ref="changePasswordFormRef"
          :model="changePasswordForm"
          :rules="passwordRules"
          label-width="100px"
          class="change-password-form"
        >
          <el-form-item label="当前密码" prop="currentPassword">
            <el-input
              v-model="changePasswordForm.currentPassword"
              type="password"
              placeholder="请输入当前密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="changePasswordForm.newPassword"
              type="password"
              placeholder="请输入新密码（至少6位，包含字母和数字）"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="changePasswordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="closeChangePasswordDialog">取消</el-button>
            <el-button type="primary" @click="handleChangePassword" :loading="changePasswordLoading">
              确定
            </el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  User,
  OfficeBuilding,
  Box,
  Calendar,
  Wallet,
  Setting,
  UserFilled,
  Key,
  Notebook,
  Menu,
  VideoCamera,
  Avatar,
  SwitchButton,
  ArrowDown,
  Lock
} from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { useAuthStore } from '@/modules/auth/store'
import { ElMessage, ElMessageBox, ElForm, ElFormItem, ElInput, ElButton, ElDialog } from 'element-plus'
import * as authApi from '@/modules/auth/api'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const activeMenu = ref(route.path)
const showUserMenu = ref(false)

// 修改密码相关状态
const showChangePasswordDialog = ref(false)
const changePasswordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const changePasswordFormRef = ref<InstanceType<typeof ElForm> | null>(null)
const changePasswordLoading = ref(false)
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度至少为6位', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$/, message: '新密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== changePasswordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}



// 判断是否是认证相关页面(不需要顶部菜单)
const isAuthPage = computed(() => {
  return route.path === '/login' || route.path === '/reset-password'
})

watch(
  () => route.path,
  (newPath) => {
    // 设置当前激活的菜单
    if (newPath.startsWith('/employee')) {
      activeMenu.value = '/employee'
    } else if (newPath.startsWith('/department')) {
      activeMenu.value = '/department'
    } else if (newPath.startsWith('/asset')) {
      activeMenu.value = '/asset'
    } else if (newPath.startsWith('/leave')) {
      activeMenu.value = '/leave'
    } else if (newPath.startsWith('/expense')) {
      activeMenu.value = '/expense'
    } else if (newPath.startsWith('/meeting')) {
      activeMenu.value = '/meeting'
    } else if (newPath.startsWith('/permission')) {
      // 权限管理页面，设置菜单保持激活状态
      activeMenu.value = 'settings'
    } else if (newPath.startsWith('/dict')) {
      // 数据字典页面，设置菜单保持激活状态
      activeMenu.value = 'settings'
    } else if (newPath.startsWith('/menu')) {
      // 菜单管理页面，设置菜单保持激活状态
      activeMenu.value = 'settings'
    }
  }
)

async function handleUserMenuCommand(command: string) {
  if (command === 'profile') {
    ElMessage.info('个人信息功能开发中')
  } else if (command === 'changePassword') {
    showChangePasswordDialog.value = true
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm(
        '确定要退出登录吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      )
      
      await authStore.logout()
      ElMessage.success('已退出登录')
      // 重定向到登录页面
      router.push('/login')
    } catch (error) {
      // 取消操作，不做任何处理
    }
  }
}

// 修改密码
async function handleChangePassword() {
  if (!changePasswordFormRef.value) return
  
  await changePasswordFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        changePasswordLoading.value = true
        
        // 调用修改密码API
        const response = await authApi.changePassword({
          currentPassword: changePasswordForm.value.currentPassword,
          newPassword: changePasswordForm.value.newPassword
        })
        
        if (response.code === 200) {
          ElMessage.success('密码修改成功')
          showChangePasswordDialog.value = false
          // 重置表单
          changePasswordForm.value = {
            currentPassword: '',
            newPassword: '',
            confirmPassword: ''
          }
        } else {
          ElMessage.error(response.message || '密码修改失败')
        }
      } catch (error: any) {
        console.error('修改密码失败:', error)
        ElMessage.error(error.message || '密码修改失败')
      } finally {
        changePasswordLoading.value = false
      }
    }
  })
}

// 关闭修改密码对话框
function closeChangePasswordDialog() {
  showChangePasswordDialog.value = false
  // 重置表单
  changePasswordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  // 重置验证状态
  if (changePasswordFormRef.value) {
    changePasswordFormRef.value.resetFields()
  }
}

</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;
@use '@/assets/styles/mixins.scss' as *;

// 认证页面独立布局(全屏,无顶部菜单)
.auth-page-layout {
  width: 100%;
  height: 100vh;
  overflow: auto;
}

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

  .header-right {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    margin-left: $spacing-lg;

    .user-info {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      padding: $spacing-sm $spacing-md;
      border-radius: $border-radius-base;
      cursor: pointer;
      transition: all 0.3s;
      user-select: none;

      &:hover {
        background-color: $background-color;
      }

      .user-name {
        font-size: 14px;
        color: $text-primary;
        font-weight: $font-weight-medium;
        max-width: 120px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .arrow-icon {
        font-size: 12px;
        color: $text-secondary;
        transition: transform 0.3s;
      }
    }

    .user-dropdown {
      .el-dropdown-menu__item {
        display: flex;
        align-items: center;
        gap: $spacing-sm;

        .el-icon {
          font-size: 16px;
        }
      }
    }
  }

  .arrow-icon.rotated {
    transform: rotate(180deg);
  }
}

@media (max-width: 768px) {
  .app-header {
    padding: 0 $spacing-md;
  }

  .header-left {
    .app-title {
      font-size: 18px;
    }
  }

  .header-menu {
    display: none;
  }

  .header-right {
    margin-left: $spacing-sm;
  }

  .user-info {
    padding: $spacing-xs $spacing-sm;
  }

  .user-name {
    max-width: 80px;
    font-size: 13px;
  }

  .arrow-icon {
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .app-header {
    padding: 0 $spacing-sm;
  }

  .header-left {
    margin-right: $spacing-md;
  }

  .app-title {
    font-size: 16px;
  }

  .user-name {
    display: none;
  }
}


.app-main {
  flex: 1;
  padding: $page-padding;
  overflow-y: auto;
}
</style>
