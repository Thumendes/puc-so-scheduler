package scheduler;

import model.Process;
import metrics.Metrics;
import java.util.*;

public class FCFS extends Scheduler {

    public FCFS() {
        super("FCFS");
    }

    @Override
    public Metrics simulate(List<Process> processes) {
        List<Process> pool = new ArrayList<>(processes);
        pool.sort(Comparator.comparingInt((Process p) -> p.arrivalTime)
                            .thenComparingInt(p -> p.pid));

        int clock = 0;
        List<Process> finished = new ArrayList<>();

        // Fila de chegada (já ordenada por arrivalTime, pid)
        Queue<Process> incoming = new LinkedList<>(pool);

        // Fila de prontos (FIFO — ordem de entrada é a ordem de execução)
        Queue<Process> ready = new LinkedList<>();

        // Processos bloqueados: processo -> instante de desbloqueio
        Map<Process, Integer> blocked = new HashMap<>();

        while (finished.size() < pool.size()) {
            admitArrivals(incoming, ready, clock);
            admitUnblocked(blocked, ready, clock);

            if (ready.isEmpty()) {
                clock = nextEvent(incoming, blocked);
                continue;
            }

            Process p = ready.poll();

            if (p.startTime == -1) {
                p.startTime = clock;
            }

            int runFor = nextRunLength(p);
            clock += runFor;
            p.cpuAccumulated += runFor;
            p.remainingBurst -= runFor;

            // Admite processos que chegaram/desbloquearam durante a execução
            admitArrivals(incoming, ready, clock);
            admitUnblocked(blocked, ready, clock);

            if (p.remainingBurst == 0) {
                p.completionTime = clock;
                p.state = Process.State.DONE;
                finished.add(p);
            } else {
                // Atingiu instante de I/O
                p.state = Process.State.BLOCKED;
                p.ioTime += IO_DURATION;
                blocked.put(p, clock + IO_DURATION);
            }
        }

        // waitingTime = turnaround - tempo de CPU - tempo bloqueado em I/O
        for (Process p : finished) {
            p.waitingTime = (p.completionTime - p.arrivalTime) - p.burstTotal - p.ioTime;
        }

        int totalTime = finished.stream().mapToInt(p -> p.completionTime).max().orElse(0);
        return new Metrics(name, finished, totalTime);
    }

    // Move processos que já chegaram de incoming para ready
    private void admitArrivals(Queue<Process> incoming, Queue<Process> ready, int clock) {
        while (!incoming.isEmpty() && incoming.peek().arrivalTime <= clock) {
            ready.add(incoming.poll());
        }
    }

    // Move processos desbloqueados para o fim da fila de prontos (ordenados por pid em caso de empate)
    private void admitUnblocked(Map<Process, Integer> blocked, Queue<Process> ready, int clock) {
        List<Process> unblocked = new ArrayList<>();
        Iterator<Map.Entry<Process, Integer>> it = blocked.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Process, Integer> e = it.next();
            if (e.getValue() <= clock) {
                unblocked.add(e.getKey());
                it.remove();
            }
        }
        unblocked.sort(Comparator.comparingInt(p -> p.pid));
        ready.addAll(unblocked);
    }

    // Avança o relógio até o próximo evento relevante (chegada ou desbloqueio)
    private int nextEvent(Queue<Process> incoming, Map<Process, Integer> blocked) {
        int t = Integer.MAX_VALUE;
        if (!incoming.isEmpty()) t = Math.min(t, incoming.peek().arrivalTime);
        for (int unblockTime : blocked.values()) t = Math.min(t, unblockTime);
        return t;
    }

    // Calcula quanto tempo o processo roda até o próximo I/O ou até terminar.
    // ioInstants com valor -1 (sem I/O) são ignorados naturalmente: -1 nunca é > cpuAccumulated.
    private int nextRunLength(Process p) {
        for (int instant : p.ioInstants) {
            if (instant > p.cpuAccumulated) {
                return instant - p.cpuAccumulated;
            }
        }
        return p.remainingBurst;
    }
}
