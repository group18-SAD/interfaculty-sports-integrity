-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 14, 2026 at 03:11 PM
-- Server version: 10.11.16-MariaDB
-- PHP Version: 8.5.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `sports_integrity`
--

-- --------------------------------------------------------

--
-- Table structure for table `app_user`
--

CREATE TABLE `app_user` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('CLERK','ADMIN') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `app_user`
--

INSERT INTO `app_user` (`id`, `username`, `password_hash`, `role`) VALUES
(1, 'admin', '65536:Oc9JfpzhyfHdVRNTzpod1g==:ItgWkhBz4z6rk+sP24MM9gvDDyKaJWgMdDx/GGm5PXU=', 'ADMIN'),
(2, 'LOE', '65536:g1m8NKsaKGFiFP/Sz1ojqg==:GybbPvZIkEZkHs2BwsHzs0AMkd/vp3nlg7lSavrgyhA=', 'CLERK');

-- --------------------------------------------------------

--
-- Table structure for table `faculty`
--

CREATE TABLE `faculty` (
  `id` int(11) NOT NULL,
  `code` varchar(10) NOT NULL,
  `name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `faculty`
--

INSERT INTO `faculty` (`id`, `code`, `name`) VALUES
(1, 'SOMAC', 'School of Management and Commerce'),
(2, 'SOL', 'School of Law'),
(3, 'CHSS', 'College of Humanities and Social Sciences'),
(4, 'CODEL', 'College of Development Studies');

-- --------------------------------------------------------

--
-- Table structure for table `fixture`
--

CREATE TABLE `fixture` (
  `id` int(11) NOT NULL,
  `sport_id` int(11) NOT NULL,
  `home_faculty_id` int(11) NOT NULL,
  `away_faculty_id` int(11) NOT NULL,
  `fixture_date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `fixture`
--

INSERT INTO `fixture` (`id`, `sport_id`, `home_faculty_id`, `away_faculty_id`, `fixture_date`) VALUES
(1, 3, 2, 1, '2026-11-14'),
(2, 1, 3, 2, '2026-11-14'),
(3, 2, 4, 2, '2026-11-14');

-- --------------------------------------------------------

--
-- Table structure for table `flag`
--

CREATE TABLE `flag` (
  `id` int(11) NOT NULL,
  `fixture_id` int(11) NOT NULL,
  `rule_code` varchar(10) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` enum('Flagged','Under Investigation','Cleared','Penalized') DEFAULT 'Flagged',
  `raised_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `flag`
--

INSERT INTO `flag` (`id`, `fixture_id`, `rule_code`, `reason`, `status`, `raised_at`) VALUES
(1, 1, 'R2', 'Margin 40 exceeds the Basketball threshold of 25', 'Flagged', '2026-11-14 07:56:18'),
(2, 2, 'R2', 'Margin 8 exceeds the Football threshold of 3', 'Cleared', '2026-11-14 08:03:40'),
(3, 3, 'R2', 'Margin 47 exceeds the Netball threshold of 15', 'Flagged', '2026-11-14 13:04:40');

-- --------------------------------------------------------

--
-- Table structure for table `lineup`
--

CREATE TABLE `lineup` (
  `fixture_id` int(11) NOT NULL,
  `faculty_id` int(11) NOT NULL,
  `reg_no` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `result`
--

CREATE TABLE `result` (
  `fixture_id` int(11) NOT NULL,
  `home_score` int(11) NOT NULL,
  `away_score` int(11) NOT NULL,
  `referee` varchar(100) DEFAULT NULL,
  `forfeit_faculty_id` int(11) DEFAULT NULL,
  `entered_by` varchar(50) DEFAULT NULL,
  `entered_at` timestamp NULL DEFAULT current_timestamp(),
  `locked` tinyint(1) NOT NULL DEFAULT 0,
  `last_edited_by` varchar(50) DEFAULT NULL,
  `last_edited_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `result`
--

INSERT INTO `result` (`fixture_id`, `home_score`, `away_score`, `referee`, `forfeit_faculty_id`, `entered_by`, `entered_at`, `locked`, `last_edited_by`, `last_edited_at`) VALUES
(1, 40, 0, 'Okello', NULL, 'admin', '2026-11-14 07:56:18', 0, NULL, NULL),
(2, 9, 1, NULL, NULL, 'admin', '2026-11-14 08:03:40', 0, NULL, NULL),
(3, 3, 50, 'Arafat', NULL, 'admin', '2026-11-14 13:04:40', 0, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `result_audit`
--

CREATE TABLE `result_audit` (
  `id` int(11) NOT NULL,
  `fixture_id` int(11) NOT NULL,
  `changed_by` varchar(50) NOT NULL,
  `changed_at` timestamp NULL DEFAULT current_timestamp(),
  `old_values` varchar(255) DEFAULT NULL,
  `new_values` varchar(255) DEFAULT NULL,
  `reason` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `roster`
--

CREATE TABLE `roster` (
  `faculty_id` int(11) NOT NULL,
  `reg_no` varchar(20) NOT NULL,
  `full_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `roster`
--

INSERT INTO `roster` (`faculty_id`, `reg_no`, `full_name`) VALUES
(1, '2025-08-45435', 'YIGA JAMES'),
(1, '2025-08-45566', 'KATA JAMES'),
(3, '2025-08-34545', 'KISA JOHN');

-- --------------------------------------------------------

--
-- Table structure for table `sport`
--

CREATE TABLE `sport` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `margin_threshold` int(11) NOT NULL DEFAULT 10
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `sport`
--

INSERT INTO `sport` (`id`, `name`, `margin_threshold`) VALUES
(1, 'Football', 3),
(2, 'Netball', 15),
(3, 'Basketball', 25);

-- --------------------------------------------------------

--
-- Table structure for table `status_history`
--

CREATE TABLE `status_history` (
  `id` int(11) NOT NULL,
  `flag_id` int(11) NOT NULL,
  `status` enum('Flagged','Under Investigation','Cleared','Penalized') NOT NULL,
  `changed_by` varchar(50) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `changed_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `status_history`
--

INSERT INTO `status_history` (`id`, `flag_id`, `status`, `changed_by`, `note`, `changed_at`) VALUES
(1, 2, 'Cleared', 'admin', NULL, '2026-11-14 13:06:13');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `app_user`
--
ALTER TABLE `app_user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `faculty`
--
ALTER TABLE `faculty`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`);

--
-- Indexes for table `fixture`
--
ALTER TABLE `fixture`
  ADD PRIMARY KEY (`id`),
  ADD KEY `sport_id` (`sport_id`),
  ADD KEY `home_faculty_id` (`home_faculty_id`),
  ADD KEY `away_faculty_id` (`away_faculty_id`);

--
-- Indexes for table `flag`
--
ALTER TABLE `flag`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fixture_id` (`fixture_id`);

--
-- Indexes for table `lineup`
--
ALTER TABLE `lineup`
  ADD PRIMARY KEY (`fixture_id`,`faculty_id`,`reg_no`),
  ADD KEY `faculty_id` (`faculty_id`);

--
-- Indexes for table `result`
--
ALTER TABLE `result`
  ADD PRIMARY KEY (`fixture_id`),
  ADD KEY `forfeit_faculty_id` (`forfeit_faculty_id`);

--
-- Indexes for table `result_audit`
--
ALTER TABLE `result_audit`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fixture_id` (`fixture_id`);

--
-- Indexes for table `roster`
--
ALTER TABLE `roster`
  ADD PRIMARY KEY (`faculty_id`,`reg_no`);

--
-- Indexes for table `sport`
--
ALTER TABLE `sport`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `status_history`
--
ALTER TABLE `status_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `flag_id` (`flag_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `app_user`
--
ALTER TABLE `app_user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `faculty`
--
ALTER TABLE `faculty`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `fixture`
--
ALTER TABLE `fixture`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `flag`
--
ALTER TABLE `flag`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `result_audit`
--
ALTER TABLE `result_audit`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `sport`
--
ALTER TABLE `sport`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `status_history`
--
ALTER TABLE `status_history`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `fixture`
--
ALTER TABLE `fixture`
  ADD CONSTRAINT `fixture_ibfk_1` FOREIGN KEY (`sport_id`) REFERENCES `sport` (`id`),
  ADD CONSTRAINT `fixture_ibfk_2` FOREIGN KEY (`home_faculty_id`) REFERENCES `faculty` (`id`),
  ADD CONSTRAINT `fixture_ibfk_3` FOREIGN KEY (`away_faculty_id`) REFERENCES `faculty` (`id`);

--
-- Constraints for table `flag`
--
ALTER TABLE `flag`
  ADD CONSTRAINT `flag_ibfk_1` FOREIGN KEY (`fixture_id`) REFERENCES `fixture` (`id`);

--
-- Constraints for table `lineup`
--
ALTER TABLE `lineup`
  ADD CONSTRAINT `lineup_ibfk_1` FOREIGN KEY (`fixture_id`) REFERENCES `fixture` (`id`),
  ADD CONSTRAINT `lineup_ibfk_2` FOREIGN KEY (`faculty_id`) REFERENCES `faculty` (`id`);

--
-- Constraints for table `result`
--
ALTER TABLE `result`
  ADD CONSTRAINT `result_ibfk_1` FOREIGN KEY (`fixture_id`) REFERENCES `fixture` (`id`),
  ADD CONSTRAINT `result_ibfk_2` FOREIGN KEY (`forfeit_faculty_id`) REFERENCES `faculty` (`id`);

--
-- Constraints for table `result_audit`
--
ALTER TABLE `result_audit`
  ADD CONSTRAINT `result_audit_ibfk_1` FOREIGN KEY (`fixture_id`) REFERENCES `fixture` (`id`);

--
-- Constraints for table `roster`
--
ALTER TABLE `roster`
  ADD CONSTRAINT `roster_ibfk_1` FOREIGN KEY (`faculty_id`) REFERENCES `faculty` (`id`);

--
-- Constraints for table `status_history`
--
ALTER TABLE `status_history`
  ADD CONSTRAINT `status_history_ibfk_1` FOREIGN KEY (`flag_id`) REFERENCES `flag` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
