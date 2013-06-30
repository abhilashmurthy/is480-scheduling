--
-- Dumping data for table `schedule`
--

INSERT INTO `schedule` (`milestone`, `term_id`, `startDate`, `endDate`) VALUES
('Acceptance', 1, '2013-08-07 00:00:00', '2013-09-20 00:00:00');

--
-- Dumping data for table `time_slot`
--

INSERT INTO `time_slot` (`term_id`, `milestone`, `startTime`, `endTime`, `team_id`) VALUES
(1, 'Acceptance', '2013-08-07 10:00:00', '2013-08-07 11:00:00', NULL),
(1, 'Acceptance', '2013-08-07 11:00:00', '2013-08-07 12:00:00', NULL),
(1, 'Acceptance', '2013-08-07 12:00:00', '2013-08-07 13:00:00', NULL),
(1, 'Acceptance', '2013-08-07 13:00:00', '2013-08-07 14:00:00', NULL),
(1, 'Acceptance', '2013-08-07 14:00:00', '2013-08-07 15:00:00', NULL);
