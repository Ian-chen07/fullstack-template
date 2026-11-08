<script setup>
import { onMounted, reactive, ref } from 'vue'

const project = ref(null)
const applications = ref([])
const message = ref('')
const inputs = reactive({})

async function load() {
  const p = await fetch('/api/project')
  project.value = await p.json()
  const a = await fetch('/api/applications')
  applications.value = await a.json()

  applications.value.forEach((item) => {
    if (item.status === 'PENDING_L1') inputs['approve-' + item.id] = item.amount
    if (item.status === 'APPROVED') inputs['settle-' + item.id] = item.approvedAmount
  })
}

async function post(url) {
  const res = await fetch(url, { method: 'POST' })
  const body = await res.json()
  message.value = res.ok ? '操作成功' : body.message
  await load()
}

function submit(id) {
  post(`/api/applications/${id}/submit`)
}

function approve(id) {
  post(`/api/applications/${id}/approve?amount=${inputs['approve-' + id]}`)
}

function settle(id) {
  post(`/api/applications/${id}/settle?actual=${inputs['settle-' + id]}`)
}

onMounted(load)
</script>

<template>
  <h1>经费申请</h1>
  <div v-if="project" class="summary">
    可用：{{ project.availableAmount }} ｜ 冻结：{{ project.frozenAmount }} ｜
    已使用：{{ project.usedAmount }} ｜ 总预算：{{ project.totalAmount }}
  </div>
  <p v-if="message" class="message">{{ message }}</p>

  <table>
    <thead>
      <tr>
        <th>编号</th><th>用途</th><th>申请</th><th>批准</th><th>实际</th><th>状态</th><th>操作</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="a in applications" :key="a.id">
        <td>{{ a.id }}</td>
        <td>{{ a.purpose }}</td>
        <td>{{ a.amount }}</td>
        <td>{{ a.approvedAmount ?? '—' }}</td>
        <td>{{ a.actualAmount ?? '—' }}</td>
        <td>{{ a.status }}</td>
        <td>
          <button v-if="a.status === 'DRAFT'" @click="submit(a.id)">提交</button>
          <span v-if="a.status === 'PENDING_L1'">
            <input v-model="inputs['approve-' + a.id]" type="number" step="0.01" />
            <button @click="approve(a.id)">批准</button>
          </span>
          <span v-if="a.status === 'APPROVED'">
            <input v-model="inputs['settle-' + a.id]" type="number" step="0.01" />
            <button @click="settle(a.id)">结算</button>
          </span>
        </td>
      </tr>
    </tbody>
  </table>
</template>