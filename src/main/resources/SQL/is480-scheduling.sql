-- phpMyAdmin SQL Dump
-- version 3.5.7
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jun 30, 2013 at 01:24 PM
-- Server version: 5.5.29
-- PHP Version: 5.4.10

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- Database: `is480-scheduling`
--

-- --------------------------------------------------------

--
-- Table structure for table `schedule`
--

CREATE TABLE `schedule` (
  `milestone` varchar(50) NOT NULL COMMENT 'Enum column storing ACCEPTANCE, MIDTERM, FINAL',
  `term_id` bigint(11) NOT NULL,
  `startDate` datetime NOT NULL,
  `endDate` datetime NOT NULL,
  PRIMARY KEY (`milestone`,`term_id`),
  KEY `term_id` (`term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `schedule`
--

INSERT INTO `schedule` (`milestone`, `term_id`, `startDate`, `endDate`) VALUES
('ACCEPTANCE', 1, '2013-08-07 00:00:00', '2013-09-20 00:00:00');

-- --------------------------------------------------------

--
-- Table structure for table `team`
--

CREATE TABLE `team` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `term_id` bigint(11) DEFAULT NULL,
  `reviewer1` bigint(11) DEFAULT NULL,
  `reviewer2` bigint(11) DEFAULT NULL,
  `supervisor` bigint(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `term_id_fk` (`term_id`),
  KEY `reviewer1` (`reviewer1`),
  KEY `reviewer2` (`reviewer2`),
  KEY `supervisor` (`supervisor`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `team`
--

INSERT INTO `team` (`id`, `name`, `term_id`, `reviewer1`, `reviewer2`, `supervisor`) VALUES
(1, 'Thunderbolt', 1, 7, 8, 6);

-- --------------------------------------------------------

--
-- Table structure for table `term`
--

CREATE TABLE `term` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `year` date NOT NULL,
  `term` bigint(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;

--
-- Dumping data for table `term`
--

INSERT INTO `term` (`id`, `year`, `term`) VALUES
(1, '2013-01-01', 1),
(2, '2013-01-01', 2);

-- --------------------------------------------------------

--
-- Table structure for table `time_slot`
--

CREATE TABLE `time_slot` (
  `term_id` bigint(11) NOT NULL,
  `milestone` varchar(50) NOT NULL COMMENT 'Enum column storing ACCEPTANCE, MIDTERM, FINAL',
  `startTime` datetime NOT NULL,
  `endTime` datetime NOT NULL,
  `team_id` bigint(11) DEFAULT NULL,
  PRIMARY KEY (`term_id`,`milestone`,`startTime`),
  KEY `team_id` (`team_id`),
  KEY `FK_schedule` (`milestone`,`term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `time_slot`
--

INSERT INTO `time_slot` (`term_id`, `milestone`, `startTime`, `endTime`, `team_id`) VALUES
(1, 'ACCEPTANCE', '2013-08-07 10:00:00', '2013-08-07 11:00:00', NULL),
(1, 'ACCEPTANCE', '2013-08-07 11:00:00', '2013-08-07 12:00:00', NULL),
(1, 'ACCEPTANCE', '2013-08-07 12:00:00', '2013-08-07 13:00:00', NULL),
(1, 'ACCEPTANCE', '2013-08-07 13:00:00', '2013-08-07 14:00:00', NULL),
(1, 'ACCEPTANCE', '2013-08-07 14:00:00', '2013-08-07 15:00:00', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `time_slot_status`
--

CREATE TABLE `time_slot_status` (
  `term_id` bigint(11) NOT NULL,
  `milestone` varchar(50) NOT NULL COMMENT 'Enum column storing ACCEPTANCE, MIDTERM, FINAL',
  `startTime` datetime NOT NULL,
  `user_id` bigint(11) NOT NULL,
  `status` varchar(50) NOT NULL COMMENT 'Enum column storing PENDING, ACCEPTED, REJECTED',
  PRIMARY KEY (`term_id`,`milestone`,`startTime`,`user_id`),
  KEY `FK_time_slot` (`milestone`,`term_id`,`startTime`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `firstName` varchar(50) NOT NULL,
  `lastName` varchar(50) NOT NULL,
  `username` varchar(50) NOT NULL,
  `team_id` bigint(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `team_id` (`team_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=9 ;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id`, `firstName`, `lastName`, `username`, `team_id`) VALUES
(1, 'Suresh', 'SUBRAMANIAM', 'suresh.s.2010', 1),
(2, 'Abhilash', 'MURTHY', 'abhilashm.2010', 1),
(3, 'Tarlochan Singh', 'GILL', 'tsgill.ps.2010', 1),
(4, 'Prakhar', 'AGARWAL', 'prakhara.2010', 1),
(5, 'Xuling', 'DAI', 'xuling.dai.2010', 1),
(6, 'Richard', 'DAVIS', 'rcdavis', NULL),
(7, 'Youngsoo', 'KIM', 'yskim', NULL),
(8, 'Lai-Tee', 'CHEOK', 'laiteecheok', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `user_role`
--

CREATE TABLE `user_role` (
  `user_id` bigint(11) NOT NULL,
  `term_id` bigint(11) NOT NULL,
  `role` varchar(50) NOT NULL  COMMENT 'Enum column storing SUPERVISOR, REVIEWER, STUDENT, TA',
  PRIMARY KEY (`user_id`,`term_id`,`role`),
  KEY `term_id` (`term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `schedule`
--
ALTER TABLE `schedule`
  ADD CONSTRAINT `schedule_ibfk_1` FOREIGN KEY (`term_id`) REFERENCES `term` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `team`
--
ALTER TABLE `team`
  ADD CONSTRAINT `team_ibfk_1` FOREIGN KEY (`term_id`) REFERENCES `term` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `team_ibfk_2` FOREIGN KEY (`reviewer1`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `team_ibfk_3` FOREIGN KEY (`reviewer2`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `team_ibfk_4` FOREIGN KEY (`supervisor`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `time_slot`
--
ALTER TABLE `time_slot`
  ADD CONSTRAINT `FK_schedule` FOREIGN KEY (`milestone`, `term_id`) REFERENCES `schedule` (`milestone`, `term_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `time_slot_ibfk_1` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `time_slot_status`
--
ALTER TABLE `time_slot_status`
  ADD CONSTRAINT `FK_time_slot` FOREIGN KEY (`milestone`, `term_id`, `startTime`) REFERENCES `time_slot` (`milestone`, `term_id`, `startTime`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `user`
--
ALTER TABLE `user`
  ADD CONSTRAINT `user_ibfk_1` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `user_role`
--
ALTER TABLE `user_role`
  ADD CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user_role_ibfk_2` FOREIGN KEY (`term_id`) REFERENCES `term` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
