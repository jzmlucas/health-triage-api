@echo off
title Health Triage - Teste de sintomas
cd /d "%~dp0"
powershell.exe -NoExit -File "%~dp0scripts\demo-interactive.ps1"
