package metrics;

import model.Process;
import java.util.List;

/**
 * Agrega e imprime as três métricas exigidas pelo enunciado para um scheduler.
 *
 * waitingTime de cada processo é calculado pelos schedulers pela fórmula:
 *   waitingTime = (completionTime - arrivalTime) - burstTotal - ioTime
 * que desconta do turnaround tanto o tempo de CPU quanto o tempo bloqueado em I/O.
 *
 * throughput = processos concluídos / tempo total simulado (do t=0 até o último completionTime).
 */
public class Metrics {

    public final String schedulerName;
    public final double avgWaitingTime;
    public final double avgTurnaround;
    public final double throughput;

    public Metrics(String schedulerName, List<Process> finished, int totalTime) {
        this.schedulerName = schedulerName;

        double totalWaiting = 0;
        double totalTurnaround = 0;

        for (Process p : finished) {
            totalTurnaround += p.completionTime - p.arrivalTime;
            totalWaiting += p.waitingTime;
        }

        int n = finished.size();
        this.avgWaitingTime = n > 0 ? totalWaiting / n : 0;
        this.avgTurnaround = n > 0 ? totalTurnaround / n : 0;
        this.throughput = totalTime > 0 ? (double) n / totalTime : 0;
    }

    public void print() {
        IO.println("=== " + schedulerName + " ===");
        IO.println("  Tempo de Espera Medio : " + String.format("%.2f ms", avgWaitingTime));
        IO.println("  Turnaround Medio      : " + String.format("%.2f ms", avgTurnaround));
        IO.println("  Throughput            : " + String.format("%.4f proc/ms", throughput));
    }
}