USE `fast-crud`;
DROP TABLE IF EXISTS student_sensitive;
DROP TABLE IF EXISTS student;

CREATE TABLE student (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  avatar_url    VARCHAR(255)          DEFAULT NULL,
  gallery       VARCHAR(1000)         DEFAULT NULL,
  name          VARCHAR(64)           DEFAULT NULL,
  age           INT                   DEFAULT NULL,
  birthday      DATE                  DEFAULT NULL,
  sex           VARCHAR(8)            DEFAULT NULL,
  state         VARCHAR(8)            DEFAULT NULL,
  love_id       BIGINT                DEFAULT NULL,
  love_name     VARCHAR(64)           DEFAULT NULL,
  foe_id        BIGINT                DEFAULT NULL,
  info          VARCHAR(500)          DEFAULT NULL,
  graduated     TINYINT(1)            DEFAULT NULL,
  luck_time     TIME                  DEFAULT NULL,
  resume_url    VARCHAR(1000)         DEFAULT NULL,
  create_time   DATETIME              DEFAULT NULL,
  update_time   DATETIME              DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE student_sensitive (
  id          BIGINT NOT NULL AUTO_INCREMENT,
  student_id  BIGINT       DEFAULT NULL,
  id_card     VARCHAR(32)  DEFAULT NULL,
  address     VARCHAR(255) DEFAULT NULL,
  phone       VARCHAR(32)  DEFAULT NULL,
  create_time DATETIME     DEFAULT NULL,
  update_time DATETIME     DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO student (id, name, age, birthday, sex, state, love_id, love_name, foe_id, info, graduated, luck_time, create_time, update_time) VALUES
 (1, '大乔', 18, '2007-03-15', '0', '3', 3, '孙策', NULL, '江东二乔之姊', 0, '08:30:00', NOW(), NOW()),
 (2, '小乔', 16, '2009-07-21', '0', '3', 4, '周瑜', NULL, '江东二乔之妹', 0, '09:15:00', NOW(), NOW()),
 (3, '曹操', 45, '1980-01-01', '1', '1', NULL, NULL, NULL, '魏武帝', 1, '12:00:00', NOW(), NOW()),
 (4, '孙策', 26, '1999-05-05', '1', '3', 1, '大乔', NULL, '江东小霸王', 1, '07:00:00', NOW(), NOW()),
 (5, '周瑜', 25, '2000-09-09', '1', '3', 2, '小乔', NULL, '东吴大都督', 1, '06:45:00', NOW(), NOW()),
 (6, '刘备', 40, '1985-06-01', '1', '2', NULL, NULL, NULL, '汉昭烈帝', 1, '11:30:00', NOW(), NOW()),
 (7, '关羽', 42, '1983-02-02', '1', '2', NULL, NULL, NULL, '武圣', 1, '10:00:00', NOW(), NOW()),
 (8, '貂蝉', 20, '2005-11-11', '0', '4', NULL, NULL, NULL, '四大美女之一', 0, '05:20:00', NOW(), NOW());

INSERT INTO student_sensitive (student_id, id_card, address, phone, create_time, update_time) VALUES
 (1, '320101200703150001', '江东吴郡富春', '13800000001', NOW(), NOW()),
 (2, '320101200907210002', '江东吴郡富春', '13800000002', NOW(), NOW()),
 (3, '110101198001010003', '沛国谯县', '13800000003', NOW(), NOW());
