<template>
  <div class="login-container">
    <div class="login-header">
      <div class="logo">
        <span class="logo-text">OA</span>
      </div>
      <h1 class="title">OA办公系统</h1>
    </div>

    <LoginForm />

    <div class="login-footer">
      <p>© 2026 Company Name. All rights reserved.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import LoginForm from '../components/LoginForm.vue'
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .logo {
    display: inline-flex;
  align-items: center;
    justify-content: center;
    width: 80px;
    height: 80px;
    margin: 0 auto;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 16px;
    font-size: 32px;
    font-weight: bold;
    color: #ffffff;

    .logo-text {
      font-size: 28px;
      font-weight: 700;
    }
  }

  .title {
    font-size: 28px;
    color: #ffffff;
    margin-top: 16px;
    font-weight: 500;
  }
}

.login-footer {
  margin-top: 40px;
  text-align: center;

  p {
    color: rgba(255, 255, 255, 0.8);
    font-size: 14px;
    margin: 0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .login-header {
    .logo {
      width: 80px;
      height: 40px;
    }

    .title {
      font-size: 20px;
    }
  }
}
</style>
