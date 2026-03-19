<script setup>
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts, User, Reading, Warning } from '@element-plus/icons-vue'

import { getAdminStatistics } from '../../api/admin'

echarts.use([BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

/**
 * 管理端统计分析页面，展示借阅概览、年度趋势、热门图书排行和分类借阅占比。
 */

const loading = ref(false)
const statistics = ref(createDefaultStatistics())
const trendChartRef = ref(null)
const hotBookChartRef = ref(null)
const categoryChartRef = ref(null)

let trendChartInstance = null
let hotBookChartInstance = null
let categoryChartInstance = null

/**
 * 页面挂载后加载统计数据并注册图表自适应事件。
 */
onMounted(() => {
  window.addEventListener('resize', handleChartResize)
  loadStatistics()
})

/**
 * 页面卸载前释放图表实例和事件监听。
 */
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleChartResize)
  disposeCharts()
})

/**
 * 创建默认统计数据。
 *
 * @returns {object} 默认统计数据
 */
function createDefaultStatistics() {
  return {
    totalBorrowCount: 0,
    activeUserCount: 0,
    overdueBookCount: 0,
    returnedBookCount: 0,
    trendYear: new Date().getFullYear(),
    monthlyBorrowTrends: buildDefaultMonthlyTrends(),
    hotBookRanking: [],
    categoryBorrowAnalysis: [],
  }
}

/**
 * 构建默认月度趋势数据。
 *
 * @returns {Array<{monthLabel: string, borrowCount: number, returnCount: number}>} 默认月度趋势
 */
function buildDefaultMonthlyTrends() {
  return Array.from({ length: 12 }, (_, index) => ({
    monthLabel: `${index + 1}月`,
    borrowCount: 0,
    returnCount: 0,
  }))
}

/**
 * 归一化统计数据结构。
 *
 * @param {object} payload 接口返回数据
 * @returns {object} 归一化后的统计数据
 */
function normalizeStatistics(payload) {
  const defaultStatistics = createDefaultStatistics()

  return {
    ...defaultStatistics,
    ...payload,
    totalBorrowCount: Number(payload?.totalBorrowCount || 0),
    activeUserCount: Number(payload?.activeUserCount || 0),
    overdueBookCount: Number(payload?.overdueBookCount || 0),
    returnedBookCount: Number(payload?.returnedBookCount || 0),
    trendYear: Number(payload?.trendYear || defaultStatistics.trendYear),
    monthlyBorrowTrends: normalizeMonthlyTrends(payload?.monthlyBorrowTrends),
    hotBookRanking: normalizeHotBookRanking(payload?.hotBookRanking),
    categoryBorrowAnalysis: normalizeCategoryBorrowAnalysis(payload?.categoryBorrowAnalysis),
  }
}

/**
 * 归一化月度趋势数据。
 *
 * @param {Array<object>} trends 月度趋势数据
 * @returns {Array<{monthLabel: string, borrowCount: number, returnCount: number}>} 归一化结果
 */
function normalizeMonthlyTrends(trends) {
  const defaultTrends = buildDefaultMonthlyTrends()
  if (!Array.isArray(trends)) {
    return defaultTrends
  }

  return defaultTrends.map((defaultTrend, index) => {
    const trend = trends[index] || {}
    return {
      monthLabel: trend.monthLabel || defaultTrend.monthLabel,
      borrowCount: Number(trend.borrowCount || 0),
      returnCount: Number(trend.returnCount || 0),
    }
  })
}

/**
 * 归一化热门图书排行数据。
 *
 * @param {Array<object>} ranking 热门图书排行
 * @returns {Array<{bookName: string, borrowCount: number}>} 归一化结果
 */
function normalizeHotBookRanking(ranking) {
  if (!Array.isArray(ranking)) {
    return []
  }

  return ranking.map((item) => ({
    bookName: item?.bookName || '未命名图书',
    borrowCount: Number(item?.borrowCount || 0),
  }))
}

/**
 * 归一化分类借阅分析数据。
 *
 * @param {Array<object>} analysis 分类借阅分析
 * @returns {Array<{categoryName: string, borrowCount: number}>} 归一化结果
 */
function normalizeCategoryBorrowAnalysis(analysis) {
  if (!Array.isArray(analysis)) {
    return []
  }

  return analysis.map((item) => ({
    categoryName: item?.categoryName || '未分类',
    borrowCount: Number(item?.borrowCount || 0),
  }))
}

/**
 * 加载统计数据。
 */
async function loadStatistics() {
  loading.value = true
  try {
    const response = await getAdminStatistics()
    statistics.value = normalizeStatistics(response)
  } catch (error) {
    statistics.value = createDefaultStatistics()
    ElMessage.error(error.message || '统计数据加载失败，请稍后重试')
  } finally {
    loading.value = false
    await nextTick()
    renderCharts()
  }
}

/**
 * 渲染全部图表。
 */
function renderCharts() {
  renderTrendChart()
  renderHotBookChart()
  renderCategoryChart()
}

