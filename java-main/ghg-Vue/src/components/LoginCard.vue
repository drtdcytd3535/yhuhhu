<template>
  <form class="card" autocomplete="off" @submit.prevent="onSubmit">
    <h2>踏入仙途</h2>

    <div class="field">
      <span class="ico">☯</span>
      <input id="user" v-model="user" type="text" placeholder=" " required />
      <label for="user">道号 / 账号</label>
    </div>

    <div class="field has-eye">
      <span class="ico">⚔</span>
      <input id="pwd" v-model="pwd" :type="showPwd ? 'text' : 'password'" placeholder=" " required />
      <label for="pwd">秘语 / 密码</label>
      <button type="button" class="eye" :class="{ on: showPwd }" @click="showPwd = !showPwd"
              :title="showPwd ? '隐匿秘语' : '窥探秘语'" tabindex="-1">
        <svg v-if="showPwd" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
          <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7z" />
          <circle cx="12" cy="12" r="3" />
        </svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
          <path d="M2 12s3.5-7 10-7c2 0 3.7.6 5.1 1.5M22 12s-3.5 7-10 7c-2 0-3.7-.6-5.1-1.5" />
          <path d="M9.9 5.2A10.7 10.7 0 0 1 12 5c6.5 0 10 7 10 7a17 17 0 0 1-3 3.7M6 6.5A17 17 0 0 0 2 12s3.5 7 10 7a10.7 10.7 0 0 0 3.1-.5" />
          <line x1="3" y1="3" x2="21" y2="21" />
        </svg>
      </button>
    </div>

    <div class="row">
      <label><input type="checkbox" v-model="remember" /> 铭记此身</label>
      <a href="#" @click.prevent>寻回秘语</a>
    </div>

    <button class="btn" type="submit" :disabled="loading" :style="loading ? 'opacity:.6;cursor:wait' : ''">
      {{ loading ? '验证中…' : '登 录' }}
    </button>

    <div class="foot">尚无道基？<span @click.prevent="emit('go-register')">结缘仙门</span></div>
  </form>

  <div class="toast" :class="{ show: toastShow }">{{ toastMsg }}</div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const emit = defineEmits(['go-register', 'login-success'])

const user = ref('')
const pwd = ref('')
const remember = ref(false)
const showPwd = ref(false)
const toastMsg = ref('')
const toastShow = ref(false)
const loading = ref(false)
let timer
let navTimer

function show(msg) {
  toastMsg.value = msg
  toastShow.value = true
  clearTimeout(timer)
  timer = setTimeout(() => (toastShow.value = false), 2200)
}

async function onSubmit() {
  const u = user.value.trim()
  if (!u || !pwd.value) {
    show('请先凝神，填全道号与秘语')
    return
  }
  if (loading.value) return
  loading.value = true
  try {
    // 真实登录：交由后端校验数据库中注册的账号与密码（t_user.password 为明文）
    const res = await fetch('/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: u, password: pwd.value })
    })
    const data = await res.json()
    if (data.success) {
      if (remember.value) {
        localStorage.setItem('xianti_user', u)
      } else {
        localStorage.removeItem('xianti_user')
      }
      show(`✦ 仙门已开，${u} 道友请入 ✦`)
      // 稍作停顿，让道友看清提示，再进入首页（permission 决定首页是否显示后台管理入口）
      const perm = data.data?.permission
      navTimer = setTimeout(() => emit('login-success', { username: u, permission: perm }), 700)
    } else {
      show(data.message || '秘语有误，难开仙门')
    }
  } catch (e) {
    show('仙门暂时无应，请稍后再试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const saved = localStorage.getItem('xianti_user')
  if (saved) {
    user.value = saved
    remember.value = true
  }
})

onBeforeUnmount(() => {
  clearTimeout(timer)
  clearTimeout(navTimer)
})
</script>

