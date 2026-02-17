@echo off
setlocal enabledelayedexpansion

:: Nome del file di uscita
set "OUTPUT_FILE=repository_context.txt"

:: Elimina il file se esiste già per ripartire da zero
if exist "%OUTPUT_FILE%" del "%OUTPUT_FILE%"

echo Generazione file di contesto in corso...

echo ================================================== >> "%OUTPUT_FILE%"
echo STRUTTURA DEL REPOSITORY (Cartella src) >> "%OUTPUT_FILE%"
echo Data generazione: %date% %time% >> "%OUTPUT_FILE%"
echo ================================================== >> "%OUTPUT_FILE%"
echo. >> "%OUTPUT_FILE%"

:: Crea un piccolo indice iniziale della struttura cartelle
dir /s /b /a-d src >> "%OUTPUT_FILE%"
echo. >> "%OUTPUT_FILE%"
echo ================================================== >> "%OUTPUT_FILE%"
echo INIZIO CODICE SORGENTE >> "%OUTPUT_FILE%"
echo ================================================== >> "%OUTPUT_FILE%"
echo. >> "%OUTPUT_FILE%"

:: Ciclo per scansionare ricorsivamente tutti i file nelle sottocartelle di src
:: Puoi aggiungere altre estensioni se necessario (es. *.kt *.java *.txt)
for /r src %%F in (*.kt) do (
    echo [INIZIO FILE: %%~pnxF] >> "%OUTPUT_FILE%"
    echo [PERCORSO RELATIVO: %%~F] >> "%OUTPUT_FILE%"
    echo -------------------------------------------------- >> "%OUTPUT_FILE%"
    
    :: Copia il contenuto del file nel file di uscita
    type "%%F" >> "%OUTPUT_FILE%"
    
    echo. >> "%OUTPUT_FILE%"
    echo [FINE FILE: %%~pnxF] >> "%OUTPUT_FILE%"
    echo. >> "%OUTPUT_FILE%"
    echo ================================================== >> "%OUTPUT_FILE%"
    echo. >> "%OUTPUT_FILE%"
)

echo Operazione completata con successo!
echo Il file "%OUTPUT_FILE%" e pronto per essere caricato su AI Studio.
pause