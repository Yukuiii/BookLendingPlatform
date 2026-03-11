<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import {
  getCurrentUserPreference,
  getCurrentUserPreferenceOptions,
  updateCurrentUserPreference,
} from '../api/preference'

/**
 * 个性化设置页面，负责维护首页推荐所需的偏好参数。
 */

const loading = ref(false)
const saving = ref(false)
const preferenceFormRef = ref(null)

const preferenceForm = reactive({
  preferFields: [],
  preferDifficulty: null,
  preferScenes: [],
})

const fieldOptions = ref([])
const sceneOptions = ref([])

const difficultyOptions = [
  { label: '不限', value: null },
  { label: '入门', value: 1 },
  { label: '进阶', value: 2 },
  { label: '专家', value: 3 },
]

/**
 * 页面挂载后加载个性化设置。
 */
onMounted(() => {
  loadPreferencePage()
})

/**
 * 加载页面所需数据。
 */
async function loadPreferencePage() {
  loading.value = true
  try {
    const [preference, options] = await Promise.all([
      getCurrentUserPreference(),
      getCurrentUserPreferenceOptions(),
    ])

    preferenceForm.preferFields = preference?.preferFields || []
    preferenceForm.preferDifficulty = preference?.preferDifficulty ?? null
    preferenceForm.preferScenes = preference?.preferScenes || []
    fieldOptions.value = options?.fieldOptions || []
    sceneOptions.value = options?.sceneOptions || []
  } catch (error) {
    ElMessage.error(error.message || '个性化设置加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 保存个性化设置。
 */
async function handleSave() {
  saving.value = true
  try {
    await updateCurrentUserPreference({
      preferFields: preferenceForm.preferFields,
      preferDifficulty: preferenceForm.preferDifficulty,
      preferScenes: preferenceForm.preferScenes,
    })
    ElMessage.success('个性化设置保存成功，首页推荐已按最新偏好生效')
  } catch (error) {
    ElMessage.error(error.message || '个性化设置保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

/**
 * 重置偏好表单。
 */
function handleReset() {
  preferenceForm.preferFields = []
  preferenceForm.preferDifficulty = null
  preferenceForm.preferScenes = []
}
</script>

<template>
  <div v-loading="loading" class="profile-grid">
    <el-card class="profile-card" shadow="never">
      <template #header>
        <div class="home-section-header">
          <div>
            <strong>推荐偏好</strong>
            <p>设置你关注的技术领域、难度和适用场景，首页“猜你喜欢”会按此推荐</p>
          </div>
        </div>
      </template>

      <el-form ref="preferenceFormRef" class="profile-form" :model="preferenceForm" label-width="96px">
        <el-form-item label="偏好领域">
          <el-select
            v-model="preferenceForm.preferFields"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            clearable
            placeholder="请选择关注的技术领域"
          >
            <el-option v-for="option in fieldOptions" :key="option" :label="option" :value="option" />
          </el-select>
        </el-form-item>

        <el-form-item label="偏好难度">
          <el-select v-model="preferenceForm.preferDifficulty" clearable placeholder="请选择偏好难度">
            <el-option v-for="option in difficultyOptions" :key="String(option.value)" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="偏好场景">
          <el-select
            v-model="preferenceForm.preferScenes"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            clearable
            placeholder="请选择阅读场景"
          >
            <el-option v-for="option in sceneOptions" :key="option" :label="option" :value="option" />
          </el-select>
        </el-form-item>

        <el-form-item class="profile-form-actions">
          <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
          <el-button @click="handleReset">恢复默认</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="profile-card" shadow="never">
      <template #header>
        <div class="home-section-header">
          <div>
            <strong>推荐说明</strong>
            <p>当前推荐会综合你的偏好维度进行排序</p>
          </div>
        </div>
      </template>

      <div class="preference-summary">
        <div class="preference-summary-item">
          <strong>领域匹配</strong>
          <p>优先推荐与你关注的技术领域一致的图书，例如系统、网络、软件工程、人工智能等。</p>
        </div>
        <div class="preference-summary-item">
          <strong>难度匹配</strong>
          <p>如果你选择了入门、进阶或专家，推荐结果会优先靠近对应难度。</p>
        </div>
        <div class="preference-summary-item">
          <strong>阅读场景</strong>
          <p>适用场景会匹配课程学习、工程实践、面试提升、项目实战等标签。</p>
        </div>
      </div>
    </el-card>
  </div>
</template>
