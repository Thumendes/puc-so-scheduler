# Simulador de Escalonamento de Processos

**Disciplina:** Sistemas Operacionais  
**Instituição:** PUC Minas

**Integrantes:**
- Arthur Mendes
- Henrique Caldeira
- Guilherme Sabino
- Vinicius Sena

---

## Requisitos

- Java 25 ou superior

---

## Compilação

A partir da raiz do projeto:

```bash
javac --enable-preview --release 25 -d bin src/*.java src/model/*.java src/metrics/*.java src/scheduler/*.java
```

---

## Execução

```bash
java --enable-preview -cp bin Main
```

O simulador lê o arquivo `processos.txt` na raiz do projeto, executa os quatro algoritmos e imprime o relatório comparativo.

---

## Formato do arquivo de entrada

Cada linha representa um processo:

```
PID;arrivalTime;burstTotal;prioridade;instantes_de_io
```

| Campo | Descrição |
|---|---|
| `PID` | Identificador único do processo |
| `arrivalTime` | Instante de chegada (ms) |
| `burstTotal` | Tempo total de CPU necessário (ms) |
| `prioridade` | `1` = alta (Fila 1 do MLQ) · `2` = baixa (Fila 2 do MLQ) |
| `instantes_de_io` | Instantes de **CPU acumulada** que disparam I/O, separados por vírgula. Usar `-1` para processos sem I/O |

Cada operação de I/O bloqueia o processo por **5 ms fixos**.

### Exemplo

```
1;0;8;1;-1
2;0;35;2;-1
3;0;22;1;4,12
4;5;40;2;15
```

- `P1`: chega em t=0, 8 ms de CPU, prioridade alta, sem I/O
- `P2`: chega em t=0, 35 ms de CPU, prioridade baixa, sem I/O
- `P3`: chega em t=0, 22 ms de CPU, prioridade alta — faz I/O ao acumular 4 ms e 12 ms de CPU
- `P4`: chega em t=5, 40 ms de CPU, prioridade baixa — faz I/O ao acumular 15 ms de CPU

---

## Algoritmos implementados

### FCFS — First Come, First Served
Não-preemptivo. Executa na ordem de chegada (desempate pelo PID). Simples e previsível, mas pode gerar alto tempo de espera para processos curtos precedidos por longos (efeito convoy).

### SRTF — Shortest Remaining Time First
Preemptivo. A cada chegada ou desbloqueio de I/O, o processo com menor tempo restante de CPU recebe o processador. Minimiza o tempo médio de espera, mas pode causar inanição de processos longos.

### Round-Robin com Quantum por Predição
Preemptivo, FIFO. O quantum é igual ao **menor τ** entre os processos na fila de prontos, atualizado por média exponencial após cada surto: `τ = 0.5 × t_real + 0.5 × τ_anterior` (τ₀ = 10 ms). Adapta o quantum ao comportamento histórico de cada processo.

### MLQ — Multilevel Queue
Duas filas fixas por prioridade:
- **Fila 1 (prioridade 1):** Round-Robin com quantum fixo de 4 ms
- **Fila 2 (prioridade 2):** FCFS

A Fila 2 só executa quando a Fila 1 está vazia. Processos de prioridade 1 preemptam processos de prioridade 2 em execução.
