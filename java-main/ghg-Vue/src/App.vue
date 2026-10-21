<template>
  <!-- 灵气粒子背景 -->
  <StarField />

  <!-- 远山水墨 -->
  <div class="mountains"></div>
  <div class="mist"></div>

  <!-- 仙逆主视觉背景（登录 / 注册页共用，带动态效果） -->
  <ArtBackground v-if="!logged" />

  <!-- ============ 登录 / 注册 ============ -->
  <div v-if="!logged" class="wrap">
    <!-- 品牌区 -->
    <div class="brand">
      <div class="seal">仙逆</div>
      <div class="title">仙逆</div>
      <div class="subtitle">逆天改命 · 问道长生</div>
    </div>

    <!-- 登录 / 注册卡片（按模式切换） -->
    <LoginCard
      v-if="mode === 'login'"
      @go-register="mode = 'register'"
      @login-success="onLoginSuccess"
    />
    <RegisterCard v-else @go-login="mode = 'login'" />
  </div>

  <!-- ============ 首页 ============ -->
  <HomePage v-else :username="username" @logout="onLogout" />
</template>

<script setup>
import { ref } from 'vue'
import StarField from './components/StarField.vue'
import ArtBackground from './components/ArtBackground.vue'
import LoginCard from './components/LoginCard.vue'
import RegisterCard from './components/RegisterCard.vue'
import HomePage from './components/HomePage.vue'

const mode = ref('login')

/* 登录态：前端本地状态（后端 session 已由 /api/login 写入）
 * 后续如需接口鉴权，可在此处补 /api/me 校验 */
const logged = ref(false)
const username = ref('道友')

/* 开发态预览入口：?preview=home 跳过登录直达首页，?preview=register 直达注册页
 * 仅 import.meta.env.DEV 生效，生产构建不受影响 */
if (import.meta.env.DEV) {
  const preview = new URLSearchParams(location.search).get('preview')
  if (preview === 'home') {
    username.value = '预览道友'
    logged.value = true
  } else if (preview === 'register') {
    mode.value = 'register'
  }
}

/** 登录成功后进入首页 */
function onLoginSuccess(payload) {
  username.value = payload?.username || '道友'
  logged.value = true
  window.scrollTo({ top: 0 })
}

/** 退出登录：回到登录页 */
function onLogout() {
  logged.value = false
  mode.value = 'login'
}
</script>

<style scoped>
/* 远山水墨 */
.mountains {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 42vh;
  z-index: 1;
  background:
    radial-gradient(120% 80% at 20% 100%, rgba(58, 42, 107, 0.55), transparent 60%),
    radial-gradient(90% 70% at 80% 100%, rgba(20, 30, 70, 0.5), transparent 55%);
  -webkit-mask-image: linear-gradient(to top, #000 30%, transparent 100%);
  mask-image: linear-gradient(to top, #000 30%, transparent 100%);
}

.mist {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 30vh;
  height: 18vh;
  z-index: 1;
  background: linear-gradient(to top, var(--mist), transparent);
  filter: blur(8px);
  animation: drift 14s ease-in-out infinite alternate;
}

@keyframes drift {
  from {
    transform: translateX(-4%);
  }
  to {
    transform: translateX(4%);
  }
}

.wrap {
  position: relative;
  z-index: 3;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 26px;
  padding: 24px;
}

.brand {
  text-align: center;
  user-select: none;
}

.seal {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 104px;
  height: 104px;
  margin-bottom: 14px;
  border-radius: 18px;
  background: linear-gradient(160deg, rgba(232, 198, 107, 0.18), rgba(111, 233, 255, 0.08));
  border: 1px solid rgba(232, 198, 107, 0.45);
  box-shadow:
    0 0 32px rgba(232, 198, 107, 0.25),
    inset 0 0 24px rgba(111, 233, 255, 0.12);
  font-family: 'Ma Shan Zheng', serif;
  font-size: 42px;
  line-height: 1;
  color: var(--gold);
  letter-spacing: 2px;
  writing-mode: vertical-rl;
  padding: 10px 0;
  text-shadow: 0 0 18px rgba(232, 198, 107, 0.6);
}

.title {
  font-family: 'Ma Shan Zheng', serif;
  font-size: 64px;
  letter-spacing: 14px;
  color: #fff;
  text-shadow:
    0 0 26px rgba(111, 233, 255, 0.55),
    0 0 60px rgba(232, 198, 107, 0.35);
}

.subtitle {
  margin-top: 8px;
  font-size: 17px;
  letter-spacing: 6px;
  color: rgba(233, 238, 251, 0.72);
}
</style>
