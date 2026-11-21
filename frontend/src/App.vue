<script setup>
import { onMounted, reactive, ref } from 'vue'

const project = ref(null)
const applications = ref([])
const message = ref('')
const messageOk = ref(true)
const inputs = reactive({})
const form = reactive({ purpose: '', amount: '' })
const role = ref('member')

async function load() {
  const p = await fetch('/api/project')
  project.value = await p.json()
  const a = await fetch('/api/applications')
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
  const res = await fetch(url, { method: 'POST', headers: { 'X-Role': role.value } })
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
  const res = await fetch(
    `/api/applications?purpose=${encodeURIComponent(form.purpose)}&amount=${form.amount}`,
    { method: 'POST', headers: { 'X-Role': role.value } },
  )
  const body = await res.json()
  messageOk.value = res.ok
  message.value = res.ok ? '已创建草稿' : body.message
  if (res.ok) {
    form.purpose = ''
    form.amount = ''
  }
  await load()
}

onMounted(load)
</script>

<template>
  <div class="container">
    <h1>经费申请</h1>

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

    <div class="form">
      <select v-model="role" class="role">
        <option value="member">我：成员</option>
        <option value="teacher">我：老师</option>
        <option value="finance">我：财务</option>
      </select>
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
            <button v-if="a.status === 'DRAFT'" @click="submit(a.id)">提交</button>
            <span v-if="a.status === 'PENDING_L1'">
              <input v-model="inputs['approve-' + a.id]" type="number" step="0.01" />
              <button @click="approve(a.id)">批准</button>
              <button class="ghost" @click="reject(a.id)">拒绝</button>
            </span>
            <span v-if="a.status === 'APPROVED'">
              <input v-model="inputs['settle-' + a.id]" type="number" step="0.01" />
              <button @click="settle(a.id)">结算</button>
            </span>
            <button
              v-if="a.status === 'PENDING_L1' || a.status === 'APPROVED'"
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
