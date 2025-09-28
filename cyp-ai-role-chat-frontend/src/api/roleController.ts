// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /role/get */
export async function getRoleById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getRoleByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseRole>('/role/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /role/list/page/vo */
export async function listRoleVoByPage(
  body: API.RoleQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageRoleVO>('/role/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
