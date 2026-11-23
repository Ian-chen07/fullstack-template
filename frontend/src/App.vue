<script setup>
import { onMounted, reactive, ref } from 'vue'

const token = ref(localStorage.getItem('token') || '')
const me = ref(JSON.parse(localStorage.getItem('me') || 'null'))
const loginForm = reactive({ username: '', password: '' })
const loginError = ref('')

const project = ref(null)
const applications = ref([])
const message = ref('')
const messageOk = ref(true)
const inputs = reactive({})
const form = reactive({ purpose: '', amount: '' })

const roleLabel = { member: '成员', teacher: '老师', finance: '财务' }

function saveSession(body) {
  token.value = body.token
  me.value = { username: body.username, role: body.role }
  localStorage.setItem('token', body.token)
  localStorage.setItem('me', JSON.stringify(me.value))
}

async function login() {
  const url =
    `/api/login?username=${encodeURIComponent(loginForm.username)}` +
    `&password=${encodeURIComponent(loginForm.password)}`
  const res = await fetch(url, { method: 'POST' })
  const body = await res.json()
  if (!res.ok) {
    loginError.value = body.message || '登录失败'
    return
  }
  loginError.value = ''
  saveSession(body)
  await load()
}

function logout() {
  fetch('/api/logout', { method: 'POST', headers: { 'X-Token': token.value } })
  token.value = ''
  me.value = null
  project.value = null
  applications.value = []
  localStorage.removeItem('token')
  localStorage.removeItem('me')
}

async function authedFetch(url, options = {}) {
  const headers = { 'X-Token': token.value, ...(options.headers || {}) }
  return fetch(url, { ...options, headers })
}

async function load() {
  if (!token.value) return

  const p = await authedFetch('/api/project')
  if (p.status === 401) return logout()
  project.value = await p.json()

  const a = await authedFetch('/api/applications')
  if (a.status === 401) return logout()
  applications.value = await a.json()

  applications.value.forEach((item) => {
    const approveKey = 'approve-' + item.id
    const settleKey = 'settle-' + item.id
    if (item.status === 'PENDING_L1' && inputs[approveKey] === undefined) {
      inputs[approveKey] = item.amount
    }
    if (item.status === 'APPROVED' && inputs[settleKey] === undefined) {
      inputs[settleKey] = item.approvedAmount
    }
  })
}

async function request(url) {
  const res = await authedFetch(url, { method: 'POST' })
  if (res.status === 401) return logout()
  const body = await res.json()
  messageOk.value = res.ok
  message.value = res.ok ? '操作成功' : body.message
  await load()
}

function submit(id) {
  request(`/api/applications/${id}/submit`)
}

function approve(id) {
  request(`/api/applications/${id}/approve?amount=${inputs['approve-' + id]}`)
}

function settle(id) {
  request(`/api/applications/${id}/settle?actual=${inputs['settle-' + id]}`)
}

function reject(id) {
  const reason = prompt('拒绝原因（必填）')
  if (reason === null) return
  request(`/api/applications/${id}/reject?reason=${encodeURIComponent(reason)}`)
}

function cancel(id) {
  const reason = prompt('取消原因（必填）')
  if (reason === null) return
  request(`/api/applications/${id}/cancel?reason=${encodeURIComponent(reason)}`)
}

async function create() {
  if (!form.purpose.trim()) {
    messageOk.value = false
    message.value = '请填写用途'
    return
  }
  const res = await authedFetch(
    `/api/applications?purpose=${encodeURIComponent(form.purpose)}&amount=${form.amount}`,
    { method: 'POST' },
  )
  if (res.status === 401) return logout()
  const body = await res.json()
  messageOk.value = res.ok
  message.value = res.ok ? '已创建草稿' : body.message
  if (res.ok) {
    form.purpose = ''
    form.amount = ''
  }
  await load()
}

function canSubmit() {
  return me.value?.role === 'member'
}
function canApprove() {
  return me.value?.role === 'teacher'
}
function canSettle() {
  return me.value?.role === 'finance'
}

onMounted(load)
</script>

