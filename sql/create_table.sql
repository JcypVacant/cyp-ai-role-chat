-- 创建库
create database if not exists cyp_ai_role_chat;

-- 切换库
use cyp_ai_role_chat;


-- 以下是建表语句
-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 角色表
create table role
(
    id           bigint auto_increment comment 'id' primary key,
    roleName     varchar(256)                       not null comment '角色名称（如哈利波特、苏格拉底）',
    cover        varchar(512)                       null comment '角色封面URL',
    description  text                               null comment '角色简介（背景、性格等）',
    systemPrompt text                               not null comment '角色系统提示词（定义角色行为、语气、知识范围）',
    category     varchar(64)                        null comment '角色分类（如文学、历史、影视、虚拟等）',
    ttsVoice     varchar(64)                        null comment '百度云TTS发音人（用于语音合成，如"zhitian"、"siqi"）',
    priority     int      default 0                 not null comment '角色优先级（用于排序，数值越高越靠前）',
    userId       bigint                             not null comment '创建者id（0表示系统预设角色）',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '最后编辑时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除（0-未删除，1-已删除）',
    INDEX idx_roleName (roleName),         -- 提升角色名称搜索性能
    INDEX idx_category (category),         -- 提升按分类筛选性能
    INDEX idx_userId (userId)              -- 提升查询用户创建角色的性能
) comment 'AI角色扮演信息表' collate = utf8mb4_unicode_ci;

-- 对话历史表（适配“多用户→单角色”聊天场景，优化索引）
create table chat_history
(
    id            bigint auto_increment comment '主键id' primary key,
    message       text                               not null comment '消息内容（文本/语音转文本等）',
    messageType   varchar(32)                        not null comment '消息来源类型：user/ai',
    messageFormat varchar(32) default 'text'         not null comment '消息格式：text/voice',
    voiceUrl      varchar(1024)                      null comment '语音消息的云端存储URL（如果消息格式为voice）',
    roleId        bigint                             not null comment '所属角色id（多用户→单角色的核心关联）',
    userId        bigint                             not null comment '创建用户id（区分同一角色下的不同用户）',
    createTime    datetime    default CURRENT_TIMESTAMP not null comment '创建时间（排序核心字段）',
    updateTime    datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint     default 0               not null comment '是否删除（0-未删，1-已删）',
    INDEX idx_roleId (roleId),                         -- 提升基于角色的查询性能
    INDEX idx_createTime (createTime),                 -- 提升基于时间的查询性能
    INDEX idx_roleId_createTime (roleId, createTime)   -- 游标查询核心索引
) comment '角色对话历史表（支持多用户与单角色聊天，适配文本/语音消息）' collate = utf8mb4_unicode_ci;

insert into role(roleName, systemPrompt, category, userId) values ('苏格拉底', '身份设定
你是古希腊哲学家苏格拉底，以“产婆术”（Socratic Method）著称。你不直接给出答案，而是通过不断的提问，引导对方自己思考，帮助他们发现真理。你机智、谦逊，强调逻辑推理与批判性思维。

对话风格

主要以提问为主，而不是直接陈述答案。

提问要层层深入，从表面到本质，引导对方思考。

保持耐心与尊重，避免直接批判，而是启发式地指出矛盾。

善用类比与日常实例来帮助对方理解抽象概念。

语言简洁清晰，富有哲理，但避免长篇大论。

核心原则

“我只知道我一无所知。”——始终保持谦逊。

通过提问让对方发现自我认知中的漏洞。

关注“定义”和“本质”，不断追问概念背后的根源。

鼓励批判性思维，而不是灌输知识。

示例语气

“你说幸福，那你认为幸福的本质是什么呢？”

“如果我们接受这个前提，那么是否也要接受这个结论呢？”

“这是否意味着你之前的观点和现在的说法有冲突呢？”

“你觉得正义是人人相同的，还是因人而异的？”', '历史与文化','1970299962884169729');