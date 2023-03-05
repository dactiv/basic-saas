/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80028
 Source Host           : localhost:3306
 Source Schema         : saas_workflow

 Target Server Type    : MySQL
 Target Server Version : 80028
 File Encoding         : 65001

 Date: 05/03/2023 10:43:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_apply
-- ----------------------------
DROP TABLE IF EXISTS `tb_apply`;
CREATE TABLE `tb_apply` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `user_id` int NOT NULL COMMENT '发起人 id',
  `username` varchar(32) NOT NULL COMMENT '发起人名称',
  `user_type` varchar(32) NOT NULL COMMENT '发起人类型',
  `apply_content` json NOT NULL COMMENT '申请内容',
  `approval_type` smallint NOT NULL COMMENT '审批类型',
  `approval_count` smallint NOT NULL COMMENT '审批数量',
  `form_id` int NOT NULL COMMENT '表单 id',
  `form_name` varchar(256) NOT NULL COMMENT '表单名称',
  `form_content` json NOT NULL COMMENT '表单信息',
  `form_type` smallint NOT NULL DEFAULT '10' COMMENT '表单类型',
  `status` smallint NOT NULL COMMENT '状态:10,新创建,20.审批中,30.已通过,40.不通过',
  `urgent_count` smallint DEFAULT NULL COMMENT '加急次数',
  `urging_time` datetime DEFAULT NULL COMMENT '加急时间',
  `completion_time` datetime DEFAULT NULL COMMENT '完成时间',
  `cancellation_time` datetime DEFAULT NULL COMMENT '撤销时间',
  `success_time` datetime DEFAULT NULL COMMENT '发送消息成功实践',
  `exception` varchar(256) DEFAULT NULL COMMENT '发送消息异常信息',
  `execute_status` smallint DEFAULT NULL COMMENT '发送消息执行状态',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `ix_form_id_form_type` (`form_id`,`form_type`)
) ENGINE=InnoDB COMMENT='流程申请表';

-- ----------------------------
-- Table structure for tb_apply_approval
-- ----------------------------
DROP TABLE IF EXISTS `tb_apply_approval`;
CREATE TABLE `tb_apply_approval` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `user_id` int NOT NULL COMMENT '审核人 id',
  `username` varchar(32) NOT NULL COMMENT '审核人名称',
  `user_type` varchar(32) NOT NULL COMMENT '发起人类型',
  `apply_id` int NOT NULL COMMENT '申请 id',
  `result` smallint DEFAULT NULL COMMENT '审核结果:10.通过,20.不通过',
  `status` smallint NOT NULL COMMENT '状态:10.等待审批,20.执行审批,30.审批完成,40.无需审核',
  `sort` smallint NOT NULL COMMENT '顺序值',
  `operation_time` datetime DEFAULT NULL COMMENT '操作时间',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='申请审批表';

-- ----------------------------
-- Table structure for tb_apply_copy
-- ----------------------------
DROP TABLE IF EXISTS `tb_apply_copy`;
CREATE TABLE `tb_apply_copy` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `user_id` int NOT NULL COMMENT '抄送人 id',
  `username` varchar(32) NOT NULL COMMENT '抄送人名称',
  `user_type` varchar(32) NOT NULL COMMENT '发起人类型',
  `apply_id` int NOT NULL COMMENT '申请 id',
  `status` smallint NOT NULL COMMENT '状态:10.等待抄送,20.已抄送,30.无需抄送',
  `sort` smallint NOT NULL COMMENT '顺序值',
  `copy_time` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='申请抄送表';

-- ----------------------------
-- Table structure for tb_form
-- ----------------------------
DROP TABLE IF EXISTS `tb_form`;
CREATE TABLE `tb_form` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `apply_department` json DEFAULT NULL COMMENT '可以发起审批的部门',
  `approval_type` smallint NOT NULL COMMENT '审批方式',
  `status` smallint NOT NULL COMMENT '状态:10.新创建,20.已发布,30.已作废',
  `type` smallint NOT NULL DEFAULT '20' COMMENT '表单类型:10.系统表单，20.自定义表单',
  `group_id` int NOT NULL COMMENT '类型分组',
  `group_name` varchar(32) NOT NULL COMMENT '类型分组名称',
  `design` json NOT NULL COMMENT '表单设计内容',
  `icon` varchar(128) DEFAULT NULL COMMENT '图标',
  `schedule_name` varchar(32) DEFAULT NULL COMMENT '日程名称',
  `participant` tinyint NOT NULL COMMENT '是否存在参与者',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='流程表单表';

