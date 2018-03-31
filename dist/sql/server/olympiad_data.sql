CREATE TABLE IF NOT EXISTS `olympiad_data` (
  `id`                 TINYINT UNSIGNED    NOT NULL DEFAULT 0,
  `current_cycle`      MEDIUMINT UNSIGNED  NOT NULL DEFAULT 1,
  `period`             MEDIUMINT UNSIGNED  NOT NULL DEFAULT 0,
  `olympiad_end`       BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `validation_end`     BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `next_weekly_change` BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
);