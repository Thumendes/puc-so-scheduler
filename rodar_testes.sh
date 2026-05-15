#!/usr/bin/env bash
# Compila e roda todos os cenários de teste, salvando saída em resultados/

set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

echo "==> Compilando..."
mkdir -p bin
javac --enable-preview --release 25 -d bin \
  src/*.java src/model/*.java src/metrics/*.java src/scheduler/*.java
echo "    OK"
echo ""

mkdir -p resultados

rodar() {
    local label="$1"
    local arquivo="$2"
    local saida="resultados/${label}.txt"

    cp "$arquivo" processos.txt
    echo "┌── $label ──────────────────────────────"
    java --enable-preview -cp bin Main | tee "$saida"
    echo ""
}

# Backup do arquivo principal
cp processos.txt processos.txt.bak

echo "====================================================="
echo " ARQUIVO PRINCIPAL"
echo "====================================================="
rodar "principal" processos.txt.bak

echo "====================================================="
echo " CASOS EXTREMOS"
echo "====================================================="

# t1 — todos chegam ao mesmo tempo
echo ">>> t1: todos chegam em t=0"
echo "    Esperado: FCFS executa na ordem PID; SRTF prioriza menores bursts;"
echo "    RR alterna com quantum = min(tau); MLQ: fila1 antes da fila2"
rodar "t1_todos_ao_mesmo_tempo" testes/t1_todos_ao_mesmo_tempo.txt

# t2 — nenhum processo faz I/O
echo ">>> t2: sem I/O"
echo "    Esperado: ioTime=0 para todos; waitingTime = turnaround - burstTotal"
rodar "t2_sem_io" testes/t2_sem_io.txt

# t3 — I/O quase a cada instante
echo ">>> t3: I/O intensivo"
echo "    Esperado: alto ioTime; processos passam a maior parte bloqueados;"
echo "    throughput baixo"
rodar "t3_io_intensivo" testes/t3_io_intensivo.txt

# t4 — processo único
echo ">>> t4: processo único"
echo "    Esperado: waitingTime=0 em todos os schedulers;"
echo "    turnaround = burstTotal + ioTime; throughput = 1 / totalTime"
rodar "t4_processo_unico" testes/t4_processo_unico.txt

# t5 — bursts muito curtos misturados com muito longos
echo ">>> t5: burst misto (1-2ms vs 55-60ms)"
echo "    Esperado: SRTF fortemente favorece curtos; FCFS prejudica curtos"
echo "    se chegarem depois de um longo"
rodar "t5_burst_misto" testes/t5_burst_misto.txt

# t6 — processo chega depois de todos já terem terminado
echo ">>> t6: chegada tardia (idle time)"
echo "    Esperado: relógio avança em idle de t=18 até t=100;"
echo "    throughput de P3 calculado sobre totalTime maior"
rodar "t6_chegada_tardia" testes/t6_chegada_tardia.txt

# Restaura arquivo principal
cp processos.txt.bak processos.txt
rm processos.txt.bak

echo "====================================================="
echo " Resultados salvos em resultados/"
ls resultados/