/**
 * 渲染借阅趋势折线图。
 */
function renderTrendChart() {
  trendChartInstance = ensureChartInstance(trendChartRef, trendChartInstance)
  if (!trendChartInstance) {
    return
  }

  const trendData = statistics.value.monthlyBorrowTrends
  trendChartInstance.setOption(
    {
      color: ['#1677ff', '#22c55e'],
      tooltip: {
        trigger: 'axis',
      },
      legend: {
        top: 14,
        right: 18,
        itemGap: 18,
      },
      grid: {
        left: 48,
        right: 24,
        top: 72,
        bottom: 34,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: trendData.map((item) => item.monthLabel),
        axisLine: {
          lineStyle: {
            color: '#d0d7de',
          },
        },
        axisLabel: {
          color: '#606266',
        },
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        splitLine: {
          lineStyle: {
            color: '#edf2f7',
          },
        },
        axisLabel: {
          color: '#606266',
        },
      },
      series: [
        {
          name: '借阅次数',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          data: trendData.map((item) => item.borrowCount),
          lineStyle: {
            width: 3,
            color: '#1677ff',
          },
          itemStyle: {
            color: '#1677ff',
          },
          areaStyle: {
            color: 'rgba(22, 119, 255, 0.10)',
          },
        },
        {
          name: '归还次数',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          data: trendData.map((item) => item.returnCount),
          lineStyle: {
            width: 3,
            color: '#22c55e',
          },
          itemStyle: {
            color: '#22c55e',
          },
          areaStyle: {
            color: 'rgba(34, 197, 94, 0.08)',
          },
        },
      ],
    },
    true,
  )
}

/**
 * 渲染热门图书排行柱状图。
 */
function renderHotBookChart() {
  hotBookChartInstance = ensureChartInstance(hotBookChartRef, hotBookChartInstance)
  if (!hotBookChartInstance) {
    return
  }

  const rankingData = statistics.value.hotBookRanking
  const hasRankingData = rankingData.length > 0
  const chartData = hasRankingData ? rankingData : [{ bookName: '暂无借阅数据', borrowCount: 0 }]

  hotBookChartInstance.setOption(
    {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
      },
      grid: {
        left: 24,
        right: 40,
        top: 18,
        bottom: 16,
        containLabel: true,
      },
      xAxis: {
        type: 'value',
        minInterval: 1,
        splitLine: {
          lineStyle: {
            color: '#edf2f7',
          },
        },
        axisLabel: {
          color: '#606266',
        },
      },
      yAxis: {
        type: 'category',
        inverse: true,
        data: chartData.map((item) => item.bookName),
        axisTick: {
          show: false,
        },
        axisLine: {
          show: false,
        },
        axisLabel: {
          color: '#606266',
          formatter: (value) => truncateText(value, 14),
        },
      },
      series: [
        {
          name: '借阅次数',
          type: 'bar',
          barWidth: 16,
          data: chartData.map((item) => item.borrowCount),
          label: {
            show: true,
            position: 'right',
            color: '#1677ff',
          },
          itemStyle: {
            borderRadius: [0, 10, 10, 0],
            color: hasRankingData
              ? new echarts.graphic.LinearGradient(1, 0, 0, 0, [
                  { offset: 0, color: '#1677ff' },
                  { offset: 1, color: '#8ec5ff' },
                ])
              : '#dcdfe6',
          },
        },
      ],
    },
    true,
  )
}

/**
 * 渲染图书借阅分类分析饼图。
 */
function renderCategoryChart() {
  categoryChartInstance = ensureChartInstance(categoryChartRef, categoryChartInstance)
  if (!categoryChartInstance) {
    return
  }

  const analysisData = statistics.value.categoryBorrowAnalysis
  const hasAnalysisData = analysisData.some((item) => item.borrowCount > 0)
  const chartData = hasAnalysisData
    ? analysisData.map((item) => ({
        name: item.categoryName,
        value: item.borrowCount,
      }))
    : [
        {
          name: '暂无数据',
          value: 1,
          itemStyle: {
            color: '#dcdfe6',
          },
        },
      ]

  categoryChartInstance.setOption(
    {
      color: ['#1677ff', '#22c55e', '#f5222d', '#fa8c16', '#13c2c2', '#722ed1'],
      tooltip: {
        trigger: 'item',
      },
      legend: {
        orient: 'vertical',
        right: 12,
        top: 'middle',
        itemWidth: 12,
        itemHeight: 12,
        textStyle: {
          color: '#606266',
        },
      },
      series: [
        {
          name: '分类借阅占比',
          type: 'pie',
          radius: '72%',
          center: ['34%', '52%'],
          data: chartData,
          label: {
            color: '#303133',
            formatter: hasAnalysisData ? '{b}: {d}%' : '{b}',
          },
          emphasis: {
            scale: true,
            scaleSize: 6,
          },
        },
      ],
    },
    true,
  )
}

