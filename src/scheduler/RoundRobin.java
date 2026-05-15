package scheduler;

import model.Process;
import metrics.Metrics;
import java.util.*;

public class RoundRobin extends Scheduler {

    private static final double ALPHA = 0.5;
    private static final double TAU_0 = 10.0;

    public RoundRobin() {
        super("Round-Robin com Quantum por Predicao");
    }

    @Override
    public Metrics simulate(List<Process> processes) {
        List<Process> pool = new ArrayList<>(processes);
        pool.sort(Comparator.comparingInt((Process p) -> p.arrivalTime)
                            .thenComparingInt(p -> p.pid));

        int clock = 0;
        List<Process> finished = new ArrayList<>();
        Queue<Process> incoming = new LinkedList<>(pool);
        Queue<Process> ready = new LinkedList<>();
        Map<Process, Integer> blocked = new HashMap<>();

        // τ por processo — previsão do próximo surto de CPU (média exponencial)
        Map<Integer, Double> tau = new HashMap<>();
        for (Process p : pool) tau.put(p.pid, TAU_0);

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

            // Quantum = menor τ entre o processo atual e os demais na fila de prontos
            int quantum = computeQuantum(p, ready, tau);
            int runFor = Math.min(quantum, nextRunLength(p));

            clock += runFor;
            p.cpuAccumulated += runFor;
            p.remainingBurst -= runFor;

            // Atualiza τ com o surto real executado
            tau.put(p.pid, ALPHA * runFor + (1 - ALPHA) * tau.get(p.pid));

            admitArrivals(incoming, ready, clock);
            admitUnblocked(blocked, ready, clock);

            if (p.remainingBurst == 0) {
                p.completionTime = clock;
                p.state = Process.State.DONE;
                finished.add(p);
            } else if (hitsIO(p)) {
                p.state = Process.State.BLOCKED;
                p.ioTime += IO_DURATION;
                blocked.put(p, clock + IO_DURATION);
            } else {
                // Quantum expirou — vai ao fim da fila de prontos
                p.state = Process.State.READY;
                ready.add(p);
            }
        }

        for (Process p : finished) {
            p.waitingTime = (p.completionTime - p.arrivalTime) - p.burstTotal - p.ioTime;
        }

        int totalTime = finished.stream().mapToInt(p -> p.completionTime).max().orElse(0);
        return new Metrics(name, finished, totalTime);
    }

    // Quantum = teto do menor τ na fila (incluindo o processo atual), mínimo 1
    private int computeQuantum(Process current, Queue<Process> ready, Map<Integer, Double> tau) {
        double min = tau.get(current.pid);
        for (Process p : ready) min = Math.min(min, tau.get(p.pid));
        return Math.max(1, (int) Math.ceil(min));
    }

    private void admitArrivals(Queue<Process> incoming, Queue<Process> ready, int clock) {
        while (!incoming.isEmpty() && incoming.peek().arrivalTime <= clock) {
            ready.add(incoming.poll());
        }
    }

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
        unblocked.sort(Comparator.comparingInt(q -> q.pid));
        ready.addAll(unblocked);
    }

    private int nextEvent(Queue<Process> incoming, Map<Process, Integer> blocked) {
        int t = Integer.MAX_VALUE;
        if (!incoming.isEmpty()) t = Math.min(t, incoming.peek().arrivalTime);
        for (int unblockTime : blocked.values()) t = Math.min(t, unblockTime);
        return t;
    }

    private boolean hitsIO(Process p) {
        for (int instant : p.ioInstants) {
            if (instant == p.cpuAccumulated) return true;
        }
        return false;
    }

    private int nextRunLength(Process p) {
        for (int instant : p.ioInstants) {
            if (instant > p.cpuAccumulated) return instant - p.cpuAccumulated;
        }
        return p.remainingBurst;
    }
}
