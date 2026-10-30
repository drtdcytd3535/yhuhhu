<template>
  <section class="adm">
    <!-- ================= 顶栏 ================= -->
    <header class="adm-top">
      <div class="adm-brand">
        <span class="adm-seal">仙逆</span>
        <div>
          <h2>执事堂</h2>
          <p>{{ admin?.realName || admin?.username || '执事' }} · 仙门数据管理</p>
        </div>
      </div>
      <div class="adm-ops">
        <button class="op" @click="emit('back')">返回仙门</button>
        <button class="op danger" @click="emit('logout')">退出执事堂</button>
      </div>
    </header>

    <!-- ================= 概览统计 ================= -->
    <div class="adm-stats">
      <div class="stat">
        <p class="sv">{{ stats.content }}</p>
        <p class="sl">收录条目</p>
      </div>
      <div class="stat">
        <p class="sv">{{ stats.online }}</p>
        <p class="sl">在架</p>
      </div>
      <div class="stat">
        <p class="sv">{{ stats.offline }}</p>
        <p class="sl">下架</p>
      </div>
      <div class="stat">
        <p class="sv">{{ stats.notices }}</p>
        <p class="sl">宗门公告</p>
      </div>
    </div>

    <!-- ================= 页签 ================= -->
    <nav class="adm-tabs">
      <button :class="{ on: tab === 'content' }" @click="tab = 'content'">内容管理</button>
      <button :class="{ on: tab === 'notice' }" @click="tab = 'notice'">公告管理</button>
    </nav>

    <!-- ================= 内容管理 ================= -->
    <div v-if="tab === 'content'" class="panel">
      <div class="panel-bar">
        <input
          v-model="keyword"
          class="sinput"
          placeholder="检索标题 / 作者"
          @keyup.enter="loadContents(1)"
        />
        <button class="op" @click="loadContents(1)">检索</button>
        <button class="op primary" @click="openEditor(null)">＋ 录入典籍</button>
      </div>

      <div v-if="loading" class="empty">正在翻阅典籍…</div>
      <table v-else-if="contents.length" class="tbl">
        <thead>
          <tr>
            <th style="width: 56px">ID</th>
            <th>标题</th>
            <th style="width: 110px">分类</th>
            <th style="width: 90px">作者</th>
            <th style="width: 70px">热度</th>
            <th style="width: 70px">浏览</th>
            <th style="width: 70px">状态</th>
            <th style="width: 170px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in contents" :key="c.id">
            <td class="dim">{{ c.id }}</td>
            <td class="tt">{{ c.title }}</td>
            <td class="dim">{{ c.categoryName || '-' }}</td>
            <td class="dim">{{ c.author || '-' }}</td>
            <td class="dim">{{ c.hot ?? 0 }}</td>
            <td class="dim">{{ c.views ?? 0 }}</td>
            <td>
              <span class="tag" :class="c.status === 1 ? 'up' : 'down'">
                {{ c.status === 1 ? '在架' : '下架' }}
              </span>
            </td>
            <td class="acts">
              <button class="mini" @click="openEditor(c)">修订</button>
              <button class="mini" @click="toggleStatus(c)">{{ c.status === 1 ? '下架' : '上架' }}</button>
              <button class="mini danger" @click="removeContent(c)">焚毁</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">仙门暂无典籍，点右上角录入。</div>

      <div v-if="pages > 1" class="pager">
        <button class="mini" :disabled="page <= 1" @click="loadContents(page - 1)">上一页</button>
        <span class="dim">第 {{ page }} / {{ pages }} 页 · 共 {{ total }} 卷</span>
        <button class="mini" :disabled="page >= pages" @click="loadContents(page + 1)">下一页</button>
      </div>
    </div>

    <!-- ================= 公告管理 ================= -->
    <div v-else class="panel">
      <div class="panel-bar">
        <input v-model="noticeForm.content" class="sinput wide" placeholder="公告内容" />
        <input v-model="noticeForm.dateLabel" class="sinput narrow" placeholder="时间标签" />
        <input v-model.number="noticeForm.sortNo" class="sinput narrow" type="number" placeholder="排序" />
        <button class="op primary" @click="publishNotice">颁下公告</button>
      </div>

      <ul v-if="notices.length" class="nlist">
        <li v-for="n in notices" :key="n.id">
          <span class="nlabel">{{ n.dateLabel || '公告' }}</span>
          <span class="ncontent">{{ n.content }}</span>
          <button class="mini danger" @click="removeNotice(n)">撤下</button>
        </li>
      </ul>
      <div v-else class="empty">暂无公告。</div>
    </div>

    <!-- ================= 典籍编辑弹窗 ================= -->
    <div v-if="editorOpen" class="mask" @click.self="editorOpen = null">
      <div class="editor">
        <h3>{{ form.id ? '修订典籍' : '录入典籍' }}</h3>

        <label class="fld">
          <span>标题 *</span>
          <input v-model="form.title" type="text" />
        </label>

        <label class="fld">
          <span>分类 *</span>
          <select v-model="form.categoryId">
            <option :value="null">— 请选择 —</option>
            <option v-for="c in categoryOptions" :key="c.id" :value="c.id">{{ c.path }}</option>
          </select>
        </label>

        <label class="fld">
          <span>作者 / 出处</span>
          <input v-model="form.author" type="text" />
        </label>

        <label class="fld">
          <span>标签（逗号分隔）</span>
          <input v-model="form.tags" type="text" />
        </label>

        <label class="fld">
          <span>简介</span>
          <textarea v-model="form.summary" rows="2"></textarea>
        </label>

        <label class="fld">
          <span>正文</span>
          <textarea v-model="form.content" rows="6"></textarea>
        </label>

        <div class="fld-row">
          <label class="fld">
            <span>热度</span>
            <input v-model.number="form.hot" type="number" />
          </label>
          <label class="fld">
            <span>状态</span>
            <select v-model.number="form.status">
              <option :value="1">在架</option>
              <option :value="0">下架</option>
            </select>
          </label>
        </div>

        <div class="editor-ops">
          <button class="op" @click="editorOpen = null">作罢</button>
          <button class="op primary" :disabled="saving" @click="saveContent">
            {{ saving ? '落笔中…' : '落 笔' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 气泡提示 -->
    <div class="toast" :class="{ show: toastShow }">{{ toastMsg }}</div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  admin: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['logout', 'back'])

/* ---------------- 状态 ---------------- */
const tab = ref('content')
const contents = ref([])
const notices = ref([])
const categories = ref([])
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const pages = ref(0)
const loading = ref(false)
const saving = ref(false)
const editorOpen = ref(false)
const form = ref(blankForm())
const noticeForm = ref({ content: '', dateLabel: '', sortNo: 0 })
const toastMsg = ref('')
const toastShow = ref(false)
let toastTimer

function blankForm() {
  return {
    id: null,
    title: '',
    summary: '',
    content: '',
    categoryId: null,
    tags: '',
    author: '',
    hot: 0,
    status: 1
  }
}

function toast(msg) {
  toastMsg.value = msg
  toastShow.value = true
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toastShow.value = false), 2200)
}

