<template>
  <div class="admin-wrap">
    <form class="card" autocomplete="off" @submit.prevent="onSubmit">
      <h2>执事堂</h2>

      <!-- 执事名号 -->
      <div class="field">
        <span class="ico">❖</span>
        <input id="admin-user" v-model="username" type="text" placeholder=" " required />
        <label for="admin-user">执事名号</label>
      </div>

      <!-- 执事秘语 -->
      <div class="field has-eye">
        <span class="ico">⚿</span>
        <input id="admin-pwd" v-model="password" :type="showPwd ? 'text' : 'password'" placeholder=" " required />
        <label for="admin-pwd">执事秘语</label>
        <button
          type="button"
          class="eye"
          :class="{ on: showPwd }"
          @click="showPwd = !showPwd"
          :title="showPwd ? '隐匿秘语' : '窥探秘语'"
          tabindex="-1"
        >
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

      <!-- 登录按钮 -->
      <button class="btn" type="submit" :disabled="loading" :style="loading ? 'opacity:.6;cursor:wait' : ''">
        {{ loading ? '验印中…' : '入 堂' }}
      </button>

      <div class="foot">执事专用通道 · 不对外开放结缘</div>
      <div class="hint">初始执事：admin / admin123</div>
    </form>

    <button class="back-link" @click="emit('back')">← 返回仙门</button>

    <!-- 气泡提示 -->
    <div class="toast" :class="{ show: toastShow }">{{ toastMsg }}</div>
  </div>
</template>

<script setup>
import { ref, onBeforeUnmount } from 'vue'

const emit = defineEmits(['login-success', 'back'])

const username = ref('admin')
const password = ref('')
const showPwd = ref(false)
const loading = ref(false)
const toastMsg = ref('')
const toastShow = ref(false)
let toastTimer

function toast(msg) {
  toastMsg.value = msg
  toastShow.value = true
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toastShow.value = false), 2200)
}

/** 提交登录：POST /api/admin/login，成功后通知父组件进入后台主页 */
async function onSubmit() {
  const name = username.value.trim()
  if (!name || !password.value) {
    toast('请先凝神，填全执事名号与秘语')
    return
  }
  if (loading.value) return
  loading.value = true
  try {
    const res = await fetch('/api/admin/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: name, password: password.value })
    })
    const body = await res.json()
    if (body.success) {
      toast('✦ 执事堂已开 ✦')
      emit('login-success', body.data || {})
    } else {
      toast(body.message || '秘语有误，难入执事堂')
    }
  } catch {
    toast('执事堂暂时无应，请稍后再试')
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => clearTimeout(toastTimer))
</script>

<style scoped>
.admin-wrap {
  position: relative;
  z-index: 3;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.card {
  width: min(380px, 92vw);
  padding: 34px 30px 28px;
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

.eye:hover,
.eye.on {
  color: var(--cyan);
}

.eye svg {
  width: 20px;
  height: 20px;
}

.btn {
  width: 100%;
  padding: 13px 0;
  border: none;
  border-radius: 12px;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 55%, var(--cyan));
  color: #1a1023;
  font-size: 16px;
  letter-spacing: 6px;
  font-family: inherit;
  cursor: pointer;
  transition: 0.25s;
  box-shadow: 0 10px 28px rgba(232, 198, 107, 0.28);
}

.btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 14px 34px rgba(232, 198, 107, 0.38);
}

.foot {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.55);
}

.hint {
  margin-top: 8px;
  text-align: center;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(111, 233, 255, 0.6);
}

.back-link {
  margin-top: 16px;
  padding: 6px 12px;
  border: none;
  background: transparent;
  color: rgba(233, 238, 251, 0.6);
  font-family: inherit;
  font-size: 13px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: 0.2s;
}

.back-link:hover {
  color: var(--cyan);
}

.toast {
  position: fixed;
  left: 50%;
  bottom: 12%;
  transform: translate(-50%, 12px);
  padding: 11px 22px;
  border-radius: 12px;
  background: rgba(8, 9, 22, 0.92);
  border: 1px solid rgba(232, 198, 107, 0.35);
  color: var(--gold);
  font-size: 14px;
  letter-spacing: 2px;
  opacity: 0;
  pointer-events: none;
  transition: 0.28s;
  z-index: 60;
}

.toast.show {
  opacity: 1;
  transform: translate(-50%);
}
</style>
