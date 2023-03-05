/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80028
 Source Host           : localhost:3306
 Source Schema         : saas_authentication

 Target Server Type    : MySQL
 Target Server Version : 80028
 File Encoding         : 65001

 Date: 05/03/2023 10:45:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_authentication_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_authentication_info`;
CREATE TABLE `tb_authentication_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `user_id` int NOT NULL COMMENT '用户 id',
  `username` varchar(32) NOT NULL COMMENT '登陆账号',
  `meta` json NOT NULL COMMENT '账号元数据',
  `user_type` varchar(32) NOT NULL COMMENT '用户类型',
  `ip_region` json DEFAULT NULL COMMENT 'ip 区域信息',
  `device` json NOT NULL COMMENT '设备信息',
  `sync_status` tinyint NOT NULL DEFAULT '0' COMMENT '同步 es 状态：0.处理中，1.成功，99.失败',
  `retry_count` tinyint NOT NULL DEFAULT '0' COMMENT '重试次数',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='认证信息表';

-- ----------------------------
-- Records of tb_authentication_info
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for tb_console_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_console_user`;
CREATE TABLE `tb_console_user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `password` char(64) NOT NULL COMMENT '密码',
  `status` tinyint NOT NULL COMMENT '状态:1.启用、2.禁用、3.锁定',
  `username` varchar(32) NOT NULL COMMENT '登录帐号',
  `gender` tinyint NOT NULL COMMENT '性别:10.男,20.女',
  `real_name` varchar(16) NOT NULL COMMENT '真实姓名',
  `phone_number` varchar(64) DEFAULT NULL COMMENT '电话号码',
  `groups_info` json DEFAULT NULL COMMENT '组信息',
  `resource_map` json DEFAULT NULL COMMENT '资源信息',
  `departments_info` json DEFAULT NULL COMMENT '部门信息',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `ux_username` (`username`) USING BTREE,
  UNIQUE KEY `ux_email` (`email`) USING BTREE
) ENGINE=InnoDB COMMENT='后台用户表';

-- ----------------------------
-- Records of tb_console_user
-- ----------------------------
BEGIN;
INSERT INTO `tb_console_user` (`id`, `creation_time`, `version`, `email`, `password`, `status`, `username`, `gender`, `real_name`, `phone_number`, `groups_info`, `resource_map`, `departments_info`, `remark`) VALUES (1, '2021-08-18 09:40:46.953', 7, 'admin@domian.com', '$2a$10$U2787VFuFP9NMyxwdsP1bOmtvofTgwU5nLcdV7Gj3ZyhdiZO.T8mG', 1, 'admin', 10, '超级管理员', NULL, '[{\"id\": 1, \"name\": \"超级管理员\", \"authority\": \"ADMIN\"}]', '{}', NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_department
-- ----------------------------
DROP TABLE IF EXISTS `tb_department`;
CREATE TABLE `tb_department` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `type` tinyint NOT NULL DEFAULT '20' COMMENT '类型:10.学生,20.老师',
  `parent_id` int DEFAULT NULL COMMENT '父类 ID',
  `count` int NOT NULL COMMENT '人员总数',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='部门表';

-- ----------------------------
-- Records of tb_department
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for tb_group
-- ----------------------------
DROP TABLE IF EXISTS `tb_group`;
CREATE TABLE `tb_group` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `authority` varchar(64) DEFAULT NULL COMMENT 'spring security role 的 authority 值',
  `sources` json NOT NULL COMMENT '来源',
  `parent_id` int DEFAULT NULL COMMENT '父类 id',
  `removable` tinyint NOT NULL COMMENT '是否可删除:0.否、1.是',
  `modifiable` tinyint NOT NULL COMMENT '是否可修改:0.否、1.是',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态:0.禁用、1.启用',
  `resource_map` json DEFAULT NULL COMMENT '资源信息',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `ux_name` (`name`) USING BTREE,
  UNIQUE KEY `ux_authority` (`authority`)
) ENGINE=InnoDB COMMENT='用户组表';