/* ---------------- 派生数据 ---------------- */
const stats = computed(() => {
  const online = contents.value.filter((c) => c.status === 1).length
  return {
    content: total.value,
    online,
    offline: total.value - online,
    notices: notices.value.length
  }
})

/** 分类树拍平成「顶级 / 子级」下拉选项 */
const categoryOptions = computed(() => {
  const out = []
  const walk = (list, prefix) => {
    for (const c of list) {
      const path = prefix ? `${prefix} / ${c.name}` : c.name
      const children = c.children || []
      if (children.length === 0) {
        out.push({ id: c.id, name: c.name, path })
      } else {
        walk(children, path)
      }
    }
  }
  walk(categories.value, '')
  return out
})

/* ---------------- 请求封装 ---------------- */
/* 登录态被服务端判定失效时，只弹一次提示并退回后台登录页 */
let kicked = false

async function api(url, options) {
  try {
    const res = await fetch(url, options)
    const body = await res.json()
    if (body.success) return body.data
    if (body.message && body.message.includes('尚未登入执事堂')) {
      if (!kicked) {
        kicked = true
        toast('登录态已失效，请重新验印')
        setTimeout(() => emit('logout'), 900)
      }
      return null
    }
    toast(body.message || '仙门暂无回应')
    return null
  } catch {
    toast('仙门暂时无应，请稍后再试')
    return null
  }
}

/* ---------------- 数据加载 ---------------- */
async function loadContents(p = 1) {
  loading.value = true
  const params = new URLSearchParams({
    keyword: keyword.value || '',
    page: String(p),
    size: String(size.value)
  })
  const data = await api('/api/admin/contents?' + params.toString())
  if (data) {
    contents.value = data.items || []
    total.value = data.total || 0
    page.value = data.page || 1
    pages.value = data.pages || 0
  }
  loading.value = false
}

async function loadNotices() {
  const data = await api('/api/admin/notices')
  if (data) notices.value = data
}

async function loadCategories() {
  const data = await api('/api/categories')
  if (data) categories.value = data
}

/* ---------------- 典籍操作 ---------------- */
function openEditor(item) {
  form.value = item
    ? {
        id: item.id,
        title: item.title || '',
        summary: item.summary || '',
        content: item.content || '',
        categoryId: item.categoryId ?? null,
        tags: item.tags || '',
        author: item.author || '',
        hot: item.hot ?? 0,
        status: item.status ?? 1
      }
    : blankForm()
  editorOpen.value = true
}

