package model;

import java.util.List;

public class Process {
    // Dados de entrada (imutáveis após criação)
    public final int pid;
    public final int arrivalTime;
    public final int burstTotal;
    public final int priority;
    public final List<Integer> ioInstants; // instantes de CPU acumulada que disparam I/O

    // Estado dinâmico (modificado pelos schedulers)
    public int remainingBurst;
    public int cpuAccumulated;
    public State state;

    // Métricas coletadas durante a simulação
    public int startTime = -1;
    public int completionTime = -1;
    public int waitingTime = 0;
    public int ioTime = 0; // tempo total bloqueado em I/O

    public enum State {
        READY, RUNNING, BLOCKED, DONE
    }

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
     * Retorna uma cópia limpa do processo para cada scheduler rodar independente
     */
    public Process copy() {
        return new Process(pid, arrivalTime, burstTotal, priority, ioInstants);
    }

    @Override
    public String toString() {
        return "P" + pid;
    }
}