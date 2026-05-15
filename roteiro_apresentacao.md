# Roteiro de Apresentação — Trabalho Prático 1

**Disciplina:** Sistemas Operacionais · Prof. João Pedro O. Batisteli  
**Grupo:** Arthur Mendes · Henrique Caldeira · Guilherme Sabino · Vinicius Sena

> O professor sorteia **2 alunos** na hora. Qualquer dupla deve conseguir cobrir tudo.  
> Tempo estimado: **12–15 minutos** + perguntas.

---

## Linha do tempo

### ▸ 0:00 – 1:00 · Pessoa A — Abertura

> *"Nosso trabalho implementa um simulador de escalonamento de CPU em Java 25.
> Simulamos quatro algoritmos — FCFS, SRTF, Round-Robin com quantum adaptativo e MLQ —
> sobre o mesmo conjunto de processos e comparamos as três métricas exigidas:
> tempo de espera médio, turnaround médio e throughput."*

- Mencionar: grupos de até 4 alunos, linguagem Java 25, arquivo `processos.txt`.

---

### ▸ 1:00 – 3:00 · Pessoa B — Arquitetura do projeto

**Mostrar a estrutura de pastas no terminal ou IDE.**

> *"A arquitetura é baseada em uma classe abstrata `Scheduler`, que cada algoritmo estende
> implementando o método `simulate()`. Isso garante que Main.java trate todos os quatro
> da mesma forma, passando cópias independentes dos processos para cada um."*

Pontos a cobrir:
- `Scheduler.java` → contrato comum + constante `IO_DURATION = 5`
- `Process.java` → campos imutáveis de entrada vs. estado dinâmico (remainingBurst, cpuAccumulated, state) vs. métricas coletadas
- `FileParser.java` → lê `processos.txt`, formato `PID;Chegada;Burst;Prioridade;InstantesIO`
- `Metrics.java` → recebe lista de processos finalizados, calcula as 3 métricas
- `Main.java` → arquivo hardcoded, cria cópias via `Process.copy()`, roda os 4 em sequência

> *"O `Process.copy()` é importante: cada scheduler parte de um estado zerado,
> sem interferência entre simulações."*

---

### ▸ 3:00 – 5:30 · Pessoa A — FCFS e SRTF

**FCFS:**
> *"O FCFS é não-preemptivo e é nossa implementação de referência. Usamos simulação
> orientada a eventos: o relógio pula direto para o próximo evento — chegada, I/O ou
> conclusão — sem avançar tick a tick. A fila de prontos é FIFO puro."*

- Mostrar o método `nextRunLength()`: encontra até onde o processo pode rodar sem I/O
- Mostrar `admitArrivals` / `admitUnblocked`: o padrão que todos os schedulers seguem
- `waitingTime = turnaround − burstTotal − ioTime` (calculado ao final, não acumulado)

**SRTF:**
> *"O SRTF é preemptivo. Usamos uma `PriorityQueue` ordenada por `remainingBurst`.
> A preempção acontece orientada a eventos: limitamos `runFor` pelo próximo evento
> de chegada ou desbloqueio. Depois desse evento, a fila reseleciona o menor burst.
> Se o processo que chegou tem burst maior, o mesmo processo é selecionado de novo —
> o overhead é mínimo e o resultado é correto."*

---

### ▸ 5:30 – 8:00 · Pessoa B — Round-Robin e MLQ

**Round-Robin:**
> *"Implementamos o Round-Robin com quantum dinâmico por média exponencial,
> conforme o enunciado. Cada processo tem seu próprio τ, iniciado em 10 ms.
> A fórmula é `τ_novo = 0.5 × t_real + 0.5 × τ_anterior`.
> O quantum da fatia é o menor τ entre os processos na fila de prontos —
> processos interativos (I/O frequente, surtos curtos) reduzem seu τ rápido
> e passam a receber quanta menores, aumentando a responsividade."*

- Mostrar `computeQuantum()` e a atualização de τ após cada surto

**MLQ:**
> *"O MLQ tem duas filas estáticas. Fila 1 (prioridade 1) usa Round-Robin com
> quantum fixo de 4 ms. Fila 2 (prioridade 2) usa FCFS.
> A Fila 1 tem precedência absoluta: se um processo de prioridade 1 chegar ou
> desbloquear enquanto a Fila 2 está rodando, o processo de Fila 2 é interrompido
> e volta ao início da fila para continuar depois."*

- Mostrar o bloco `queue2`: como calculamos `runFor` limitado por chegadas/desbloqueios de prioridade 1
- `queue2.addFirst(p)` para retornar ao início sem perder progresso

---

### ▸ 8:00 – 10:30 · Pessoa A — Resultados

**Rodar o simulador ao vivo:**
```bash
java --enable-preview -cp bin Main
```

