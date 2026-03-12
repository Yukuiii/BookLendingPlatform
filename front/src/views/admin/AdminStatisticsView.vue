<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts, User, Reading, Warning } from '@element-plus/icons-vue'

import { getAdminStatistics } from '../../api/admin'

/**
 * 管理端统计分析页面，展示总借阅次数、活跃用户数、超期未还图书数、归还图书数量。
 */

const loading = ref(false)
const statistics = ref({
  totalBorrowCount: 0,
  activeUserCount: 0,
  overdueBookCount: 0,
  returnedBookCount: 0,
})

/**
 * 页面挂载后加载统计数据。
 */
onMounted(() => {
  loadStatistics()
})

/**
 * 加载统计数据。
 */
async function loadStatistics() {
  loading.value = true
  try {
    const response = await getAdminStatistics()
    statistics.value = response || {}
  } catch (error) {
    ElMessage.error(error.message || '统计数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>统计分析</strong>
          <p>查看本月图书借阅相关统计数据</p>
        </div>
        <el-button type="primary" plain @click="loadStatistics">刷新数据</el-button>
      </div>
    </template>

    <div v-loading="loading" class="statistics-container">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="statistics-card" shadow="hover">
            <div class="statistics-card-content">
              <div class="statistics-icon" style="background-color: #ecf5ff">
                <el-icon :size="32" color="#409eff"><TrendCharts /></el-icon>
              </div>
              <div class="statistics-info">
                <div class="statistics-label">总借阅次数</div>
                <div class="statistics-value">{{ statistics.totalBorrowCount || 0 }}</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="statistics-card" shadow="hover">
            <div class="statistics-card-content">
              <div class="statistics-icon" style="background-color: #f0f9ff">
                <el-icon :size="32" color="#67c23a"><User /></el-icon>
              </div>
              <div class="statistics-info">
                <div class="statistics-label">活跃用户数</div>
                <div class="statistics-value">{{ statistics.activeUserCount || 0 }}</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="statistics-card" shadow="hover">
            <div class="statistics-card-content">
              <div class="statistics-icon" style="background-color: #fef0f0">
                <el-icon :size="32" color="#f56c6c"><Warning /></el-icon>
              </div>
              <div class="statistics-info">
                <div class="statistics-label">超期未还图书数</div>
                <div class="statistics-value">{{ statistics.overdueBookCount || 0 }}</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="statistics-card" shadow="hover">
            <div class="statistics-card-content">
              <div class="statistics-icon" style="background-color: #f4f4f5">
                <el-icon :size="32" color="#909399"><Reading /></el-icon>
              </div>
              <div class="statistics-info">
                <div class="statistics-label">归还图书数量</div>
                <div class="statistics-value">{{ statistics.returnedBookCount || 0 }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </el-card>
</template>

<style scoped>
.statistics-container {
  min-height: 200px;
}

.statistics-card {
  margin-bottom: 20px;
}

.statistics-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.statistics-icon {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.statistics-info {
  flex: 1;
}

.statistics-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.statistics-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
</style>
