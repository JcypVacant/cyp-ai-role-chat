<template>
  <div class="role-chat-container">
    <!-- 角色信息头部 -->
    <div class="role-header">
      <a-avatar :src="roleInfo?.cover || defaultAvatar" size="large" />
      <div class="role-info">
        <h2>{{ roleInfo?.roleName|| 'AI角色' }}</h2>
        <p>{{ roleInfo?.description || '正在进行语音对话...' }}</p>
      </div>
    </div>

    <!-- 对话消息区域 -->
    <div class="chat-messages" ref="chatContainer" @scroll="handleScroll">
      <!-- 加载更多提示（顶部） -->
      <div v-if="isLoadingMore" class="loading-tip">加载更多历史消息...</div>
      <!-- 加载历史提示 -->
      <div v-if="isLoadingHistory && chatHistory.length === 0" class="loading-tip">加载历史消息中...</div>

      <!-- 空状态 -->
      <div v-if="!isLoadingHistory && chatHistory.length === 0" class="empty-tip">
        开始与{{ roleInfo?.roleName || 'AI角色' }}的对话吧~
      </div>

      <!-- 消息列表 -->
      <div
        v-for="msg in chatHistory"
        :key="msg.id"
        :class="['message-item', msg.messageType === 'user' ? 'user-message' : 'ai-message']"
      >
        <a-avatar
          :src="getAvatarUrl(msg)"
          size="small"
          class="message-avatar"
        />
        <div class="message-bubble">
          <p class="message-text">{{ msg.message }}</p>
          <audio
            v-if="msg.messageFormat === 'voice' && msg.voiceUrl"
            controls
            :src="fixVoiceUrl(msg.voiceUrl)"
            class="message-audio"
          />
        </div>
      </div>
    </div>

    <!-- 底部录音控制区域：固定在底部，带背景层隔离 -->
    <div class="audio-control-bar">
      <!-- 录音核心控制区 -->
      <div class="audio-control-wrapper">
        <!-- 1. 未录音状态：显示麦克风图标 + 提示文本 -->
        <div v-if="recordingStatus === 'idle'" class="control-idle">
          <button
            class="record-btn idle-btn"
            @click="startRecording"
            :disabled="isRecordingDisabled"
          >
            <AudioTwoTone />
            <span class="btn-text">按住录音</span>
          </button>
          <p class="control-tip">按住说话，松开自动结束</p>
        </div>

        <!-- 2. 录音中状态：显示停止图标 + 实时时长 + 脉冲动画 -->
        <div v-else-if="recordingStatus === 'recording'" class="control-recording">
          <div class="recording-animation">
            <!-- 脉冲圆环动画 -->
            <div class="pulse-ring"></div>
            <button class="record-btn recording-btn" @click="stopRecording">
              <StopTwoTone theme="filled" twoToneColor="#fff" class="icon-stop" />
            </button>
          </div>
          <!-- 实时录音时长 -->
          <span class="recording-time">{{ formatRecordingTime(recordingTime) }}</span>
          <p class="control-tip">点击按钮结束录音</p>
        </div>

        <!-- 3. 录音完成状态：显示发送/取消按钮 + 录音时长 -->
        <div v-else-if="recordingStatus === 'recorded'" class="control-recorded">
          <!-- 录音时长 -->
          <span class="recorded-time">录音时长：{{ formatRecordingTime(recordingTime) }}</span>
          <!-- 操作按钮组 -->
          <div class="recorded-btns">
            <button class="operate-btn cancel-btn" @click="cancelRecording">
              <CloseCircleTwoTone />
              <span>取消</span>
            </button>
            <button class="operate-btn send-btn" @click="sendRecording" :disabled="!recordedVoiceUrl">
              <CheckCircleTwoTone />
              <span>发送</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, nextTick, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { chatByVoice, getChatHistory } from '@/api/chatController';