> *"Com 12 processos variados — chegadas simultâneas, sem I/O, múltiplos I/Os,
> prioridades mistas e bursts de 6 a 50 ms — o SRTF foi o melhor em espera média:
> 78 ms contra 130 ms do FCFS, uma redução de 40%."*

**Mostrar o gráfico `1_principal_comparativo.png`.**

> *"Um resultado contraintuitivo foi o cenário de I/O intensivo: o SRTF perdeu
> para o FCFS. Com I/O a cada 1-2 ms, os dois processos têm bursts restantes
> sempre similares, então as preempções do SRTF não trazem ganho e adicionam overhead."*

**Mostrar o gráfico `2_espera_por_cenario.png`** e apontar o t3.

---

### ▸ 10:30 – 12:00 · Pessoa B — Análise e conclusão

**Mostrar o radar `4_radar_normalizado.png`.**

> *"O radar normaliza as três métricas e mostra a área de cada algoritmo.
> O SRTF tem a maior área — melhor desempenho geral. O RR é o menor porque
> o τ₀ = 10 ms força um quantum alto no início, antes do algoritmo aprender
> o perfil dos processos. Em simulações mais longas, o RR tende a melhorar."*

Síntese:
- SRTF → menor espera, mas risco de inanição para processos longos
- RR → mais justo, melhora com o tempo
- MLQ → garantia para alta prioridade, mas Fila 2 pode sofrer inanição
- FCFS → simples, sem overhead, adequado para cargas homogêneas

> *"Para o professor: podemos executar qualquer um dos cenários de teste
> individualmente com `bash rodar_testes.sh` e mostrar os resultados salvos em `resultados/`."*

---

## Perguntas prováveis do professor — e respostas

| Pergunta | O que responder |
|---|---|
| "Por que o SRTF perdeu para o FCFS no t3?" | I/O frequente deixa os bursts restantes similares — preempção não ajuda e gera overhead |
| "Como o I/O é tratado?" | Cada instante de I/O é um valor de CPU acumulada. Quando `cpuAccumulated` atinge o valor, o processo vai para `blocked` por 5 ms fixos |
| "Por que usar simulação orientada a eventos e não tick a tick?" | Evita O(totalTime) iterações — para bursts de 50 ms seria 50 ticks sem fazer nada. Salta direto para o próximo evento |
| "O que é o sentinel -1 no processos.txt?" | O FileParser sempre espera o campo de I/O. Usamos -1 para processos sem I/O: `-1 > cpuAccumulated` nunca é verdadeiro, então é ignorado naturalmente |
| "Como vocês garantem que cada scheduler não interfere no outro?" | `Process.copy()` cria um novo objeto com estado zerado — `remainingBurst`, `cpuAccumulated`, `startTime` etc. voltam ao valor inicial |
| "Por que o quantum do RR é o menor τ e não a média?" | O enunciado especifica o menor τ. O objetivo é que processos com surtos curtos previstos terminem sem interrupção, aumentando a responsividade |
| "O MLQ pode causar inanição?" | Sim — se a Fila 1 nunca esvaziar, processos de prioridade 2 nunca executam. O enunciado não pede aging, então não implementamos |
| "Como o waitingTime é calculado?" | `waitingTime = (completionTime - arrivalTime) - burstTotal - ioTime` — desconta do turnaround o tempo efetivamente útil (CPU + I/O bloqueado) |
| "Por que o RR teve pior espera que o FCFS no arquivo principal?" | τ₀ = 10 ms para todos faz o quantum inicial ser alto. Com 12 processos diversificados, há muitas trocas desnecessárias antes do τ convergir |

---

## Glossário — conceitos da matéria

### Burst (surto de CPU)
É o período contínuo em que um processo usa a CPU sem interrupção. Todo processo alterna entre fases de CPU e fases de I/O — um burst é uma dessas fases de CPU. O `burstTotal` no nosso simulador é a soma de todos os bursts que o processo vai precisar ao longo da sua vida.

Exemplo: processo com `burstTotal = 22` e I/O em `cpu = 4` e `cpu = 12` tem três bursts:
- Burst 1: 4 ms (até o 1º I/O)
- Burst 2: 8 ms (entre o 1º e o 2º I/O)
- Burst 3: 10 ms (do 2º I/O até terminar)

---

### I/O (Entrada e Saída) — por que importa no escalonamento?
I/O é qualquer operação que não usa a CPU: ler disco, esperar rede, aguardar teclado. Durante o I/O, o processo fica **bloqueado** — ele não precisa da CPU e não deveria ocupar a fila de prontos.