async function saveContent() {
  if (!form.value.title.trim()) {
    toast('典籍不可无名')
    return
  }
  if (form.value.categoryId == null) {
    toast('请择一处分类安放')
    return
  }
  saving.value = true
  const data = await api('/api/admin/contents', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(form.value)
  })
  saving.value = false
  if (data) {
    editorOpen.value = false
    toast('已录入仙门')
    loadContents(page.value)
  }
}

async function toggleStatus(item) {
  const next = item.status === 1 ? 0 : 1
  const ok = await api(`/api/admin/contents/${item.id}/status`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status: next })
  })
  if (ok) {
    toast(next === 1 ? '典籍已上架' : '典籍已下架')
    loadContents(page.value)
  }
}

async function removeContent(item) {
  if (!window.confirm(`确认焚毁《${item.title}》？此操作不可复原。`)) return
  const ok = await api(`/api/admin/contents/${item.id}`, { method: 'DELETE' })
  if (ok !== null) {
    toast('典籍已焚')
    loadContents(page.value)
  }
}

/* ---------------- 公告操作 ---------------- */
async function publishNotice() {
  if (!noticeForm.value.content.trim()) {
    toast('公告不可无字')
    return
  }
  const ok = await api('/api/admin/notices', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(noticeForm.value)
  })
  if (ok) {
    noticeForm.value = { content: '', dateLabel: '', sortNo: 0 }
    toast('公告已颁下')
    loadNotices()
  }
}

async function removeNotice(item) {
  if (!window.confirm(`确认撤下公告「${item.content}」？`)) return
  const ok = await api(`/api/admin/notices/${item.id}`, { method: 'DELETE' })
  if (ok !== null) {
    toast('公告已撤下')
    loadNotices()
  }
}

/* ---------------- 初始化 ---------------- */
onMounted(() => {
  loadCategories()
  loadContents(1)
  loadNotices()
})

onBeforeUnmount(() => clearTimeout(toastTimer))
</script>

<style scoped>
.adm {
  position: relative;
  z-index: 3;
  max-width: 1120px;
  margin: 0 auto;
  padding: 26px 24px 48px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.adm-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.adm-brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.adm-seal {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: 12px;
  background: linear-gradient(160deg, rgba(232, 198, 107, 0.18), rgba(111, 233, 255, 0.08));
  border: 1px solid rgba(232, 198, 107, 0.45);
  font-family: 'Ma Shan Zheng', serif;
  font-size: 20px;
  color: var(--gold);
}

.adm-brand h2 {
  font-family: 'Ma Shan Zheng', serif;
  font-size: 26px;
  letter-spacing: 6px;
  color: var(--gold);
}

.adm-brand p {
  margin-top: 2px;
  font-size: 12px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.55);
}

.adm-ops {
  display: flex;
  gap: 10px;
}

.adm-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat {
  padding: 14px 16px;
  border-radius: 14px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.62), rgba(8, 9, 22, 0.7));
  border: 1px solid rgba(111, 233, 255, 0.16);
}

.sv {
  font-family: 'Ma Shan Zheng', serif;
  font-size: 24px;
  color: var(--gold);
}

.sl {
  margin-top: 2px;
  font-size: 12px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.5);
}

.adm-tabs {
  display: flex;
  gap: 8px;
}

.adm-tabs button {
  padding: 8px 20px;
  border-radius: 10px;
  border: 1px solid rgba(111, 233, 255, 0.22);
  background: rgba(111, 233, 255, 0.05);
  color: rgba(233, 238, 251, 0.7);
  font-family: inherit;
  font-size: 14px;
  letter-spacing: 3px;
  cursor: pointer;
  transition: 0.22s;
}

.adm-tabs button:hover {
  color: var(--cyan);
}

.adm-tabs button.on {
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 60%, var(--cyan));
  border-color: transparent;
}

.panel {
  padding: 18px 18px 16px;
  border-radius: 16px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.62), rgba(8, 9, 22, 0.7));
  border: 1px solid rgba(111, 233, 255, 0.18);
  box-shadow: inset 0 0 26px rgba(58, 42, 107, 0.24);
  backdrop-filter: blur(10px);
}

.panel-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.sinput {
  flex: 1;
  min-width: 150px;
  padding: 9px 12px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(160, 190, 230, 0.22);
  color: #fff;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  transition: 0.22s;
}

.sinput:focus {
  border-color: var(--cyan);
  box-shadow: 0 0 0 3px rgba(111, 233, 255, 0.14);
}

.sinput.wide {
  flex: 3;
}

.sinput.narrow {
  flex: 0 0 110px;
  min-width: 90px;
}