-- ----------------------------
-- Table structure for tb_form_participant
-- ----------------------------
DROP TABLE IF EXISTS `tb_form_participant`;
CREATE TABLE `tb_form_participant` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `user_id` int NOT NULL COMMENT '用户 id',
  `username` varchar(32) NOT NULL COMMENT '用户名',
  `user_type` varchar(32) NOT NULL COMMENT '用户类型',
  `type` smallint NOT NULL COMMENT '参与者类型:10.审批人,20.抄送人',
  `form_id` int NOT NULL COMMENT '流程表单 id',
  `sort` smallint NOT NULL COMMENT '顺序值',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='流程表单参与者表';

-- ----------------------------
-- Table structure for tb_group
-- ----------------------------
DROP TABLE IF EXISTS `tb_group`;
CREATE TABLE `tb_group` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `parent_id` int DEFAULT NULL COMMENT '父类 id',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='流程组表';

-- ----------------------------
-- Table structure for tb_kit
-- ----------------------------
DROP TABLE IF EXISTS `tb_kit`;
CREATE TABLE `tb_kit` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `title` varchar(32) NOT NULL COMMENT '名称',
  `layout` json NOT NULL COMMENT '布局内容',
  `category_id` int NOT NULL COMMENT '类别 id',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='套件';

-- ----------------------------
-- Table structure for tb_kit_category
-- ----------------------------
DROP TABLE IF EXISTS `tb_kit_category`;
CREATE TABLE `tb_kit_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `title` varchar(32) NOT NULL COMMENT '名称',
  `icon` varchar(64) NOT NULL COMMENT 'icon 图标名称',
  `remark` varchar(256) DEFAULT NULL COMMENT '布局内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='套件类别';

-- ----------------------------
-- Table structure for tb_schedule
-- ----------------------------
DROP TABLE IF EXISTS `tb_schedule`;
CREATE TABLE `tb_schedule` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `user_id` int NOT NULL COMMENT '用户 id',
  `username` varchar(32) NOT NULL COMMENT '用户名称',
  `user_type` varchar(32) NOT NULL COMMENT '用户类型',
  `name` varchar(32) NOT NULL COMMENT '日程名称',
  `content` varchar(512) NOT NULL COMMENT '日程内容',
  `remark` text COMMENT '备注',
  `meta` json DEFAULT NULL COMMENT '元数据信息',
  `status` tinyint NOT NULL COMMENT '状态:10.新创建,15.已更新, 20.已发布',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='日程表';

-- ----------------------------
-- Table structure for tb_schedule_participant
-- ----------------------------
DROP TABLE IF EXISTS `tb_schedule_participant`;
CREATE TABLE `tb_schedule_participant` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `user_id` int NOT NULL COMMENT '用户 id',
  `username` varchar(32) NOT NULL COMMENT '用户名称',
  `user_type` varchar(32) NOT NULL COMMENT '用户类型',
  `status` tinyint NOT NULL COMMENT '状态:10.等待确认,20.已确认.30.拒绝参加',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `sign_In_time` datetime DEFAULT NULL COMMENT '签到时间',
  `schedule_id` int NOT NULL COMMENT '日程 id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='日程参与者表';

-- ----------------------------
-- Table structure for tb_user_apply_history
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_apply_history`;
CREATE TABLE `tb_user_apply_history` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `user_id` int NOT NULL COMMENT '用户 id',
  `user_type` varchar(32) NOT NULL COMMENT '用户类型',
  `username` varchar(255) NOT NULL COMMENT '用户名称',
  `form_id` int DEFAULT NULL COMMENT '表单 id',
  `form_type` smallint NOT NULL COMMENT '表单类型',
  `form_name` varchar(64) NOT NULL COMMENT '表单名称',
  `participant` json NOT NULL COMMENT '参与者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `ux_form_id_type_user_id` (`form_type`,`user_id`,`form_id`) USING BTREE
) ENGINE=InnoDB COMMENT='用户提交申请审核人历史记录';

-- ----------------------------
-- Table structure for tb_work
-- ----------------------------
DROP TABLE IF EXISTS `tb_work`;
CREATE TABLE `tb_work` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `user_id` int NOT NULL COMMENT '用户 id',
  `username` varchar(32) NOT NULL COMMENT '用户名称',
  `user_type` varchar(32) NOT NULL COMMENT '用户类型',
  `name` varchar(256) NOT NULL COMMENT '工作名称',
  `type` smallint NOT NULL COMMENT '类型:10.我发起的,20.我的经办,30.我收到的',
  `status` smallint NOT NULL COMMENT '状态:10.待处理, 20.已处理',
  `apply_id` int NOT NULL COMMENT '申请 id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='工作内容表';

SET FOREIGN_KEY_CHECKS = 1;
