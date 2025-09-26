package com.cyp.cypairolechat.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色返回对象
 */
@Data
public class RoleVO implements Serializable {

    private Long id;

    private String roleName;

    private String cover;

    private String description;

    private String category;

    private Integer priority;

    private Long userId;

    private static final long serialVersionUID = 1L;
}
