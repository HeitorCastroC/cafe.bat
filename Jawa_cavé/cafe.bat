@echo off
chcp 65001 >nul
cd /d "%~dp0"

:: ── Verificar Java ─────────────────────────────────────────────────────────
where javac >nul 2>&1
if errorlevel 1 (
    echo.
    echo  ERRO: Java nao encontrado. Instale o JDK em https://adoptium.net
    pause
    exit /b 1
)

:: ── Converter planilha para CSV ────────────────────────────────────────────
:: Tenta Python 3 (comando "python" ou "python3")
set PYTHON_CMD=
where python >nul 2>&1  && set PYTHON_CMD=python
where python3 >nul 2>&1 && set PYTHON_CMD=python3

if "%PYTHON_CMD%"=="" (
    echo.
    echo  AVISO: Python nao encontrado. O CSV sera usado sem atualizacao.
    echo  Instale Python em https://python.org para carregar a planilha automaticamente.
    echo.
    goto :compilar
)

:: Verificar se openpyxl esta instalado
%PYTHON_CMD% -c "import openpyxl" >nul 2>&1
if errorlevel 1 (
    echo  Instalando openpyxl...
    %PYTHON_CMD% -m pip install openpyxl --quiet
)

:: Converter escala_cafe.xlsx -> escala_cafe.csv
if exist "escala_cafe.xlsx" (
    echo  Convertendo planilha...
    %PYTHON_CMD% -c ^
"import openpyxl, csv, sys; ^
wb = openpyxl.load_workbook('escala_cafe.xlsx', data_only=True); ^
ws = wb.active; ^
rows = list(ws.iter_rows(values_only=True)); ^
f = open('escala_cafe.csv', 'w', newline='', encoding='utf-8'); ^
w = csv.writer(f); ^
w.writerow(['nome','data','pacotes']); ^
[w.writerow([str(r[0]).strip(), str(r[1]).strip(), int(r[2]) if isinstance(r[2], (int,float)) else 0]) ^
 for r in rows[1:] if r[0] and str(r[0]).strip()]; ^
f.close(); ^
print('  OK: escala_cafe.csv atualizado.')"
    if errorlevel 1 (
        echo  AVISO: Falha ao converter a planilha. Usando CSV existente se disponivel.
    )
) else (
    echo  AVISO: escala_cafe.xlsx nao encontrado. Usando CSV existente se disponivel.
)

:compilar
:: ── Compilar se necessario ─────────────────────────────────────────────────
if not exist "Cafe.class" (
    echo  Compilando...
    javac -encoding UTF-8 --release 8 Cafe.java
    if errorlevel 1 (
        echo  ERRO na compilacao.
        pause
        exit /b 1
    )
) else (
    :: Recompilar se o fonte for mais novo que o .class
    for /f %%A in ('powershell -NoProfile -Command "(Get-Item Cafe.java).LastWriteTime -gt (Get-Item Cafe.class).LastWriteTime"') do (
        if "%%A"=="True" (
            echo  Recompilando (fonte atualizado)...
            javac -encoding UTF-8 --release 8 Cafe.java
            if errorlevel 1 (
                echo  ERRO na compilacao.
                pause
                exit /b 1
            )
        )
    )
)

:: ── Executar ───────────────────────────────────────────────────────────────
java -Dfile.encoding=UTF-8 Cafe
pause