<template>
  <div class="container">
    <div v-if="!token" class="login">
      <h1>经费申请 · 登录</h1>
      <input v-model="loginForm.username" placeholder="用户名，如 member1" class="wide" />
      <input v-model="loginForm.password" type="password" placeholder="密码" class="wide" />
      <button @click="login">登录</button>
      <p v-if="loginError" class="message error">{{ loginError }}</p>
      <p class="hint">测试账号：member1 / teacher1 / finance1，密码都是 123456</p>
    </div>

    <div v-else>
      <div class="topbar">
        <h1>经费申请</h1>
        <div class="who">
          当前用户：{{ me?.username }}（{{ roleLabel[me?.role] || me?.role }}）
          <button class="ghost" @click="logout">退出</button>
        </div>
      </div>

      <div class="cards" v-if="project">
        <div class="card">
          <div class="label">可用</div>
          <div class="value">{{ project.availableAmount }}</div>
        </div>
        <div class="card">
          <div class="label">冻结</div>
          <div class="value">{{ project.frozenAmount }}</div>
        </div>
        <div class="card">
          <div class="label">已使用</div>
          <div class="value">{{ project.usedAmount }}</div>
        </div>
        <div class="card">
          <div class="label">总预算</div>
          <div class="value">{{ project.totalAmount }}</div>
        </div>
      </div>

      <div class="form" v-if="canSubmit()">
        <input v-model="form.purpose" class="wide" placeholder="用途，比如 买试剂" />
        <input v-model="form.amount" type="number" step="0.01" placeholder="金额" />
        <button @click="create">新建申请</button>
      </div>

      <p v-if="message" class="message" :class="messageOk ? 'ok' : 'error'">{{ message }}</p>

      <table>
        <thead>
          <tr>
            <th>编号</th><th>用途</th><th>申请</th><th>批准</th><th>实际</th><th>状态</th><th>原因</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in applications" :key="a.id">
            <td>{{ a.id }}</td>
            <td>{{ a.purpose }}</td>
            <td>{{ a.amount }}</td>
            <td>{{ a.approvedAmount ?? '—' }}</td>
            <td>{{ a.actualAmount ?? '—' }}</td>
            <td><span class="badge" :class="'s-' + a.status">{{ a.status }}</span></td>
            <td>{{ a.reason ?? '—' }}</td>
            <td>
              <button v-if="a.status === 'DRAFT' && canSubmit()" @click="submit(a.id)">提交</button>
              <span v-if="a.status === 'PENDING_L1' && canApprove()">
                <input v-model="inputs['approve-' + a.id]" type="number" step="0.01" />
                <button @click="approve(a.id)">批准</button>
                <button class="ghost" @click="reject(a.id)">拒绝</button>
              </span>
              <span v-if="a.status === 'APPROVED' && canSettle()">
                <input v-model="inputs['settle-' + a.id]" type="number" step="0.01" />
                <button @click="settle(a.id)">结算</button>
              </span>
              <button
                v-if="(a.status === 'PENDING_L1' || a.status === 'APPROVED') && canSubmit()"
                class="ghost"
                @click="cancel(a.id)"
              >
                取消
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <p v-if="!applications.length" class="empty">还没有申请单，用上面的表单新建一条。</p>
    </div>
  </div>
</template>

<style>
body {
  margin: 0;
  background: #f6f7f9;
  color: #1f2329;
  font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 20px 60px;
}

h1 {
  font-size: 26px;
  margin: 0 0 20px;
}

.login {
  max-width: 360px;
  margin: 80px auto;
  background: #fff;
  padding: 28px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(31, 35, 41, 0.08);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.login h1 {
  font-size: 20px;
  margin: 0 0 8px;
}

.hint {
  font-size: 12px;
  color: #646a73;
  margin: 4px 0 0;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.topbar h1 {
  margin: 0;
}

.who {
  font-size: 13px;
  color: #646a73;
  display: flex;
  align-items: center;
  gap: 8px;
}

.cards {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.card {
  flex: 1 1 150px;
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
  box-shadow: 0 1px 3px rgba(31, 35, 41, 0.08);
}

.card .label {
  font-size: 12px;
  color: #646a73;
}

.card .value {
  font-size: 22px;
  font-weight: 600;
  margin-top: 6px;
}

.form {
  display: flex;
  gap: 10px;
  align-items: center;
  background: #fff;
  padding: 14px 16px;
  border-radius: 10px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(31, 35, 41, 0.08);
}

.form input.wide {
  width: 260px;
}

table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(31, 35, 41, 0.08);
}

th,
td {
  padding: 10px 12px;
  text-align: left;
  font-size: 14px;
  border-bottom: 1px solid #eef0f3;
}

th {
  background: #fafbfc;
  color: #646a73;
  font-weight: 500;
}

tr:last-child td {
  border-bottom: 0;
}

input {
  border: 1px solid #dee0e3;
  border-radius: 6px;
  padding: 6px 8px;
  font-size: 13px;
  width: 90px;
}

.login input.wide {
  width: auto;
}

button {
  border: 0;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 13px;
  cursor: pointer;
  background: #3370ff;
  color: #fff;
  margin-right: 6px;
}

button:hover {
  opacity: 0.9;
}

button.ghost {
  background: #f2f3f5;
  color: #1f2329;
}

.badge {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  background: #f2f3f5;
  color: #646a73;
}

.s-DRAFT {
  background: #eef4ff;
  color: #3370ff;
}

.s-PENDING_L1 {
  background: #fff7e8;
  color: #d97000;
}

.s-APPROVED {
  background: #e8f8ef;
  color: #2ea121;
}

.s-SETTLED {
  background: #f2f3f5;
  color: #646a73;
}

.s-REJECTED,
.s-CANCELED {
  background: #fdeceb;
  color: #d83931;
}

.message {
  margin: 0 0 14px;
  font-size: 14px;
}

.message.ok {
  color: #2ea121;
}

.message.error {
  color: #d83931;
}

.empty {
  color: #646a73;
  font-size: 14px;
}
</style>
