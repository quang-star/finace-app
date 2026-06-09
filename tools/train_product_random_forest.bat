@echo off
cd /d "%~dp0.."
python tools\train_product_random_forest.py
if errorlevel 1 exit /b %errorlevel%

python tools\export_product_random_forest_java.py
if errorlevel 1 exit /b %errorlevel%