<style scoped>
.card {
  width: min(380px, 92vw);
  padding: 34px 30px 30px;
  border-radius: 20px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.66), rgba(8, 9, 22, 0.72));
  border: 1px solid rgba(111, 233, 255, 0.22);
  box-shadow:
    0 20px 70px rgba(0, 0, 0, 0.55),
    inset 0 0 30px rgba(58, 42, 107, 0.25);
  backdrop-filter: blur(14px);
}

.card h2 {
  text-align: center;
  font-size: 22px;
  letter-spacing: 6px;
  color: var(--gold);
  margin-bottom: 22px;
  font-weight: 400;
}

.field {
  position: relative;
  margin-bottom: 20px;
}

.field input {
  width: 100%;
  padding: 14px 16px 14px 44px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(160, 190, 230, 0.22);
  border-radius: 12px;
  color: #fff;
  font-size: 16px;
  font-family: inherit;
  transition: 0.25s;
  outline: none;
}

.field input:focus {
  border-color: var(--cyan);
  box-shadow:
    0 0 0 3px rgba(111, 233, 255, 0.16),
    0 0 22px rgba(111, 233, 255, 0.25);
  background: rgba(111, 233, 255, 0.06);
}

.field label {
  position: absolute;
  left: 44px;
  top: 14px;
  font-size: 16px;
  color: rgba(233, 238, 251, 0.55);
  pointer-events: none;
  transition: 0.2s;
  letter-spacing: 2px;
}

.field input:focus + label,
.field input:not(:placeholder-shown) + label {
  top: -9px;
  left: 14px;
  font-size: 12px;
  color: var(--cyan);
  background: linear-gradient(#0b0c1c, #0b0c1c) padding-box;
  padding: 0 6px;
}

.field .ico {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  color: rgba(232, 198, 107, 0.8);
}

/* 密码可见性小眼睛 */
.field.has-eye input {
  padding-right: 46px;
}

.eye {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: none;
  background: transparent;
  color: rgba(233, 238, 251, 0.5);
  cursor: pointer;
  transition: 0.2s;
}

.eye:hover {
  color: var(--cyan);
}

.eye.on {
  color: var(--cyan);
}

.eye svg {
  width: 20px;
  height: 20px;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: -4px 0 22px;
  font-size: 13px;
}

.row label {
  display: flex;
  align-items: center;
  gap: 7px;
  color: rgba(233, 238, 251, 0.7);
  cursor: pointer;
  letter-spacing: 1px;
}

.row input {
  accent-color: var(--cyan);
}

.row a {
  color: rgba(111, 233, 255, 0.8);
  text-decoration: none;
  letter-spacing: 1px;
}

.row a:hover {
  text-decoration: underline;
}

.btn {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-family: inherit;
  font-size: 19px;
  letter-spacing: 10px;
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 55%, var(--cyan));
  box-shadow:
    0 10px 30px rgba(232, 198, 107, 0.35),
    0 0 18px rgba(111, 233, 255, 0.3);
  transition: 0.25s;
  font-weight: 700;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow:
    0 14px 40px rgba(232, 198, 107, 0.5),
    0 0 26px rgba(111, 233, 255, 0.45);
}

.btn:active {
  transform: translateY(0);
}

.foot {
  text-align: center;
  margin-top: 18px;
  font-size: 13px;
  color: rgba(233, 238, 251, 0.5);
  letter-spacing: 2px;
}

.foot span {
  color: var(--cyan);
}

.toast {
  position: fixed;
  top: 30px;
  left: 50%;
  transform: translateX(-50%) translateY(-20px);
  background: rgba(8, 9, 22, 0.92);
  border: 1px solid var(--cyan);
  color: var(--cyan);
  padding: 12px 22px;
  border-radius: 10px;
  letter-spacing: 2px;
  opacity: 0;
  transition: 0.35s;
  z-index: 9;
  box-shadow: 0 0 24px rgba(111, 233, 255, 0.3);
}

.toast.show {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}
</style>