import { getRoleById } from '@/api/roleController'; // 需要实现该接口
import { useLoginUserStore } from '@/stores/loginUser';
// 状态定义
const isRecording = ref(false);
const isLoadingHistory = ref(true);
const mediaRecorder = ref<MediaRecorder | null>(null);
const audioChunks = ref<Blob[]>([]);
const chatHistory = ref<API.ChatHistory[]>([]);
const roleInfo = ref<API.Role | null>(null);
const userAvatar = ref('');
const defaultAvatar = 'https://api.dicebear.com/6.x/identicon/svg?seed=ai';
const chatContainer = ref<HTMLDivElement>(null);

// 路由与用户状态
const route = useRoute();
const router = useRouter();
const loginUserStore = useLoginUserStore();
// 在函数组件中
const roleId = route.params.id as string  // 类型断言为string
// 新增分页相关状态
const currentPage = ref(1); // 当前页码
const totalCount = ref(0); // 总条数
const hasMore = ref(true); // 是否有更多数据
const isLoadingMore = ref(false); // 加载更多中
// 初始化
onMounted(async () => {
  // 检查登录状态
  await loginUserStore.fetchLoginUser();
  userAvatar.value = loginUserStore.loginUser.userAvatar || '';
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录');
    router.push(`/user/login?redirect=/role/chat/${roleId}`);
    return;
  }
  // 加载角色信息和历史消息
  loadRoleInfo();
  loadChatHistory();
  // 滚动到底部
  scrollToBottom();
})
// 处理滚动加载更多（监听顶部滚动）
const handleScroll = () => {
  const container = chatContainer.value;
  if (!container) return;

  // 滚动到顶部且有更多数据时加载上一页
  if (container.scrollTop <= 10 && hasMore.value && !isLoadingMore.value) {
    loadChatHistory(true);
  }
};
// 处理头像地址：优先用用户/角色头像，无则用默认图
const getAvatarUrl = (msg) => {
  if (msg.messageType === 'user') {
    return userAvatar.value || 'https://via.placeholder.com/40/eee/999?text=U'; // 用户默认头像
  }
  return roleInfo.value?.cover || 'https://via.placeholder.com/40/66b1ff/fff?text=AI'; // AI 默认头像
};
const loadRoleInfo = async () => {
  const res = await getRoleById({roleId})
  if (res.data.code === 0) {
    roleInfo.value = res.data.data || null
    console.log(res.data.data)
  }
}
// 修改加载历史消息方法（支持分页）
const loadChatHistory = async (isLoadMore = false) => {
  // 没有更多数据时停止请求
  if (isLoadMore && (!hasMore.value || isLoadingMore.value)) return;

  // 设置加载状态
  if (isLoadMore) {
    isLoadingMore.value = true;
  } else {
    isLoadingHistory.value = true;
  }

  try {
    const res = await getChatHistory({
      roleId: roleId,
      userId: loginUserStore.loginUser.id,
      current: isLoadMore ? currentPage.value + 1 : 1,
      pageSize: 10,
    });

    if (res.data.code === 0) {
      console.log(res.data.data)
      const pageData = res.data.data;
      totalCount.value = pageData.total || 0;
      console.log(pageData.records)

      if (isLoadMore) {
        // 加载更多：往前添加历史消息
        chatHistory.value = [...(pageData.records || []), ...chatHistory.value];
        currentPage.value++;
      } else {
        // 首次加载：直接替换
        chatHistory.value = pageData.records || [];
        currentPage.value = 1;
      }

      // 判断是否还有更多数据
      hasMore.value = (currentPage.value * 10) < totalCount.value;
    } else {
      message.error('历史消息加载失败');
    }
  } catch (e) {
    message.error('历史消息加载失败');
    console.error(e);
  } finally {
    isLoadingHistory.value = false;
    isLoadingMore.value = false;
  }
};
// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

// 切换录音状态
const toggleRecording = async () => {
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录');
    return;
  }

  if (isRecording.value) {
    stopRecording();
  } else {
    startRecording();
  }
};



