-- ============================================================
-- Nexora Database & Dedicated User Setup Script
-- Run this as root via:
--   mysql -u root -p < scripts\setup-nexora-db.sql
-- OR paste each block interactively after connecting.
-- ============================================================

-- Step 1: Check and create the nexora database
-- (createDatabaseIfNotExist=true in JDBC URL also does this,
--  but explicit creation lets us control charset.)
CREATE DATABASE IF NOT EXISTS `nexora`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

SELECT SCHEMA_NAME AS 'Database',
       DEFAULT_CHARACTER_SET_NAME AS 'Charset',
       DEFAULT_COLLATION_NAME AS 'Collation'
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = 'nexora';

-- Step 2: Create the dedicated user (replace ENTER_PASSWORD_HERE
--         with your chosen strong password when running manually)
-- NOTE: Do NOT run this file as-is — replace the placeholder!
-- CREATE USER IF NOT EXISTS 'nexora_user'@'localhost'
--   IDENTIFIED BY 'ENTER_PASSWORD_HERE';

-- Step 3: Grant permissions on nexora database only
-- GRANT ALL PRIVILEGES ON `nexora`.* TO 'nexora_user'@'localhost';
-- FLUSH PRIVILEGES;

-- Step 4: Verify grants
-- SHOW GRANTS FOR 'nexora_user'@'localhost';

-- Step 5: Verify the user can see the database (run as nexora_user)
-- SHOW DATABASES LIKE 'nexora';