Isso é crítico para o escalonador porque:
- Um processo bloqueado em I/O não deve segurar a CPU → outro processo deve assumir.
- Processos que fazem muito I/O tendem a ter bursts de CPU curtos → schedulers inteligentes os reconhecem e os tratam de forma diferente (como o RR com τ adaptativo).
- No nosso simulador, cada I/O bloqueia o processo por **5 ms fixos**, independente do tipo de operação.

---

### Preemptivo vs. Não-preemptivo
- **Não-preemptivo (FCFS):** uma vez que o processo começa a rodar, ele fica na CPU até terminar ou fazer I/O. Nenhum outro processo pode "tirar" a CPU dele no meio.
- **Preemptivo (SRTF, RR, MLQ-fila1):** o processo pode ser interrompido antes de terminar, se outro processo com maior prioridade ou menor burst restante aparecer. A CPU é tomada de volta pelo sistema operacional.

A preempção aumenta a responsividade (processos urgentes não ficam presos atrás de longos), mas gera **overhead de troca de contexto** — salvar e restaurar o estado do processo tem custo.

---

### Simulação Tick a Tick vs. Orientada a Eventos

**Tick a tick:** o relógio avança 1 ms por vez. A cada tick, verifica-se o que acontece. Simples de implementar, mas ineficiente — um processo com burst de 50 ms gera 50 iterações sem nada relevante acontecer.

**Orientada a eventos:** o relógio pula direto para o próximo evento relevante — chegada de processo, término de I/O, fim de burst. Sem iterações vazias. É o que implementamos: `nextRunLength()` calcula exatamente até onde o processo pode rodar, e o relógio salta para lá.

```
Tick a tick:  t=0, t=1, t=2, ... t=49, t=50  →  50 passos
Orientada:    t=0 → t=4 (I/O) → t=9 (retorna) → ...  →  muito menos passos
```

---

### Quantum
É a fatia máxima de tempo que um processo pode usar a CPU em cada rodada no Round-Robin. Quando o quantum expira, o processo é interrompido e vai para o fim da fila — o próximo processo assume.

- Quantum **pequeno** → alta responsividade, mas muito overhead de troca de contexto.
- Quantum **grande** → o RR se comporta quase como FCFS (o processo roda até o fim antes de ser trocado).

No nosso RR, o quantum não é fixo — é o **menor τ** da fila, calculado dinamicamente.

---

### τ (tau) — média exponencial do burst
**Pronuncia-se "tau"** (como em "tao"). É a letra grega τ, usada convencionalmente em SO para representar a **previsão do próximo burst de CPU** de um processo.

A fórmula é:

```
τ_novo = α × t_real + (1 − α) × τ_anterior
```

Com α = 0.5 e τ₀ = 10 ms:

| Burst real | τ antes | τ depois |
|:----------:|:-------:|:--------:|
| 3 ms | 10,0 | 6,5 |
| 3 ms | 6,5 | 4,75 |
| 3 ms | 4,75 | 3,875 |
| 3 ms | 3,875 | ≈ 3,4 |

O τ converge para o burst real do processo. Um processo com bursts curtos e frequentes terá τ pequeno → recebe quanta menores → termina mais rápido, sem interrupção desnecessária.

O nome "média exponencial" vem do fato de que os bursts mais recentes têm peso maior (α) e os mais antigos vão sendo exponencialmente esquecidos.

---

### Turnaround (Tempo de Retorno)
Tempo total desde que o processo chegou até terminar completamente, incluindo espera na fila, uso de CPU e tempo bloqueado em I/O:

```
turnaround = completionTime − arrivalTime
```

É a métrica que o **usuário sente** — quanto tempo demorou desde que ele submeteu o processo até ter o resultado.

---

### Tempo de Espera (Waiting Time)
Tempo que o processo passou na fila de prontos, sem fazer nada útil — nem CPU nem I/O. É o turnaround menos o tempo produtivo:

```
waitingTime = turnaround − burstTotal − ioTime
```

É a métrica que avalia a **eficiência do escalonador** em si, independente do tamanho do processo.

---

### Throughput (Vazão)
Número de processos concluídos por unidade de tempo:

```
throughput = total de processos / tempo total simulado
```

Mede a **capacidade produtiva** do sistema. Um scheduler que gera muito overhead de troca de contexto pode ter throughput menor mesmo finalizando os processos em ordem similar.

---

## Dicas para a arguição

- **Não decorar código** — entender o fluxo. O professor vai apontar uma linha e perguntar o que ela faz.
- **Mostrar rodando** — ter o terminal com `java --enable-preview -cp bin Main` pronto.
- **Se não souber, dizer** — "não tenho certeza, mas meu raciocínio é..." vale mais que inventar.
- **Qualquer dupla pode cobrir tudo** — cada pessoa deve saber os 4 algoritmos, não só os 2 que apresentou.
