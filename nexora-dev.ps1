# ============================================================
# Nexora Dev Environment Setup Script
# Sets JDK 17 + Maven 3.9.12 for this terminal session only.
# Does NOT touch system-wide or JDK 25 settings.
# Usage: . .\nexora-dev.ps1   (dot-source it to keep the env)
# ============================================================

$JDK17  = "C:\Users\HP\jdk17\jdk-17.0.21.1+2"
$MVN    = "C:\Users\HP\.m2\wrapper\dists\apache-maven-3.9.12-bin\59fe215c0ad6947fea90184bf7add084544567b927287592651fda3782e0e798"

$env:JAVA_HOME  = $JDK17
$env:MAVEN_HOME = $MVN
$env:PATH       = "$JDK17\bin;$MVN\bin;$env:PATH"

Write-Host ""
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "  Nexora Dev Environment Active" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "  JAVA_HOME  = $env:JAVA_HOME" -ForegroundColor Green
Write-Host "  MAVEN_HOME = $env:MAVEN_HOME" -ForegroundColor Green
Write-Host ""
java -version
Write-Host ""
mvn -version
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "To start the backend:  .\mvnw.cmd spring-boot:run" -ForegroundColor Yellow
Write-Host "   OR directly:  mvn spring-boot:run" -ForegroundColor Yellow
Write-Host ""