// 滚动到最新消息
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
  });
};
// 修复音频URL格式：在域名.com后补充缺失的斜杠
const fixVoiceUrl = (url: string) => {
  if (!url) return '';
  // 匹配".com"后面没有斜杠的情况，例如"myqcloud.comvoice-user"
  const regex = /(\.com)(?!\/)/i;
  if (regex.test(url)) {
    return url.replace(regex, '$1/');  // 替换为".com/"
  }
  return url;  // 格式正确则直接返回
};
import {
  AudioTwoTone,
  StopTwoTone,
  CheckCircleTwoTone,
  CloseCircleTwoTone
} from '@ant-design/icons-vue';

// 录音核心状态管理
const recordingStatus = ref('idle'); // 状态：idle(未录音)/recording(录音中)/recorded(录音完成)
const recordingTime = ref(0); // 录音时长（秒）
const recordedVoiceUrl = ref(''); // 录音完成后的音频链接
const isRecordingDisabled = ref(false); // 是否禁用录音（如接口请求中）
let recordingTimer = null; // 录音时长计时器

// 1. 开始录音（需对接实际录音API）
const startRecording = () => {
  if (isRecordingDisabled.value) return;

  // 1.1 切换状态 + 重置时长
  recordingStatus.value = 'recording';
  recordingTime.value = 0;

  // 1.2 启动时长计时器（每秒+1，最大60秒）
  recordingTimer = setInterval(() => {
    if (recordingTime.value >= 60) {
      stopRecording(); // 超过60秒自动停止
    } else {
      recordingTime.value += 1;
    }
  }, 1000);

  // 1.3 对接实际录音逻辑（如H5 MediaRecorder/原生录音SDK）
  console.log('开始录音...');
  // 示例：调用录音API获取音频流，结束后赋值 recordedVoiceUrl
  // recordApi.start().then(url => { recordedVoiceUrl.value = url; });
};

// 2. 停止录音
const stopRecording = () => {
  // 2.1 清除计时器 + 切换状态
  clearInterval(recordingTimer);
  recordingStatus.value = 'recorded';

  // 2.2 对接实际停止录音逻辑
  console.log('停止录音，时长：', recordingTime.value, '秒');
  // 示例：recordApi.stop().then(url => { recordedVoiceUrl.value = url; });
};

// 3. 取消录音（重置状态）
const cancelRecording = () => {
  clearInterval(recordingTimer);
  recordingStatus.value = 'idle';
  recordedVoiceUrl.value = '';
  console.log('取消录音');
};

// 4. 发送录音（需对接接口）
const sendRecording = () => {
  if (!recordedVoiceUrl.value) return;

  // 4.1 禁用按钮防止重复发送
  isRecordingDisabled.value = true;

  // 4.2 对接发送录音接口
  console.log('发送录音，URL：', recordedVoiceUrl.value);
  // 示例：sendApi.sendVoice(recordedVoiceUrl.value).then(() => {
  //   recordingStatus.value = 'idle'; // 发送成功重置状态
  //   isRecordingDisabled.value = false;
  // });
};

// 格式化录音时长（00:00 格式）
const formatRecordingTime = (seconds) => {
  const min = Math.floor(seconds / 60).toString().padStart(2, '0');
  const sec = (seconds % 60).toString().padStart(2, '0');
  return `${min}:${sec}`;
};
</script>

<style scoped>
/* 新增加载更多样式 */
.loading-tip {
  text-align: center;
  padding: 10px;
  color: #666;
  font-size: 14px;
}
.role-chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

.role-header {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
  margin-bottom: 16px;
}

.role-info {
  margin-left: 16px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 20px;
}

.loading-tip, .empty-tip {
  text-align: center;
  padding: 20px;
  color: #666;
}


/* 提升选择器优先级，避免被其他样式覆盖 */
.chat-messages .message-item {
  display: flex;
  margin-bottom: 24px; /* 增大间距，让每个对话框更独立 */
  width: 100%; /* 占满父容器宽度 */
  min-height: 140px; /* 即使内容少，也给足高度空间 */
}

