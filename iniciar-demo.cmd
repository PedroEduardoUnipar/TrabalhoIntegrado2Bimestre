@echo off
cd /d "%~dp0"
start "Loja - Backend" cmd /k "cd /d backend && mvnw.cmd spring-boot:run"
start "Loja - Frontend" cmd /k "cd /d frontend && npm ci && npm start"
echo Aguarde a inicializacao e abra http://localhost:4200
pause
