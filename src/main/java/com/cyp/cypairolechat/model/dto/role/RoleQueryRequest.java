package com.cyp.cypairolechat.model.dto.role;


import com.cyp.cypairolechat.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 角色查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryRequest extends PageRequest implements Serializable {

    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色分类
     */
    private String category;

    /**
     * 创建者 id
     */
    private Long userId;
    /**
     * 角色简介
     */
    private String description;

    private static final long serialVersionUID = 1L;
}
