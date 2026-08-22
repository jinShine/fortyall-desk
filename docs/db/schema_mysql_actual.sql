/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `login_phone` varchar(20) NOT NULL,
  `status` enum('ACTIVE','WITHDRAWN') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_login_phone` (`login_phone`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `center` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `booking_window_days` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `renewal_grace_days` int NOT NULL,
  `status` enum('ACTIVE','SUSPENDED','TERMINATED') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `idempotency_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `idem_key` varchar(100) NOT NULL,
  `result_json` mediumtext NOT NULL,
  `result_type` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_idempotency_key` (`idem_key`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lesson` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `end_time` time(6) NOT NULL,
  `lesson_date` date NOT NULL,
  `manually_modified` bit(1) NOT NULL,
  `origin` enum('REGULAR','SELF_BOOKED') NOT NULL,
  `start_time` time(6) NOT NULL,
  `center_id` bigint NOT NULL,
  `coach_membership_id` bigint NOT NULL,
  `standing_schedule_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lesson_coach_slot` (`coach_membership_id`,`lesson_date`,`start_time`),
  UNIQUE KEY `uk_lesson_schedule_date` (`standing_schedule_id`,`lesson_date`),
  KEY `FKhgf3umtx3ki8dk9gffjropil8` (`center_id`),
  CONSTRAINT `FKf104d8j9dban94tdktrrrj183` FOREIGN KEY (`coach_membership_id`) REFERENCES `membership` (`id`),
  CONSTRAINT `FKhgf3umtx3ki8dk9gffjropil8` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`),
  CONSTRAINT `FKqloifkt4phv4apb3nkfekgbho` FOREIGN KEY (`standing_schedule_id`) REFERENCES `standing_schedule` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lesson_participant` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `reserved` bit(1) NOT NULL,
  `status` enum('ATTENDED','EARLY_CANCEL','LATE_CANCEL','NO_SHOW','SCHEDULED','SUSPENDED') NOT NULL,
  `lesson_id` bigint NOT NULL,
  `lesson_pass_id` bigint DEFAULT NULL,
  `membership_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_participant_lesson_member` (`lesson_id`,`membership_id`),
  KEY `FK8wgcv9aeihil20madqt2thudt` (`lesson_pass_id`),
  KEY `FKikgfkcf8rottxdbl1aw9c810p` (`membership_id`),
  CONSTRAINT `FK4wj8p73vlsmwy8bhbnyju4lu2` FOREIGN KEY (`lesson_id`) REFERENCES `lesson` (`id`),
  CONSTRAINT `FK8wgcv9aeihil20madqt2thudt` FOREIGN KEY (`lesson_pass_id`) REFERENCES `lesson_pass` (`id`),
  CONSTRAINT `FKikgfkcf8rottxdbl1aw9c810p` FOREIGN KEY (`membership_id`) REFERENCES `membership` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lesson_pass` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `paid_amount` decimal(12,2) NOT NULL,
  `product_name_snapshot` varchar(100) NOT NULL,
  `session_count_snapshot` int NOT NULL,
  `status` enum('ACTIVE','CANCELLED','EXHAUSTED','EXPIRED','PENDING') NOT NULL,
  `valid_from` date NOT NULL,
  `valid_until` date NOT NULL,
  `membership_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnuhj3sn59cc95hqau1lvsyii9` (`membership_id`),
  KEY `FKp88s6v8q9l9nnedgt8nisjfry` (`product_id`),
  CONSTRAINT `FKnuhj3sn59cc95hqau1lvsyii9` FOREIGN KEY (`membership_id`) REFERENCES `membership` (`id`),
  CONSTRAINT `FKp88s6v8q9l9nnedgt8nisjfry` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `membership` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `contact_phone` varchar(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `status` enum('ACTIVE','DORMANT','PENDING','WITHDRAWN') NOT NULL,
  `account_id` bigint DEFAULT NULL,
  `center_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_membership_center_phone` (`center_id`,`contact_phone`),
  KEY `FKd1yliqdvipm4yvq2tulbktpg6` (`account_id`),
  CONSTRAINT `FKd1yliqdvipm4yvq2tulbktpg6` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `FKr6fhdv5or9rhvsfymk0tkfcqj` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `membership_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role` enum('ADMIN','COACH','MEMBER') NOT NULL,
  `membership_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_membership_role` (`membership_id`,`role`),
  CONSTRAINT `FK4q9ii00n33edpii6bnev1evem` FOREIGN KEY (`membership_id`) REFERENCES `membership` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pass_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `delta` int NOT NULL,
  `lesson_participant_id` bigint DEFAULT NULL,
  `memo` varchar(200) DEFAULT NULL,
  `type` enum('ADJUST','ATTEND','CARRY_OVER','DEBT_SETTLE','EXPIRE','EXPIRE_RESTORE','ISSUE','LATE_CANCEL','NO_SHOW','RESTORE') NOT NULL,
  `lesson_pass_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5mnpn9easedynd9ut8p4nyjrn` (`lesson_pass_id`),
  CONSTRAINT `FK5mnpn9easedynd9ut8p4nyjrn` FOREIGN KEY (`lesson_pass_id`) REFERENCES `lesson_pass` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `capacity` int NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `duration_minutes` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `price` decimal(12,2) NOT NULL,
  `session_count` int NOT NULL,
  `valid_days` int NOT NULL,
  `center_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrvn0ppulf4idf1baugmyeqhfk` (`center_id`),
  CONSTRAINT `FKrvn0ppulf4idf1baugmyeqhfk` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `regular_enrollment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `grace_until` date DEFAULT NULL,
  `hold_until` date DEFAULT NULL,
  `status` enum('ACTIVE','GRACE','HOLD','TERMINATED') NOT NULL,
  `active_pass_id` bigint DEFAULT NULL,
  `membership_id` bigint NOT NULL,
  `next_pass_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq28xeipoggcf8rmv2g9er4ig1` (`active_pass_id`),
  KEY `FKsj2xy8k8neutclsnsc1qmvqdq` (`membership_id`),
  KEY `FK5tdc5ladal6yg7wak4gs2lvj9` (`next_pass_id`),
  CONSTRAINT `FK5tdc5ladal6yg7wak4gs2lvj9` FOREIGN KEY (`next_pass_id`) REFERENCES `lesson_pass` (`id`),
  CONSTRAINT `FKq28xeipoggcf8rmv2g9er4ig1` FOREIGN KEY (`active_pass_id`) REFERENCES `lesson_pass` (`id`),
  CONSTRAINT `FKsj2xy8k8neutclsnsc1qmvqdq` FOREIGN KEY (`membership_id`) REFERENCES `membership` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schedule_participant` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `regular_enrollment_id` bigint NOT NULL,
  `standing_schedule_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schedule_participant` (`standing_schedule_id`,`regular_enrollment_id`),
  KEY `FKdf1l92po8kb8s3sw2l8xdvluj` (`regular_enrollment_id`),
  CONSTRAINT `FKdf1l92po8kb8s3sw2l8xdvluj` FOREIGN KEY (`regular_enrollment_id`) REFERENCES `regular_enrollment` (`id`),
  CONSTRAINT `FKf3uhfq9geu046idgvko1fgl0v` FOREIGN KEY (`standing_schedule_id`) REFERENCES `standing_schedule` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `standing_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `day_of_week` enum('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') NOT NULL,
  `start_time` time(6) NOT NULL,
  `status` enum('ACTIVE','TERMINATED') NOT NULL,
  `center_id` bigint NOT NULL,
  `coach_membership_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5eu9nnnmx3oxowmirgvsmt2o4` (`center_id`),
  KEY `FK97yjfuxc05mkhys59ia1d7pw8` (`coach_membership_id`),
  KEY `FK3yxxe995p778puytjs7syeujk` (`product_id`),
  CONSTRAINT `FK3yxxe995p778puytjs7syeujk` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FK5eu9nnnmx3oxowmirgvsmt2o4` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`),
  CONSTRAINT `FK97yjfuxc05mkhys59ia1d7pw8` FOREIGN KEY (`coach_membership_id`) REFERENCES `membership` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