/* 对话框强制大尺寸（核心样式） */
.chat-messages .message-item .message-content {
  margin: 0 16px; /* 加大边距，避免贴边 */
  padding: 30px; /* 内部留白足够多，视觉上更"大" */
  border-radius: 16px; /* 圆角适中，保持对话框感 */
  width: 85%; /* 固定宽度（父容器的85%） */
  min-height: 140px; /* 强制最小高度，确保大框 */
  box-sizing: border-box; /* 保证padding不影响整体宽高 */
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1); /* 阴影加深，突出大框 */
}

/* 用户消息（靠右的蓝色框） */
.chat-messages .message-item.user-message .message-content {
  background-color: #1890ff;
  color: white;
  /* 确保靠右时也占满设定宽度 */
  margin-left: auto;
}

/* AI消息（靠左的白色框） */
.chat-messages .message-item.ai-message .message-content {
  background-color: #fff;
  border: 1px solid #eee; /* 加边框，增强白色框的存在感 */
}

/* 语音进度条强制固定长度 */
.chat-messages .message-item .message-content .audio-player {
  width: 200px; /* 固定进度条长度（可根据需求调整） */
  height: 30px; /* 进度条高度加大，更显眼 */
  margin-top: 15px; /* 与顶部保持距离 */
  /* 确保进度条在框内居中 */
  display: block;
}

/* 确保滚动容器有足够宽度供大框使用 */
.chat-messages {
  padding: 20px;
  width: 900px;
  box-sizing: border-box;
}

.user-message .message-content {
  background-color: #1890ff;
  color: white;
}

.audio-player {
  margin-top: 6px;
  width: 100%;
}

.message-time {
  display: block;
  font-size: 12px;
  color: #888;
  margin-top: 4px;
  text-align: right;
}

.user-message .message-time {
  color: #ddd;
}

.audio-control {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
}

.recording-tip {
  color: #f5222d;
  margin-top: 8px;
  font-size: 14px;
}
/* 1. 聊天容器：限制宽度 + 居中，适配不同屏幕 */
.chat-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;
  background-color: #f9fafb; /* 轻微背景色，区分页面其他区域 */
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 2. 单个消息项：Flex 布局，控制左右对齐 */
.message-item {
  display: flex;
  align-items: flex-start; /* 头像与气泡顶部对齐 */
  margin-bottom: 16px;
  gap: 8px; /* 头像与气泡间距 */
}

/* 3. 用户消息：居右 + 蓝色气泡 */
.user-message {
  flex-direction: row-reverse; /* 反转 Flex 顺序：气泡居左，头像居右 */
}

/* 4. AI 消息：居左 + 灰色气泡 */
.ai-message {
  flex-direction: row; /* 默认顺序：头像居左，气泡居右 */
}

/* 5. 头像样式：固定大小 + 圆角 */
.message-avatar {
  width: 40px;
  height: 40px;
  margin-top: 2px; /* 轻微下移，与气泡视觉对齐 */
  border: 1px solid #eee;
}

/* 6. 消息气泡：核心样式，区分用户/AI */
.message-bubble {
  max-width: 70%; /* 气泡最大宽度，避免过宽 */
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.5;
  position: relative;
}

/* 用户气泡：蓝色背景 + 右上角圆角减小（符合聊天习惯） */
.user-message .message-bubble {
  background-color: #1677ff; /* 蚂蚁蓝，与 Ant Design 风格统一 */
  color: #fff;
  border-top-right-radius: 8px; /* 右上角圆角小，模拟“发送方”气泡 */
}

/* AI 气泡：浅灰背景 + 左上角圆角减小 */
.ai-message .message-bubble {
  background-color: #ffffff; /* 白色背景，突出 AI 消息 */
  color: #333;
  border: 1px solid #eee;
  border-top-left-radius: 4px; /* 左上角圆角小，模拟“接收方”气泡 */
}

/* 7. 文本消息样式：避免换行异常 */
.message-text {
  margin: 0;
  word-break: break-all; /* 长文本换行 */
  white-space: pre-wrap; /* 保留换行符（如用户输入的回车） */
  font-size: 14px;
}

/* 8. 音频播放器样式：统一风格 */
.message-audio {
  width: 200px;
  margin-top: 8px;
  padding: 8px;
  border-radius: 8px;
  background-color: rgba(0, 0, 0, 0.03);
  outline: none;
}

