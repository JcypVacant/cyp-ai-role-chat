// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /chat/voice */
export async function chatByVoice(body: {}, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/chat/voice', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
