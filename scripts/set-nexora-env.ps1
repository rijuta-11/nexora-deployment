# ============================================================
# Nexora Environment Variables — Database Credentials
# Run this in your terminal BEFORE starting the backend.
# Usage:  . .\scripts\set-nexora-env.ps1
#
# This file must NEVER be committed to Git.
# It is already listed in .gitignore (see below).
# ============================================================

# --- Java 17 & Maven (from previous step) ---
$env:JAVA_HOME  = "C:\Users\HP\jdk17\jdk-17.0.21.1+2"
$env:MAVEN_HOME = "C:\Users\HP\.m2\wrapper\dists\apache-maven-3.9.12-bin\59fe215c0ad6947fea90184bf7add084544567b927287592651fda3782e0e798"
$env:PATH       = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"

# --- Database (fill in your nexora_user password below) ---
$env:SPRING_DATASOURCE_URL      = "jdbc:mysql://localhost:3306/nexora?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true"
$env:SPRING_DATASOURCE_USERNAME = "nexora_user"
$env:SPRING_DATASOURCE_PASSWORD = "REPLACE_WITH_YOUR_NEXORA_USER_PASSWORD"

Write-Host ""
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  Nexora Environment Ready" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  JAVA_HOME  = $env:JAVA_HOME" -ForegroundColor Green
Write-Host "  DB URL     = $env:SPRING_DATASOURCE_URL" -ForegroundColor Green
Write-Host "  DB User    = $env:SPRING_DATASOURCE_USERNAME" -ForegroundColor Green
Write-Host "  DB Pass    = *** (set, not shown)" -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "To start the backend:" -ForegroundColor Yellow
Write-Host "  mvn spring-boot:run" -ForegroundColor Yellow
Write-Host ""
