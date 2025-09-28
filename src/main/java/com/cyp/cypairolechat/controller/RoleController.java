package com.cyp.cypairolechat.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyp.cypairolechat.common.BaseResponse;
import com.cyp.cypairolechat.common.ResultUtils;
import com.cyp.cypairolechat.exception.ErrorCode;
import com.cyp.cypairolechat.exception.ThrowUtils;
import com.cyp.cypairolechat.model.dto.role.RoleQueryRequest;
import com.cyp.cypairolechat.model.entity.Role;
import com.cyp.cypairolechat.model.vo.RoleVO;
import com.cyp.cypairolechat.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/role")
public class RoleController {
    @Resource
    private RoleService roleService;


    /**
     * 分页获取角色列表
     *
     * @param roleQueryRequest 查询请求
     * @return 角色分页列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<RoleVO>> listRoleVOByPage(@RequestBody RoleQueryRequest roleQueryRequest) {
        ThrowUtils.throwIf(roleQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageSize = roleQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个角色");

        long pageNum = roleQueryRequest.getCurrent();
        QueryWrapper<Role> queryWrapper = roleService.getQueryWrapper(roleQueryRequest);

        // 执行分页查询
        Page<Role> rolePage = roleService.page(Page.of(pageNum, pageSize), queryWrapper);

        // 封装 VO
        Page<RoleVO> roleVOPage = roleService.getRoleVOPage(rolePage);
        return ResultUtils.success(roleVOPage);
    }
    @GetMapping("/get")
    public BaseResponse<Role> getRoleById(@RequestParam("roleId") Long roleId) {
        Role role = roleService.getById(roleId);
        return ResultUtils.success(role);
    }

}