/**
 * 确保图表实例已初始化。
 *
 * @param {{ value: HTMLElement | null }} containerRef 图表容器引用
 * @param {import('echarts').EChartsType | null} chartInstance 当前图表实例
 * @returns {import('echarts').EChartsType | null} 图表实例
 */
function ensureChartInstance(containerRef, chartInstance) {
  if (!containerRef.value) {
    return null
  }
  if (chartInstance && chartInstance.getDom() === containerRef.value) {
    return chartInstance
  }
  if (chartInstance) {
    chartInstance.dispose()
  }
  return echarts.init(containerRef.value)
}

/**
 * 处理图表尺寸变化。
 */
function handleChartResize() {
  trendChartInstance?.resize()
  hotBookChartInstance?.resize()
  categoryChartInstance?.resize()
}

/**
 * 释放全部图表实例。
 */
function disposeCharts() {
  trendChartInstance?.dispose()
  hotBookChartInstance?.dispose()
  categoryChartInstance?.dispose()
  trendChartInstance = null
  hotBookChartInstance = null
  categoryChartInstance = null
}

/**
 * 截断过长文本，避免图表标签挤压布局。
 *
 * @param {string} text 原始文本
 * @param {number} maxLength 最大长度
 * @returns {string} 处理后的文本
 */
function truncateText(text, maxLength) {
  if (!text || text.length <= maxLength) {
    return text || ''
  }
  return `${text.slice(0, maxLength)}...`
}
</script>

<template>
  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>统计分析</strong>
          <p>查看本月概览、年度借阅趋势与热门图书分布</p>
        </div>
        <el-button type="primary" plain @click="loadStatistics">刷新数据</el-button>
      </div>
    </template>

    <div v-loading="loading" class="statistics-container">
      <el-row :gutter="20" class="statistics-overview">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="statistics-card" shadow="hover">
            <div class="statistics-card-content">
              <div class="statistics-icon" style="background-color: #ecf5ff">
                <el-icon :size="32" color="#1677ff"><TrendCharts /></el-icon>
              </div>
              <div class="statistics-info">
                <div class="statistics-label">本月借阅次数</div>
                <div class="statistics-value">{{ statistics.totalBorrowCount }}</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="statistics-card" shadow="hover">
            <div class="statistics-card-content">
              <div class="statistics-icon" style="background-color: #f0f9ff">
                <el-icon :size="32" color="#22c55e"><User /></el-icon>
              </div>
              <div class="statistics-info">
                <div class="statistics-label">本月活跃用户数</div>
                <div class="statistics-value">{{ statistics.activeUserCount }}</div>
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
                <div class="statistics-label">当前超期未还数</div>
                <div class="statistics-value">{{ statistics.overdueBookCount }}</div>
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
                <div class="statistics-label">本月归还数量</div>
                <div class="statistics-value">{{ statistics.returnedBookCount }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :xs="24">
          <el-card class="statistics-chart-card" shadow="hover">
            <template #header>
              <div class="statistics-chart-header">
                <div>
                  <strong>借阅趋势分析</strong>
                  <p>{{ statistics.trendYear }} 年 1 月至 12 月借阅与归还变化</p>
                </div>
                <el-tag type="primary" effect="plain">{{ statistics.trendYear }} 年</el-tag>
              </div>
            </template>
            <div ref="trendChartRef" class="chart-container chart-container-trend"></div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="14">
          <el-card class="statistics-chart-card" shadow="hover">
            <template #header>
              <div class="statistics-chart-header">
                <div>
                  <strong>热门图书排行</strong>
                  <p>按累计借阅次数展示最受欢迎图书</p>
                </div>
                <el-tag effect="plain">累计热度</el-tag>
              </div>
            </template>
            <div ref="hotBookChartRef" class="chart-container chart-container-panel"></div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="10">
          <el-card class="statistics-chart-card" shadow="hover">
            <template #header>
              <div class="statistics-chart-header">
                <div>
                  <strong>图书借阅分类分析</strong>
                  <p>展示各分类借阅次数在整体中的占比</p>
                </div>
                <el-tag effect="plain">分类占比</el-tag>
              </div>
            </template>
            <div ref="categoryChartRef" class="chart-container chart-container-panel"></div>
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

.statistics-overview {
  margin-bottom: 4px;
}

.statistics-card,
.statistics-chart-card {
  margin-bottom: 20px;
  border: 1px solid #eef2f6;
}

.statistics-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.statistics-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
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
  font-weight: 700;
  color: #303133;
}

.statistics-chart-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.statistics-chart-header p {
  margin: 6px 0 0;
  font-size: 13px;
  color: #909399;
}

.chart-container {
  width: 100%;
}

.chart-container-trend {
  height: 360px;
}

.chart-container-panel {
  height: 360px;
}

@media (max-width: 768px) {
  .statistics-card-content {
    align-items: flex-start;
  }

  .statistics-chart-header {
    flex-direction: column;
  }

  .chart-container-trend,
  .chart-container-panel {
    height: 320px;
  }
}
</style>
