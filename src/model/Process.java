package model;

import java.util.List;

/**
 * Representa a abstração de um processo no Sistema Operacional.
 * Contém dados de entrada, estado de execução e métricas de desempenho.
 */
public class Process {
    // --- Dados de entrada (imutáveis após criação conforme enunciado) ---
    
   
    public final int pid;
    
    public final int arrivalTime;
    
    public final int burstTotal;
     
    public final int priority;
    
    public final List<Integer> ioInstants;

    // --- Estado dinâmico (modificado pelos schedulers durante a simulação) ---
    

    public int remainingBurst;
    
    public int cpuAccumulated;
    
    public State state;

    // --- Métricas coletadas durante a simulação para o relatório final ---
    

    public int startTime = -1;
    

    public int completionTime = -1;
    

    public int waitingTime = 0;
    

    public int ioTime = 0; 

    /**
     * Estados possíveis de um processo no simulador.
     */
    public enum State {
        READY,    
        RUNNING,  
        BLOCKED,  
        DONE      
    }

    /**
     * Construtor que inicializa o processo com os dados do arquivo.
     * Define o burst restante inicial como o burst total e o estado como READY.
     */
    public Process(int pid, int arrivalTime, int burstTotal, int priority, List<Integer> ioInstants) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTotal = burstTotal;
        this.priority = priority;
        this.ioInstants = ioInstants;
        this.remainingBurst = burstTotal;
        this.state = State.READY;
    }

    /**
     * Retorna uma cópia limpa do processo.
     * Essencial para que cada algoritmo de escalonamento (FCFS, SRTF, etc.) 
     * rode de forma independente no Main sem herdar estados de simulações anteriores.
     */
    public Process copy() {
        return new Process(pid, arrivalTime, burstTotal, priority, ioInstants);
    }

    @Override
    public String toString() {
        return "P" + pid;
    }
}