<template>
  <!-- 灵气粒子背景 -->
  <StarField />

  <!-- 远山水墨 -->
  <div class="mountains"></div>
  <div class="mist"></div>

  <!-- ============ 后台管理系统（独立于前台站点） ============ -->
  <template v-if="adminState">
    <!-- 后台登录：只有登录，不开放注册 -->
    <AdminLogin v-if="adminState === 'login'" @login-success="onAdminLogin" @back="exitAdmin" />
    <!-- 后台主页：管理首页展示的数据 -->
    <AdminPanel v-else :admin="admin" @logout="onAdminLogout" @back="exitAdmin" />
  </template>

  <template v-else>
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
    <HomePage v-else :username="username" @logout="onLogout" @admin="adminState = 'login'" />
  </template>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import StarField from './components/StarField.vue'
import ArtBackground from './components/ArtBackground.vue'
import LoginCard from './components/LoginCard.vue'
import RegisterCard from './components/RegisterCard.vue'
import HomePage from './components/HomePage.vue'
import AdminLogin from './components/AdminLogin.vue'
import AdminPanel from './components/AdminPanel.vue'

const mode = ref('login')

/* 后台管理系统视图：null 前台站点 / 'login' 后台登录 / 'panel' 后台主页
 * 与前台登录态互不干涉，退出后台只是回到后台登录页 */
const adminState = ref(null)
const admin = ref({ username: '', realName: '' })

/* 刷新后台页面时用它记住「刚才在后台」，避免 F5 后被踢回前台 */
const ADMIN_FLAG = 'xianni_admin_panel'

function markAdmin(on) {
  try {
    if (on) sessionStorage.setItem(ADMIN_FLAG, '1')
    else sessionStorage.removeItem(ADMIN_FLAG)
  } catch (e) {
    /* 隐私模式忽略 */
  }
}

/** 后台登录成功 → 进入后台主页 */
function onAdminLogin(payload) {
  admin.value = payload || {}
  adminState.value = 'panel'
  markAdmin(true)
}

/** 返回仙门（保留后端 session，下次进来仍是登录态） */
function exitAdmin() {
  adminState.value = null
  markAdmin(false)
}

/** 退出后台：回后台登录页并清掉记忆标记 */
function onAdminLogout() {
  adminState.value = 'login'
  markAdmin(false)
}

/* 刷新恢复：本地标记 + 后端 session 双重确认 */
onMounted(async () => {
  let flag = false
  try {
    flag = sessionStorage.getItem(ADMIN_FLAG) === '1'
  } catch (e) {
    flag = false
  }
  if (!flag) return
  try {
    const res = await fetch('/api/admin/me')
    const body = await res.json()
    if (body.success) {
      admin.value = body.data || {}
      adminState.value = 'panel'
    } else {
      markAdmin(false)
    }
  } catch (e) {
    markAdmin(false)
  }
})

/* 登录态：前端本地状态（后端 session 已由 /api/login 写入）
 * 后续如需接口鉴权，可在此处补 /api/me 校验 */
const logged = ref(false)
const username = ref('道友')

/* 开发态预览入口：?preview=home 跳过登录直达首页，?preview=cave 直达我的洞府，
 * ?preview=admin 直达后台登录页，?preview=register 直达注册页。
 * 仅 import.meta.env.DEV 生效，生产构建不受影响 */
if (import.meta.env.DEV) {
  const preview = new URLSearchParams(location.search).get('preview')
  if (preview === 'home' || preview === 'cave') {
    username.value = '预览道友'
    logged.value = true
  } else if (preview === 'admin') {
    adminState.value = 'login'
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