/* 用户消息的音频播放器：适配蓝色背景 */
.user-message .message-audio {
  background-color: rgba(255, 255, 255, 0.2);
}

/* 9. 时间样式：右下角小字，弱化显示 */
.message-time {
  display: block;
  text-align: right;
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

/* 用户消息的时间：白色弱化 */
.user-message .message-time {
  color: rgba(255, 255, 255, 0.7);
}

/* 10. 响应式适配：小屏幕调整间距和宽度 */
@media (max-width: 480px) {
  .chat-container {
    padding: 8px;
    max-width: 100%;
  }

  .message-bubble {
    max-width: 80%; /* 小屏幕气泡宽度更大 */
  }

  .message-avatar {
    width: 36px;
    height: 36px;
  }
}
/* 底部录音控制栏：固定在底部，全屏宽度，带背景层 */
.audio-control-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 20px;
  background-color: #fff;
  border-top: 1px solid #f0f0f0;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.03);
  z-index: 100; /* 确保在其他元素上方 */
}

/* 控制区容器：居中对齐，限制宽度 */
.audio-control-wrapper {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

/* 1. 未录音状态样式 */
.control-idle {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

/* 录音按钮（未录音） */
.record-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;
}

.idle-btn {
  width: 64px;
  height: 64px;
  background-color: #f5f7fa;
}

.idle-btn:hover {
  background-color: #e6f7ff;
}

.icon-mic {
  font-size: 28px;
}

.btn-text {
  display: none; /* 移动端可根据需求显示 */
  margin-left: 8px;
  font-size: 14px;
  color: #1677ff;
}

/* 2. 录音中状态样式 */
.control-recording {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

/* 录音中动画容器 */
.recording-animation {
  position: relative;
}

/* 脉冲圆环动画 */
.pulse-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 76px;
  height: 76px;
  border: 4px solid rgba(22, 119, 255, 0.2);
  border-radius: 50%;
  animation: pulse 1.5s infinite ease-in-out;
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(0.9);
    opacity: 1;
  }
  70% {
    transform: translate(-50%, -50%) scale(1.1);
    opacity: 0.5;
  }
  100% {
    transform: translate(-50%, -50%) scale(0.9);
    opacity: 1;
  }
}

/* 录音中按钮 */
.recording-btn {
  width: 64px;
  height: 64px;
  background-color: #1677ff;
  z-index: 1; /* 盖在脉冲圆环上 */
}

.recording-btn:hover {
  background-color: #096dd9;
}

.icon-stop {
  font-size: 24px;
}

/* 录音时长文本 */
.recording-time {
  font-size: 14px;
  color: #1677ff;
  font-weight: 500;
}

/* 3. 录音完成状态样式 */
.control-recorded {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 录音完成时长 */
.recorded-time {
  font-size: 13px;
  color: #666;
  align-self: flex-start;
}

/* 操作按钮组 */
.recorded-btns {
  display: flex;
  gap: 16px;
  align-self: flex-end; /* 右对齐按钮 */
}

/* 取消/发送按钮 */
.operate-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 16px;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn {
  background-color: #f9f9f9;
  color: #ff4d4f;
}

.cancel-btn:hover {
  background-color: #fff2f0;
}

.send-btn {
  background-color: #f0f9ff;
  color: #52c41a;
}

.send-btn:hover {
  background-color: #e6f7ff;
}

.send-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

/* 控制区提示文本 */
.control-tip {
  font-size: 12px;
  color: #999;
  margin: 0;
}

/* 响应式适配：小屏幕优化 */
@media (max-width: 480px) {
  .audio-control-bar {
    padding: 10px 16px;
  }

  .idle-btn, .recording-btn {
    width: 56px;
    height: 56px;
  }

  .pulse-ring {
    width: 68px;
    height: 68px;
  }

  .icon-mic {
    font-size: 24px;
  }

  .recorded-btns {
    gap: 12px;
  }

  .operate-btn {
    padding: 5px 12px;
    font-size: 13px;
  }
}
</style>