-- ----------------------------
-- Records of tb_group
-- ----------------------------
BEGIN;
INSERT INTO `tb_group` (`id`, `creation_time`, `version`, `name`, `authority`, `sources`, `parent_id`, `removable`, `modifiable`, `status`, `resource_map`, `remark`) VALUES (1, '2022-03-31 13:50:37.408', 10673, '超级管理员', 'ADMIN', '[\"CONSOLE\", \"SYSTEM\"]', NULL, 0, 1, 1, '{\"admin\": [\"c105bc3c46a9c400b21ebd70c393a870\", \"93eb7e3234bb85bc8fc4c56f66620fcb\", \"40eccbad195a43cab476f8d7f5ee0977\", \"d2fd8915c18760aaf022922a425eb742\", \"d0358a7a6ef2191998f35e7277255d14\", \"ea969e5db9a22eea455a63dd98261221\", \"c0f81fa364c6baac203ac689492b2ec8\", \"d34a68585f584fc6bc128788ea04f893\", \"4692760165792776a9365879cda83425\", \"941ee2f9170a88bc933126a650858547\", \"9a923847a8d284dd4c50f46776983846\", \"4c4f9d9e22fd2a23a1ab6dd5ffe3ac1b\", \"1a8a33477e4ff395f9e27f2807c8b08a\", \"038956f0265efa901a0d3b0305e05095\", \"ced77b42c6ebb29815ccbbccedd71038\", \"bc8e3d15f5afa39ebf8682e996fc5fb8\", \"02a927d203b3da316ec8a763cab49e06\", \"e4c579623398ef4814d7f696744ec18b\", \"a81032fe729c9b74ede17a750df6b116\", \"3cc6339a1c145f6ff012d74be0a06287\", \"3cab94fcaefed15e15717079dbc0ca68\", \"b6af4bfcc76d86024410234767696b1c\", \"427ba0906b2954d32fee906e45a86a3a\", \"276159f818535e1f08a76a70b491f8df\", \"cac819e8ea4735dabbf1ca0e5c5016ca\", \"55aabb85603954e753797714023cad11\", \"a6108a873ad6ea551a58afd82d883f79\", \"b9b4b28c345ab4de4477577e7dbc1e0c\", \"d07583c590dd7870748783aff5d62a1b\", \"12bc91a7099b5f0ba794695ebd61bdc2\", \"65651270085b26ed29dea25a6e9c8c63\", \"b3f197b3ca972a3e4ae9a8d911833f97\", \"34c0661794f962048bc72d788dcd9290\", \"9fd4db432595ee2a7f933ad24aaf2097\", \"39caebc3de90fae0fd0ee7afedaec399\", \"9256d4381b4a7a871ded502395c54d80\", \"a9d5284cad9c7cc76882d492b6151ae2\", \"29386639b8b7fc829b4ead3f90f7e68d\", \"3c24dceb059c573c0b3cfcadf4782a66\", \"393b8df599c03776423bf52bedfb99b3\", \"084daf0ba9d3b3ab01449c2722edd1bf\", \"20d5581012fadb5b71a9a47d81f8ec8d\", \"63b66ad095a890ef151f4395795f2ce6\", \"8211f16d94147d124b89f750f2c81ab1\", \"eac7bf0e2dfa5c1c55c38d2021764bce\"], \"basic\": [\"7df98a39176a3d094b4ae187790125da\", \"d93adebd04a3980dfaceff5f7412f028\", \"da0e40ce274e2250bded8a41d5237d34\", \"1706e77392ba48b26ef1e865ae340b29\", \"be33885e2da5a47c3e3dc2c3a66fd190\", \"bce4dcc356dbd029f1328d0e693c1116\", \"cc7460d4d2f50296f0d5f328db7d3bf1\", \"b29aafaf189d7bd021c85f7a0bd4edd4\", \"50fd8392bce0c334ee12c239642e1c6c\", \"384a6f3e9fdcd485635cd611f226ed40\", \"d778c462be6f5058674cea84ddc0499d\", \"d75af774904c49629e7c6c9ec92978f1\", \"df3d7027eb7dc3a8353aa10c8467baf3\", \"d07461d47adaf85878be11e6f0a8d5c0\", \"58c402d490c81d3ddf5fa8f5540aa157\", \"6061117c9eee5f5fbafa5306f218c51e\", \"0df46be4ae42c241edea1b984efb285a\", \"0a2d3195f91596ff6f134261157c0c50\", \"ca8bf373e2049ef490dd1bb178b50123\", \"9d41f663b9e1da350f62b5de6890255b\", \"8d6a2669bb1b36c66b7380d5d6277d29\", \"e0e99e49090d8945ddf8b0a54ea440e5\", \"6f98119afcaac85ac3a84509e5fbfd83\", \"03e5575677e020d0a837e985321cb491\", \"74f43c07fcca13eb92df6cfadadcff06\", \"e144ca86d791e95d78a702ceea86b685\", \"8c427d8cc21ba09955da2f27c07d71e6\", \"c7d7cb54c20069fc6d90b28cd7703257\", \"3aa32eca3cc472449c981082e3757de3\", \"9219c1bc53874ad353e2d481e9a6f386\", \"fcd740d7d2f752beca09a34f40096b21\", \"020df2f306b2661415d4bbf9051a8f05\", \"0d51e2f3bd61fa803bc7c423e89abeec\", \"aa1f236cf415024cd8dc40f8f7e9dd85\", \"4e98554d703160e62844986d605888c2\", \"97999aafef09c014539976ae67110a32\", \"90dce1e6f8fba58d8e4c6b0107a75f13\", \"e167dce8b074dd8f5bb5bb58efe27626\", \"eba589223728ddceedd1ab9179c00863\", \"e6113b36f59ff0d3bb6ed95591856bf9\", \"7d9d47185757a089b35704be80fab78c\", \"92f6265e352275b8c114991913ef2a2d\", \"c2610afd9ecabddb8ce4fc1ed966d21e\", \"7e6de2b66877ca77570525dd976cafa0\", \"14ddb02d41d319b0b10061a07df6617b\", \"72a721ba1c55c4778e69f36b4a24ba38\", \"e2284de7e313d16848db6f6b6580c608\", \"4f02abd4c8cf5f15bb251706d7cf4a9e\", \"5de6fac7e7acb3b6db0d9d9db50bca0c\", \"4e3593ab711586dcde95c8f05ba806b6\", \"4f65e1b45a394836e8562ebc9723d28b\", \"2456627c0c35a82283ad7b32d93e5e37\", \"872275de451a2b4ef7b2bb5d3921ac92\", \"ae63d830b0be8f945ebda96f7c65215f\", \"dfa7c122da41cf24ffce0321caaacfae\", \"a47db51ac265c916be0b3bf2eabcca3f\", \"3afe55eab41668f2c843c3d152c113b9\", \"a14520c90f57c471854157bc0658410a\", \"39c8ba80912f0467f1e5632104faea4d\", \"b1f4b5c981fc0777e17dffceb355d15f\", \"ef99462ff8bf84e9a76ade943cc692af\", \"2803af0d6368ce20051660cff76f4acf\", \"f014a01b648f0fa97f5bbbab2994436b\", \"7d5c538115665e8b5521f13a1aafeb9a\", \"d8ada9cdef4702c29744849675e1dc6a\", \"8f603d6e15ea8cee043e75de9a878448\", \"25df588c8561a5ad02be9f61791c7726\", \"5b88ce0b36ba71bf5780d9e9368e82dc\", \"2757910f01e17e257eeaaf5fb6fc7127\", \"280436b87beeb8bc4076dbcc5445f067\", \"83e3222dbb2a469d55b6b574622fb8d6\", \"299f52b5f260fa5801e720c7c06e4d38\", \"e1a475f19e54101e58f43808496a4994\", \"af4c275440eaba078576cb00c7f6301d\", \"eddc92d9d719c12728bce6412549db32\", \"bacf94d00921b5e04e05234ed6bb083b\", \"b611034e349d46c8624a9a066b61597e\", \"fea98e2f7dc40cc5e9e90fa750a5444b\", \"9cb97eed9bd7b4e03bc106b574c77a2c\", \"c098acbd9e017fc3d3b27414cbb56778\", \"8ca6422ae7b21f4426958567083cc837\", \"741b8c26740aa845ba7f81084dc8a253\", \"94da77a33b34afb137578bad6245b756\", \"8f5a1374fd4101af4d8f6637e01697c5\", \"18e558fd537521c0e4808eb6d7dd5498\", \"f6b916bce39b35fd299945c4f97c9303\", \"b25ffe5c2a0f48b3b158ab859f7fb678\", \"00cecfc082528ce04f320d2d6c052ba0\", \"32c33c88a1157bc4b7086192abaedabb\", \"fc6e033388afaca2e47f5ed364fb758d\", \"6ba4bc91d8f36fcb769f9ce333325b93\", \"0c093128bc6ab736db0bdba147d7c770\", \"9a7c1f99e88fce4b7083dbed6e5b2325\", \"4d4c6cc319333e613e2c7e88ab4d64c8\", \"6590e647824260b36efab094cb67983b\", \"2e08c733072a050d71c5e7419bcda040\", \"307814645981427863f8f3efd2d39bc8\", \"22403d5418cb835d0aa849d27129f553\", \"3d882357697866304fd281e350f5ad50\", \"0519db18128d90fe0f3ca35dcccf139f\", \"5d91e0a328a1476f18492d15891ea0e6\", \"1d98052b294da5863117ae456123ef6f\", \"460d2204eff63a6ca83296bf4545a572\", \"670acd25fcb7f4c4cbbc4d45d98048ee\", \"c704a7838b9cc2bd48b3cd68eb148c21\", \"27cc34b1d62ef4354fc0a76f07814725\", \"7c20803450ba10a8c990fa4e692c7d34\", \"86b8c68c470d2418be11852ff084e9b5\", \"eacd6da0bb25ef79c5af73565e3f667d\", \"10004810333e62b2cc6c2cb7e9df37ed\", \"36d7397498433761a35aa2f1196e767b\", \"61c810f5c1b558467b501778f0c52b03\", \"8feb3138d610a8662025ef4524a23a05\", \"8f0d21a7500b72c0aca4254ba77b3d21\", \"9a481227d0198a7191d1c0d0fec5f43a\", \"137669ab9c421a64a16fd3532e8ed1e6\", \"0bbc57d42f15181e64653e1e98185f78\", \"c21a79615e5486df17091c7faf74cf0f\", \"469277a6853974808b911c965b5a063a\", \"bfb5709f77d4f9348ef1665636f0ea47\", \"409cb3a6685c030eb3fa8f4f7fdf9bd6\", \"cddf252a25d4a5a6c18dc4d48178b2cb\", \"3c1c3cc89c1cdad347644c18611899c0\", \"9ee54fc5d7859d93c1bc140def0ee2e2\", \"894d81a8843230d697b3d23a10a757d5\", \"a8004d32d135c1d3c56c6304c82bd130\", \"e329c93d8944d169d3aeb3c17a3d4c17\", \"975eb72ea29c5ee795f848aee6f8de0b\", \"e25629955c957f28080f01ef4918dabf\", \"bd48d14a006a67aa94d4af6a46fb61cc\", \"4ce2772972600ec938b8c14f34d38078\", \"a96f391fbf69b3b31728adc4d3def3da\", \"696c8e6e34965a5b81791a615a88f3ef\", \"340407d40a358207c28b9af4aa60ab6c\", \"c2831140b3dac0327a13b99ed4942154\", \"27977152a3e6668fe4d7389989659695\", \"9c5065752bad5be339ee161f8437f0ed\", \"42d495816e8b3a855133e0e064f2b76e\", \"96000c713330fca073b65ee4de6d4b9e\", \"c98b53da22a03fd59446e32ad82f2905\", \"cd97cdc0cec8b080430270efc882b46e\", \"1395b2edae8b67134db060fbbcabde97\", \"092ecbaacb7ea0a5ac6ec5cb62081bb8\", \"491e2d1f0b45216bd3d0964796d5737a\", \"064cacb6b7a04efd59889b5970e6e8d7\", \"dddeac9d366ad296a849d4dbab069937\", \"38f4115a93b5891f6cc043cdc9cca8c5\", \"39f394baeaad027db8e59984b917454c\", \"f3b8d5362001edb4f5e95dba93e6f0c4\", \"ec0d0057e6cf7e5ffd2b8d9f8e36a186\", \"07ac083566476f3ea067380a7efc82a7\", \"892a7cdf3df248071ab502af13cae6bf\", \"d4ffe196864746607ce7e56f9168ebb2\", \"990cd159c91adfd8de75e0a749744b1a\", \"47442c8561b96ec7e6ef4b78b43abce6\", \"df6d4d1db90ac8a0aa82bd27dc0873cc\", \"5c6987f12800bd4d6a6f97ef77049d30\", \"016c826fa1025d5de240597956fe7db3\", \"d71f84ebaf6ef9aa4be6e39ada8cdcf8\", \"195053836cc9041dca163c9c39588b70\", \"82921b51fbf30b7a5b17d51d534b7ce9\", \"65545e4de4c0ce1136f6836c0d890f6c\", \"bd49961d61cf4bb010ad45810c65aedf\", \"5dceee4e0b87b54609a11878092f0746\", \"64c10ab608d09bfb486ebcb5d1210cbc\", \"98401198730d354127b9760e2ede43de\", \"469ad0b29ad4b3aa688736143b4aa6ba\", \"8f1b851244f1de83b796fc8ed3e63c4c\", \"3e5fa6f111c0eba94129730b67f7ac10\", \"72ec7e17e3a1b56ad6717a8e7bbb45b1\", \"6f323b3955cedf936e28070f221d4930\", \"037d944f700271f484bb0da2726755c5\", \"82cc9cd4931922d26364108c650a31c0\", \"794b69566f7e73cc9309e97a354bf189\", \"250a9de919dcfaec1e0ed7d533b2d26c\", \"677d615ecac879c6643eacd502643606\", \"648536caac57282120b6e0f069453573\", \"03dd2596854a5e74165cbdb1a0abc614\"], \"message\": [\"5e904adafb383f945d0e86d6b3f551aa\", \"9060b44ee1e7e3d71fb80c2db56fccb5\", \"f0a5c80dff12cbd4e8864fea35a3c854\", \"8c953d1f8ee41edc140594358332a545\", \"5563aaeb2665608d55f1f8cbd2740451\", \"b0435a08e47bf751c782e5b4756f9d90\", \"112e240da6d6aa894cf411f97220990b\", \"496872bcd3fe6a77daa91e05502c7671\", \"3eff5c4b9f95ba7b468796d4ec6cbd2a\", \"8cb3247123f76a03d47ffe98255a9ac8\", \"d1bf9c339f72807628de3147f00d4acf\", \"d15cd6da0ac4f0d1402c1053e9da7db1\", \"e70851d9e35b5f9e0381fa647ba6cd07\", \"471430b26a9e1da99f49552bddf5f359\", \"561dea664737fd65a59a23dc4badf1b1\", \"810642b6cc4f13cee0f15bc09d268a7a\", \"ae3e8aae452a3b290a2289b0bddd2a35\", \"83c87301af83867f1e9b98ddd1c93d4d\", \"713aa7c84998f28038094fd39fab62dd\", \"79094ed813da72fc31f7f54988431d83\", \"0f2965615ca63a7133d2ae2341136e1d\", \"613508a8cc5f85890cc7b1f538f136a8\", \"e480caad8c17a32baeb1fc676931e2c1\", \"0e140523951fc704de80d2e381560b37\", \"fa668fcbdb1795561b95bb30bf8742ec\", \"5da971ae3356ba90c38d912c330f45a7\", \"ffbd0baccb2bdbc4e43b2414f962b76a\", \"c66f0faf10d6753775a8af02d05bc350\", \"f8fffe88b80eca14fd6a2e8db5128833\", \"4df06148bb5937e0efa3e96f2307bb17\", \"8e7cb458156d64366a32ca5f4efa1d56\", \"b6270768cfb89564188411bff214f0fa\", \"1da73e2d79bf270a71f22380166e83fc\", \"fe2c86d8f1de500938007bb946d485e8\", \"5afc2ac5b6ec2904b095035d16539b69\", \"ee23c2ecc0f05d367be75bf655abae5f\"], \"workflow\": [\"f12101456a2f397027378bba32f04625\", \"6849682ed2debb0b25979487b1149131\", \"15b7db5230ea1b6a3b2a0dcdd814648a\", \"c953343b7cf99496aa8fd99ba2abe526\", \"bac5a5930e3da5a358754e7ead1770b6\", \"d5b20bb3df3ac86ebbdaed9848aedac6\", \"fb4f32d4eb2dbe373f5482e7322319d8\", \"15a614bed36304d80db40715fd18cc71\", \"7b9b161a307383c32519754d17f7f087\", \"2337b052ea545785113e0e21cc9c4f9f\", \"a94ebfd33d77f28bbe006e70c9b6593d\", \"603ea17f2ed33eea946d38c9b46f265d\", \"873ccb9818af8e35ecdc4d0d000fd207\", \"720abbac9637da382b158fc079336093\", \"0c8cc032675683d8a8443b95cee77c0c\", \"e46da722219584688bbf271fb9a1b896\", \"09803213c80c18ba28e0f9bd5da1fa8a\", \"9bfd6a1cc9ebccbd5ddccd745688b7ca\", \"ebdca2362da86e8efecc7b21ffcaba1d\", \"1272dedd2c8e361e823ec2062e9e2372\", \"8f0a1c77f2f73606606728097f3dfe54\", \"e4776972c03486892abcc33e3a633e70\", \"8a1dd818eb68fcea8f2e8684fcde915d\", \"3f7f9c50f3a44e6a2bdfd8dac48f2b2d\", \"a7c3311efe90b3db3bce3b10e1c7665e\", \"7d18287537211887b8e380bf690b0aa4\", \"5d984b52ad69410aa5f23a7e2b2b7f42\", \"765b04484238a92496607cf9490e4c75\", \"67cd316cc2174c4e56fbc9d75fbd2867\", \"832f2ec374e6ed813745dac105b18a2d\", \"5771646e5cad14d866b310dd58cf9275\", \"4a2627d863bea710efbb78bd176d6df8\", \"ed9bee9ae71f549608097c5372453391\", \"298af9c78272ac2e292c11143502052f\", \"2693b0d244cdf260c4936bc7ce28ea2f\", \"7d09ee17e9be14836dd4f71a68f36ab5\", \"6b54d073dd95228fcee381d6414b06c3\", \"8f5912d3a7cff5d8c7c51a1ab0afe268\", \"c26f01ea18a53f6a23aedc71692a0484\", \"8bfc7109be95fc75074990209b392efe\", \"c676c28efe885d8760be676c66dec6d9\", \"667a9c2512a2d02a7301452cb1e23660\", \"3bf901f0a35185dcdec5b12b92feaf67\"], \"resources\": [\"73f53e0b5c830f7d06d2f98968eceffd\", \"aa2a5663b7ef2026033c8cc2c39f5550\", \"79f1f4e74136fef9808ee29e5a0a76f7\", \"36afc0ecbae235275d31780478aa39d0\", \"a1a7865c61315940f62b0c2750c668fc\", \"17d21b32cfd415030b1d2ca2dbbe4214\", \"717128a21dd837cc83c45ddaa3761c14\", \"67f3ec2e5cfeb5fa881993351c41b21f\", \"5e71130bf8573fed311592dcfc4288fe\", \"fc9834df5e378c3994fbaa1342bd21e1\", \"e69b7a19d279af89912c1a3abf29d215\", \"093c7d6c08109933a8d60435241450fd\", \"487429c75cb4a9df7cf9bd788ca99a8d\", \"22156dcc8dd05e91941e6e349d40844e\", \"13e3b10116af344d64be2fe79fe47f76\", \"679ea52872badbbbaf30c70fd6f0b106\", \"729fa1e5546a8ff72ac325a9b6ddddef\", \"f90636c2dd6bbd6eb2a38e20d36f5b34\", \"1185a3cd887f48f78ef37364b3146484\", \"213383baa822defd621ac9c2c4bc3822\", \"cf179af0f580542d7c41ae289f6db75b\", \"45bdfd91af0e00847bac6171dda24bdf\", \"13dc0c1c7e0f2fc833a39393629ed824\", \"b80b7cad13804a067f369a6d1647d493\", \"63b6c8ee142373646d70d6ebf04ade29\", \"7d2915778c7a6307399f35c3376f45fa\", \"e7f41606d2907d2471ec51522b4ab49b\", \"333d303d6ed6802c7032e7d9d5e971d4\", \"041d934729bca095da821c59fcd45a4a\", \"d62953833a6bb48fcce0bdc112f876ad\", \"39f8567fb90b8eea58258b75f607410c\", \"b4bc3dd8ff21ab2200264fb3c7e1acfc\", \"b7303fcfb7e998ecc869ce574e586f6e\", \"19f015d1bde5509e0c6353db9a23bfe5\", \"1b7d40f3c0e7a5305056e01a9e134373\", \"ff45e18cbc06c8425b1d602774877c21\", \"56af9b212c0a2f543a281d4ae9007043\", \"df37db282bc6c4e039c61a29e420af5c\", \"c7c076fbd6febb6f3761028dfe301c3f\", \"d521259f9adda59b4b41bfdba68eb475\", \"175ccaa5b314a8ba2d658522027dba35\", \"8e4f932d44e5144d0d5651600762e638\", \"fb14cfaf3f16b11c5967a75c0f292932\", \"3b611de344a100261c5c677e2306ab22\", \"cbe13b44e16d68cae24a4d89eb9d81b5\", \"c392b4d21413e2b070b86f15df058de9\", \"956fa1e27c2ecc8119239af3290c4533\", \"6d924b90a440cf6f9c0717368967cf89\", \"c4315166a6130cab4e0b43ef42b64947\", \"a9fab648c7505e36489b5b9ab7da0d35\", \"c5dd12a970571642bba6c602fe8fa876\", \"b2ffd08affcb9fe92c16ff46a0bb92f5\", \"539ac6a698a6520bf5f35c1701218803\", \"45873640cae5b179e56f9a0ea1057b0c\", \"09aa8ae135abc28dbb9f4a5b622f34a8\", \"77697c1a7d32716e3b32708c3ab2801e\", \"c5fc9f98a11f97f004078e8242812866\", \"ea6681cf23c3fcd92dae875676a6d943\", \"b55f759ff8d17a7fe739578ce59985e1\", \"2f734906edd2e41355c3055778c3d3d2\", \"7480754a4de2131d4720b02701d36581\", \"89dd1903bd424cef637549e618a1d40d\", \"9125df2a5808bb33afc0f847eb61c39b\", \"1fafc4ea462d1f92a8e81a2200583f72\", \"61d3fcef1c3a54c98b6cb0cd5b5aa055\", \"55b57d3f98462d4b8f2c4a76ecb8bd6e\", \"eb3ce79a72eed63ba9ee5c6462c1162b\", \"7d6041ff1206f002c4f128b6e082fac1\", \"4d6eabedba5dcc213514129b0c65ab85\", \"b18b61b9ff251ce1cf02d0cf1c88ccea\", \"606049a4d2342af84fac3e11e7911936\", \"f6f0a5c3ff60317a00f5c872f7356840\", \"71c54a7d5c6e801529a35c735a65bde4\", \"9e0bd29bdf6a5dba38a4467938c16449\", \"4034cafa2dae34bb6f5faa420862a97f\", \"63b5ae2de0bca71650071072324230bb\", \"69104219dd1dfba0411c773ed0f6d625\", \"804dc27facc9e7c89e4712ff5c75cf17\", \"e2afe02704ecba15ae3d03c4f9e22d62\", \"e6be7ef74eaa0fe8545199f18a23db12\", \"c274e2359d3869387cd43c297dc6619c\", \"8a96d0d526ec605badd64e66641b7282\", \"8a16f6b0c0aaf3fea563f13145bfafd8\", \"6a439500d59e0b5ad6f050bd227c486e\", \"22d21ddbfe92fde0fead288f077193dd\", \"8a56387235593cabf24f702e17e4d6d9\", \"4256f875c7f964558ea7d2ed2aed1592\", \"b371cce43ed0dd19a6923fabb1a9675e\", \"91b91499264f221b6d4cb78c14143a59\", \"cf811eb4867b5e2e2d5d4a16ce67aee2\", \"7dff11b208983aa1ebc8f759fb3475db\", \"deb6c2176c006634bec3457f74192e2e\", \"0c130cf5e584531bfe5c3518ece92b81\", \"881c2188979ae7a97db5902d7ad492ac\", \"d38bafed90d722c9e9eca25d44908f9c\", \"f34add451e3661cdff2685c1a594ccc2\", \"d439d78a340d8158780a559c8760b573\", \"96a85de76f4e36588162dec5e1542e62\", \"9b59868d766364f467d951a169c25be8\", \"c5f81d96917aef59a8304d1b7957a1e3\", \"e2777b36be601d17ead5f2bdac468235\", \"f7e3be2d135812404ae5dc1335f3bd98\", \"51fc5c923392e607b7d638b3e5d2d21f\", \"cee578134c25e028be66e3d64b92eb0e\", \"f944daf2cc2c2eefb27ab956e9ec5152\", \"18fbb3c83550ea4d45712b64ae5eacb8\", \"2e454d04db25e2d067681e4b3df8b49a\", \"d6d44685d68a8eec33227c46310d2b9d\", \"f9b7d051a4b00e02ae87856805b863c8\", \"5d92dc18fdc54f776281e12d3694e157\", \"7c582f3d240e501a27c041636ca97fe2\", \"dba8aab34ef1b73fefb23de305b3427d\", \"1193ca920e04530195d8f1420c909ef9\"], \"authentication\": [\"94f7ead2a94ff76c65c3491ccb4c159e\", \"3b8f55820987c6214d582f02eef9904c\", \"db6299ddd6137aabc7d1495a4a47e802\", \"a44a679bb2ef89aa564531eeef5a9a0d\", \"11d03720aa4a5d9d62468c3d816edb3c\", \"4f993ee6700e2fa3fdb8161b35e136e2\", \"40db90ba3ae4142446729cd202fea9e9\", \"ae220064c184f549e10ae2f2042a2024\", \"2665ecfcd1bc0b08f59de309fc66ea70\", \"ec71f69dc5ff0cb551f365415f37fc2f\", \"bc05bde2cc89f8b6335491bf3097bac7\", \"271bff636a4a5f6bfd5b6e62c7cba630\", \"84527f88d20cbcfbf661b2684547889b\", \"8d2eecc1675b93bfa0c7a7312160f9d1\", \"e6782e7bda689ceffdce9f2a05744259\", \"737ff86b33fe4a48416fbf6fa9888d9d\", \"79f942586cc30f7ff829670387c8ca20\", \"3d993923184200bc5dc020ecd422a3cd\", \"87b8fbefe97fb41fa0320dc07509a649\", \"e2c70643a297885a996f81d571e61dde\", \"dbf81d47e79eea74fcd2200495e7c538\", \"bb7d9bbac01a91478d37630bf2a06334\", \"1ab1c778ecd253753299235c120143a8\", \"cd4d8b1fed4a8f75fa3567381fa98707\", \"5beb77a2ae733a80965e294704600448\", \"f9e2df10164bf8925c7113732b581b44\", \"7966fe5564487a131a26c88f26d2ea99\", \"28dc449e14fef61e435c47feb63dad69\", \"d63b9debc4c0101c3df9e60b87d8393b\", \"f4d7b76da8691f99fb15f32d60aeaea3\", \"9e68c345d0d7eded3949db6c2dd4bbe3\", \"3f761558caa9d417e8bbd46a4a5a51ee\", \"66385c80d2c8b581b4beeab821402ba3\", \"399c5df23184e4c6a394bc57407c69b4\", \"17ffd7e378cdc52bd9dc820207730681\", \"1d46bdfd454ed352c461a138d7b90597\", \"d37a3627a14f02750533fb0878600ec9\", \"bd107e2fcbcd97e2f617e1bed7b34776\", \"ea5a28a52d0f77db2c268768a720a9af\", \"a3879056e97c5fd1d610cae2448e8321\", \"4ef47aa9eaff9a14576a02b81355a90c\", \"7037d1c1751f80a35565213a20169c44\", \"318b9f68b4cdfc4603812fad344003d4\", \"6ee843adfb2f7359dcf58602a09538ca\", \"0e2c1d749c1c71304a634602d0c12748\", \"6bdaa774256fbac4a9f9011a7131577d\"]}', NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_member_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_member_user`;
CREATE TABLE `tb_member_user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `password` char(64) NOT NULL COMMENT '密码',
  `status` tinyint NOT NULL COMMENT '状态:1.启用、2.禁用、3.锁定',
  `username` varchar(32) NOT NULL COMMENT '登录帐号',
  `gender` tinyint NOT NULL COMMENT '性别:10.男,20.女',
  `phone_number` varchar(64) DEFAULT NULL COMMENT '电话号码',
  `groups_info` json DEFAULT NULL COMMENT '组信息',
  `resource_map` json DEFAULT NULL COMMENT '资源信息',
  `session_key` varchar(128) DEFAULT NULL COMMENT '微信 session key',
  `open_id` varchar(128) DEFAULT NULL COMMENT '微信 open id',
  `union_id` varchar(128) DEFAULT NULL COMMENT '微信 union id',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='会员用户表';

-- ----------------------------
-- Records of tb_member_user
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