.op {
  padding: 8px 16px;
  border-radius: 10px;
  border: 1px solid rgba(111, 233, 255, 0.28);
  background: rgba(111, 233, 255, 0.07);
  color: rgba(111, 233, 255, 0.92);
  font-family: inherit;
  font-size: 14px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: 0.22s;
}

.op:hover {
  color: #1a1023;
  background: linear-gradient(100deg, var(--cyan), #b8f4ff 70%, var(--gold));
  border-color: transparent;
}

.op.primary {
  border-color: rgba(232, 198, 107, 0.45);
  background: rgba(232, 198, 107, 0.12);
  color: var(--gold);
}

.op.primary:hover:not(:disabled) {
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 60%, var(--cyan));
  border-color: transparent;
}

.op.danger {
  border-color: rgba(226, 75, 74, 0.4);
  background: rgba(226, 75, 74, 0.08);
  color: rgba(247, 193, 193, 0.9);
}

.op.danger:hover:not(:disabled) {
  background: rgba(226, 75, 74, 0.22);
  color: #fff;
  border-color: transparent;
}

.op:disabled {
  opacity: 0.6;
  cursor: wait;
}

.tbl {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.tbl th {
  text-align: left;
  padding: 9px 10px;
  font-weight: 400;
  font-size: 12px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.5);
  border-bottom: 1px solid rgba(111, 233, 255, 0.14);
}

.tbl td {
  padding: 10px;
  border-bottom: 1px solid rgba(160, 190, 230, 0.08);
  vertical-align: middle;
}

.tbl tbody tr:hover {
  background: rgba(58, 42, 107, 0.22);
}

.tt {
  color: #e9eefb;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dim {
  color: rgba(233, 238, 251, 0.5);
  font-size: 13px;
}

.tag {
  display: inline-block;
  padding: 3px 9px;
  border-radius: 7px;
  font-size: 12px;
  letter-spacing: 1px;
}

.tag.up {
  color: #9fe1cb;
  background: rgba(29, 158, 117, 0.16);
  border: 1px solid rgba(29, 158, 117, 0.35);
}

.tag.down {
  color: rgba(233, 238, 251, 0.45);
  background: rgba(160, 190, 230, 0.08);
  border: 1px solid rgba(160, 190, 230, 0.18);
}

.acts {
  display: flex;
  gap: 6px;
}

.mini {
  padding: 5px 11px;
  border-radius: 8px;
  border: 1px solid rgba(160, 190, 230, 0.2);
  background: rgba(255, 255, 255, 0.04);
  color: rgba(233, 238, 251, 0.75);
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.2s;
}

.mini:hover:not(:disabled) {
  border-color: rgba(232, 198, 107, 0.45);
  color: var(--gold);
}

.mini.danger:hover:not(:disabled) {
  border-color: rgba(226, 75, 74, 0.5);
  color: #f7c1c1;
}

.mini:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.nlist {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nlist li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(8, 9, 22, 0.45);
  border: 1px solid rgba(111, 233, 255, 0.1);
}

.nlabel {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 12px;
  color: var(--cyan);
  background: rgba(111, 233, 255, 0.1);
}

.ncontent {
  flex: 1;
  font-size: 14px;
  color: #e9eefb;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty {
  padding: 24px 4px;
  text-align: center;
  font-size: 13px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.45);
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-top: 14px;
  font-size: 13px;
}

.mask {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 80;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(3, 4, 10, 0.72);
  backdrop-filter: blur(4px);
}

.editor {
  width: min(520px, 100%);
  max-height: 86vh;
  overflow-y: auto;
  padding: 24px 24px 20px;
  border-radius: 18px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.96), rgba(8, 9, 22, 0.98));
  border: 1px solid rgba(232, 198, 107, 0.32);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.6);
}

.editor h3 {
  margin-bottom: 18px;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 21px;
  letter-spacing: 5px;
  color: var(--gold);
}

.fld {
  display: block;
  margin-bottom: 14px;
}

.fld > span {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.55);
}

.fld input,
.fld select,
.fld textarea {
  width: 100%;
  padding: 9px 12px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(160, 190, 230, 0.22);
  color: #fff;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  transition: 0.22s;
  resize: vertical;
}

.fld input:focus,
.fld select:focus,
.fld textarea:focus {
  border-color: var(--cyan);
  box-shadow: 0 0 0 3px rgba(111, 233, 255, 0.14);
}

.fld select option {
  background: #0b0c1c;
  color: #e9eefb;
}

.fld-row {
  display: flex;
  gap: 12px;
}

.fld-row .fld {
  flex: 1;
}

.editor-ops {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 6px;
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
  z-index: 90;
}

.toast.show {
  opacity: 1;
  transform: translate(-50%);
}

@media (max-width: 640px) {
  .adm-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
