/*
 Navicat Premium Dump SQL

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : spring_boot_test

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 23/10/2026 16:50:33
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_admin
-- ----------------------------
DROP TABLE IF EXISTS `t_admin`;
CREATE TABLE `t_admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '后台账号（唯一）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（BCrypt 密文）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执事名号（展示用）',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ADMIN' COMMENT '角色：ADMIN 执事',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1 启用 / 0 停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_admin
-- ----------------------------
INSERT INTO `t_admin` VALUES (1, 'admin', '$2a$10$hKuW6XQUa6X44fXIXn8.SOZ2HzTZf2nl6uNpqf6awTrQcGkSK8ShO', '仙门执事', 'ADMIN', 1, '2026-10-23 16:36:33');

-- ----------------------------
-- Table structure for t_category
-- ----------------------------
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID（0=顶级）',
  `level` tinyint NOT NULL DEFAULT 1 COMMENT '层级：1 顶级 / 2 子级',
  `icon` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标字符',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类描述',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序号（越小越前）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_parent_name`(`parent_id` ASC, `name` ASC) USING BTREE,
  INDEX `idx_parent`(`parent_id` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分级分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_category
-- ----------------------------
INSERT INTO `t_category` VALUES (1, '功法', 0, 1, '⚔', '剑诀、火法、体修、身法、御兽诸般法门', 1, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (2, '丹药', 0, 1, '❖', '疗伤、突破、淬毒各类丹方', 2, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (3, '洞天', 0, 1, '⛰', '秘境与灵脉所在之地', 3, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (4, '道友', 0, 1, '✦', '仙门同修名录', 4, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (5, '论道', 0, 1, '✍', '修行心得与问答', 5, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (11, '剑诀', 1, 2, '·', '以剑证道，锋芒无匹', 1, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (12, '火法', 1, 2, '·', '烈焰焚天，焚尽诸邪', 2, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (13, '体修', 1, 2, '·', '以身为炉，淬炼道体', 3, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (14, '身法', 1, 2, '·', '来去无痕，踏虚而行', 4, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (15, '御兽', 1, 2, '·', '万灵臣服，听我号令', 5, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (21, '疗伤丹', 2, 2, '·', '生死人肉白骨', 1, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (22, '突破丹', 2, 2, '·', '助破境凝婴', 2, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (23, '毒丹', 2, 2, '·', '阴毒之物，慎用', 3, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (31, '秘境', 3, 2, '·', '险境藏机缘', 1, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (32, '灵脉', 3, 2, '·', '灵气汇聚之地', 2, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (41, '剑修', 4, 2, '·', '执剑者', 1, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (42, '丹修', 4, 2, '·', '炼丹者', 2, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (43, '体修', 4, 2, '·', '炼体者', 3, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (51, '心得', 5, 2, '·', '修行体悟', 1, '2026-10-16 16:31:40');
INSERT INTO `t_category` VALUES (52, '问答', 5, 2, '·', '疑难互答', 2, '2026-10-16 16:31:40');

-- ----------------------------
-- Table structure for t_content
-- ----------------------------
DROP TABLE IF EXISTS `t_content`;
CREATE TABLE `t_content`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简介',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '正文详情',
  `category_id` bigint NOT NULL COMMENT '所属分类ID（最末级）',
  `tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签（逗号分隔）',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作者 / 出处',
  `hot` int NOT NULL DEFAULT 0 COMMENT '热度',
  `views` int NOT NULL DEFAULT 0 COMMENT '浏览量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1 上架 / 0 下架',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_hot`(`hot` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_title`(`title` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '内容表（功法/丹药/洞天/道友/论道）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_content
-- ----------------------------
INSERT INTO `t_content` VALUES (1, '太虚剑典', '剑气纵横三万里，一剑光寒十九州。', '太虚剑典乃太虚剑尊所创，共分九重。初重练剑意，三重可斩灵，六重剑出如龙，九重则一剑破空、剑光寒彻十九州。修习者需以剑为友、以心为鞘，切忌贪快求成。', 11, '剑诀,太虚,上乘', '太虚剑尊', 98000, 12603, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (2, '斩灵十三剑', '十三剑落，灵亦为刍狗。', '斩灵十三剑共有十三式，招招断灵脉、斩元神。第七式「断魂」最险，若心境不稳易反噬自身，故需筑基后期方可习练。', 11, '剑诀,杀伐', '墨长歌', 61000, 8200, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (3, '星辰剑诀', '以星河为剑，剑落星辰坠。', '星辰剑诀借星河之力，于夜半子时引星辉入剑，剑身星辰流转。适合资质中上、耐得住寂寞的剑修。', 11, '剑诀,星辰', '苏九霄', 42000, 5300, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (4, '焚天诀', '焚尽苍穹，烈焰之下寸草不存。', '焚天诀以真火焚烧经脉再塑，修炼时痛楚难当，然成者一身真火焚山煮海。第三重「焚心」须以清心丹辅助，否则易走火。', 12, '火法,真火', '焚天老祖', 72000, 9100, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (5, '赤炎焚世录', '赤炎过处，山河俱焚。', '赤炎焚世录记载火法七篇，另有控火诀要。修至第五篇可隔空引火，第六篇焚世需合体期修为。', 12, '火法,焚世', '焚天老祖', 35000, 4300, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (6, '星辰炼体术', '以星河为炉，淬炼不朽道体。', '星辰炼体术于星辉最盛处引星力入体，日积月累，皮肉如精铁、骨骼如星辰。大成一拳可崩山，然需极强忍耐力。', 13, '体修,星辰,炼体', '星辰道人', 56000, 7800, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (7, '不灭金身诀', '金身不灭，万法难伤。', '不灭金身诀以身炼金身，需三味真火反复淬炼。金身成时可硬抗同阶三击而不损，是体修立身之本。', 13, '体修,金身', '顾青冥', 47000, 6200, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (8, '御风踏虚步', '踏虚而行，身随风动，来去无痕。', '御风踏虚步以身合风，初成可踏枝而行，大成则借风腾空，百丈之内转瞬即至。适合身手灵巧、擅长袭杀的道友。', 14, '身法,御风', '风行者', 33000, 4600, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (9, '九天雷影步', '雷落之前，影已先行。', '九天雷影步引天雷入步，动若雷霆。以雷淬足，初练时足下焦黑，须以灵药护体。成者身法快逾奔雷。', 14, '身法,雷', '雷影道人', 29000, 3800, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (10, '万灵御兽录', '万灵臣服，听我号令而行。', '万灵御兽录载有契约之法与驭兽之术。以血契为始，以心契为上。可为灵兽疗伤、共修，切忌强虐，否则契约反噬。', 15, '御兽,契约', '万兽真人', 28000, 3700, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (11, '灵兽契约术', '一纸血契，两心相通。', '灵兽契约术详解血契、魂契、心契三门。魂契最稳，可与灵兽共享修为气息；血契最速，然灵兽寿元有损。', 15, '御兽,契约', '万兽真人', 19000, 2400, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (12, '九转回春丹', '一丹入口，断肢可续。', '九转回春丹以九味灵药九转炉炼，成丹时丹香十里。重伤垂死服之可续命，断肢服之可复生。药材难得，一炉常只成三枚。', 21, '丹药,疗伤,九转', '玄天丹师', 68000, 8900, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (13, '生生造化丹', '造化之力，起死回生。', '生生造化丹需千年灵芝与地心火炉相合，丹成之时有造化之气环绕。仅存于古籍，能炼者不过三人。', 21, '丹药,疗伤,造化', '玄天丹师', 41000, 5200, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (14, '破境丹', '一丹破境，桎梏全消。', '破境丹专助卡在瓶颈的道友冲关。服用后经脉灵气暴涨，若心境不足则易爆体而亡，务必在静室中闭关服用。', 22, '丹药,突破', '白凤眠', 58000, 7300, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (15, '凝婴丹', '元婴凝，大道初成。', '凝婴丹以元婴期灵兽内丹为主药，助金丹圆满者凝成元婴。丹成率极低，故价高且常为大宗门垄断。', 22, '丹药,突破,元婴', '白凤眠', 52000, 6800, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (16, '蚀骨销魂散', '无声无息，蚀骨销魂。', '蚀骨销魂散无色无味，中者经脉渐腐而不自知。解药难寻，唯九转回春丹可缓。此物为仙门禁品，慎之慎之。', 23, '毒丹,禁品', '暗影客', 23000, 3000, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (17, '万毒腐心丹', '万毒入心，神仙难救。', '万毒腐心丹以百种毒物炼就，专破护体罡气。然炼制者常自伤，故世间存量极少。', 23, '毒丹,禁品', '暗影客', 15000, 1900, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (18, '落霞谷', '落霞与灵雾齐飞，灵气渐复之地。', '落霞谷深藏群山之间，谷中灵雾终年不散，灵药遍地。每月初一十五灵潮最盛，适合闭关与采药。近期灵气渐复，开放时间调整为先辰。', 31, '秘境,落霞,灵药', '仙门执事', 54000, 7100, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (19, '万骨窟', '枯骨成山，机缘与凶险并存。', '万骨窟乃上古战场遗迹，白骨堆积如山，怨气极重。深处藏有上古法宝碎片，然也有化神级尸傀游荡，非结伴不可入。', 31, '秘境,险地', '仙门执事', 49000, 6400, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (20, '云梦泽', '云梦浩渺，水气化灵。', '云梦泽水汽氤氲，日久化灵，泽中灵鱼可食可炼。泽心有一孤岛，岛上石台据传为古修讲道之处。', 31, '秘境,云梦', '云梦真人', 31000, 4200, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (21, '青龙灵脉', '龙气蜿蜒，灵气如泉。', '青龙灵脉横贯三百里，脉眼灵气浓郁如泉，于其中打坐一日可抵外间十日。灵脉归属仙门，需凭功勋换取修行时日。', 32, '灵脉,青龙', '仙门执事', 60000, 7901, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (22, '太阴灵泉', '太阴之气，滋养生机。', '太阴灵泉自地底涌出，泉水偏寒，可洗髓伐骨、调和爆裂功法之后患。体修尤为钟爱。', 32, '灵脉,太阴', '仙门执事', 37000, 4800, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (23, '墨长歌', '大乘圆满，剑修之冠，斩灵十三剑独步天下。', '墨长歌出身寒微，以凡俗之身一步步入道，历时八百载至大乘圆满。手中长剑「问心」随其走过百余场生死之战。', 41, '道友,剑修,大乘', '仙门名录', 88000, 11200, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (24, '苏九霄', '渡劫中期，善以星河之力炼剑。', '苏九霄年少成名，喜观星，自创星辰剑诀。性孤僻，不喜应酬，常年居于观星崖。', 41, '道友,剑修,渡劫', '仙门名录', 66000, 8400, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (25, '白凤眠', '合体后期，丹道大家，一炉可通生死。', '白凤眠执掌丹阁三十载，所炼破境丹、凝婴丹为仙门立下大功。其人温和，凡有求丹者皆不拒，唯要求以药换药。', 42, '道友,丹修,丹阁', '仙门名录', 71000, 9001, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (26, '玄天丹师', '丹道宗师，九转回春丹传世。', '玄天丹师为上古丹修，著有玄天丹经。传闻其已飞升，唯留丹经与三炉丹方于人间。', 42, '道友,丹修,上古', '仙门名录', 63000, 8100, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (27, '顾青冥', '合体初期，体修新秀，不灭金身初成。', '顾青冥以炼体入道，日行万仞山间，肉身为炉。金身初成之日，硬抗合体期一击而退半步，震动仙门。', 43, '道友,体修,金身', '仙门名录', 45000, 5900, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (28, '云无咎', '元婴圆满，以星辰炼体术破境在即。', '云无咎资质平平，然勤修不辍，以勤补拙。同门皆言其破境只差一线机缘。', 43, '道友,体修,元婴', '仙门名录', 34000, 4400, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (29, '论道三问', '修道者当自问：为何修、修向何处、修成如何。', '修行之路，最忌盲从。道友当自问三事：一问我为何修行，二问我欲修向何处，三问修成之后欲作何为。三问有答，道心方稳。', 51, '论道,心得,道心', '仙门长老', 40000, 5100, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (30, '破境心得录', '破境之要，在于心境先足，灵力后至。', '多数道友破境失败，非灵力不足，而是心境未稳。建议破境前先静心三月，观云、听泉、弈棋皆可。心境足则灵力自至。', 51, '论道,心得,破境', '白凤眠', 38000, 4900, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (31, '拜师求道须知', '择师三看：看道心、看耐心、看品行。', '拜师求道，非择名头。一看道心是否正，二看是否有教人之耐心，三看品行是否可为师表。三者齐备，纵然境界不高，亦是良师。', 52, '论道,问答,拜师', '仙门长老', 44000, 5700, 1, '2026-10-16 16:31:40');
INSERT INTO `t_content` VALUES (32, '渡劫十二问', '渡劫常见疑难十二则，逐条解答。', '汇总道友渡劫常见十二问：雷劫可否借外力、丹劫如何护炉、心劫怎么破……每问附一二解法，供道友参考。', 52, '论道,问答,渡劫', '雷影道人', 36000, 4700, 1, '2026-10-16 16:31:40');

-- ----------------------------
-- Table structure for t_notice
-- ----------------------------
DROP TABLE IF EXISTS `t_notice`;
CREATE TABLE `t_notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
  `date_label` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '时间标签（今日/昨日…）',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sort`(`sort_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宗门公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_notice
-- ----------------------------
INSERT INTO `t_notice` VALUES (1, '仙门大比将于三日后于论道台开启，道友可前往报名。', '今日', 1, '2026-10-16 16:31:40');
INSERT INTO `t_notice` VALUES (2, '洞天福地「落霞谷」灵气渐复，开放时间调整为先辰。', '昨日', 2, '2026-10-16 16:31:40');
INSERT INTO `t_notice` VALUES (3, '丹阁新入九转金丹三百枚，先到先得。', '前日', 3, '2026-10-16 16:31:40');
INSERT INTO `t_notice` VALUES (4, '青龙灵脉本月功勋兑换额度刷新，道友可依功勋换取修行时日。', '三日前', 4, '2026-10-16 16:31:40');

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '道号 / 账号（唯一）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '秘语 / 密码（BCrypt 密文）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结缘（注册）时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仙门弟子（用户）表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1, 'ghjjvj', '$2a$10$xWQ5TGuFrkWlYZUKm6sSSuJc77oLUx.CeOuTEVMAb5cwH5970jcv2', '2026-10-14 09:23:39');
INSERT INTO `t_user` VALUES (2, '王林', '$2a$10$82TleiFXWZfiRUpjYMFUGeUbrZsdhpVY3Hhb2ofYymw.kkdDesw56', '2026-10-14 09:41:05');

SET FOREIGN_KEY_CHECKS = 1;
