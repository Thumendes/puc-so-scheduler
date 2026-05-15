package scheduler;

import model.Process;
import metrics.Metrics;
import java.util.*;

public class MLQ extends Scheduler {

    // Quantum fixo para a Fila 1 (Alta Prioridade — Round-Robin)
    private static final int QUEUE1_QUANTUM = 4;

    public MLQ() {
        super("MLQ");
    }

    @Override
    public Metrics simulate(List<Process> processes) {
        List<Process> pool = new ArrayList<>(processes);
        pool.sort(Comparator.comparingInt((Process p) -> p.arrivalTime)
                            .thenComparingInt(p -> p.pid));

        int clock = 0;
        List<Process> finished = new ArrayList<>();
        Queue<Process> incoming = new LinkedList<>(pool);

        Deque<Process> queue1 = new ArrayDeque<>(); // priority=1 — Round-Robin
        Deque<Process> queue2 = new ArrayDeque<>(); // priority=2 — FCFS

        Map<Process, Integer> blocked = new HashMap<>();

        while (finished.size() < pool.size()) {
            admitArrivals(incoming, queue1, queue2, clock);
            admitUnblocked(blocked, queue1, queue2, clock);

            if (queue1.isEmpty() && queue2.isEmpty()) {
                clock = nextEvent(incoming, blocked);
                continue;
            }

            if (!queue1.isEmpty()) {
                // ── Fila 1: Round-Robin com quantum fixo ──────────────────────
                Process p = queue1.poll();
                if (p.startTime == -1) p.startTime = clock;

                int runFor = Math.min(QUEUE1_QUANTUM, nextRunLength(p));
                clock += runFor;
                p.cpuAccumulated += runFor;
                p.remainingBurst -= runFor;

                admitArrivals(incoming, queue1, queue2, clock);
                admitUnblocked(blocked, queue1, queue2, clock);

                if (p.remainingBurst == 0) {
                    p.completionTime = clock;
                    p.state = Process.State.DONE;
                    finished.add(p);
                } else if (hitsIO(p)) {
                    p.state = Process.State.BLOCKED;
                    p.ioTime += IO_DURATION;
                    blocked.put(p, clock + IO_DURATION);
                } else {
                    p.state = Process.State.READY;
                    queue1.addLast(p);
                }

            } else {
                // ── Fila 2: FCFS — interrompível por chegada/desbloqueio da Fila 1 ──
                Process p = queue2.poll();
                if (p.startTime == -1) p.startTime = clock;

                int runFor = nextRunLength(p);

                // Pára se um processo de prioridade 1 chegar (incoming está ordenado por arrivalTime)
                for (Process next : incoming) {
                    if (next.priority == 1) {
                        runFor = Math.min(runFor, next.arrivalTime - clock);
                        break;
                    }
                }
                // Pára se um processo de prioridade 1 desbloquear
                for (Map.Entry<Process, Integer> e : blocked.entrySet()) {
                    if (e.getKey().priority == 1) {
                        runFor = Math.min(runFor, e.getValue() - clock);
                    }
                }

                clock += runFor;
                p.cpuAccumulated += runFor;
                p.remainingBurst -= runFor;

                admitArrivals(incoming, queue1, queue2, clock);
                admitUnblocked(blocked, queue1, queue2, clock);

                if (p.remainingBurst == 0) {
                    p.completionTime = clock;
                    p.state = Process.State.DONE;
                    finished.add(p);
                } else if (hitsIO(p)) {
                    p.state = Process.State.BLOCKED;
                    p.ioTime += IO_DURATION;
                    blocked.put(p, clock + IO_DURATION);
                } else {
                    // Preemptado por Fila 1 — volta ao início da fila para continuar depois
                    p.state = Process.State.READY;
                    queue2.addFirst(p);
                }
            }
        }

        for (Process p : finished) {
            p.waitingTime = (p.completionTime - p.arrivalTime) - p.burstTotal - p.ioTime;
        }

        int totalTime = finished.stream().mapToInt(p -> p.completionTime).max().orElse(0);
        return new Metrics(name, finished, totalTime);
    }

    private void admitArrivals(Queue<Process> incoming, Deque<Process> q1, Deque<Process> q2, int clock) {
        while (!incoming.isEmpty() && incoming.peek().arrivalTime <= clock) {
            Process p = incoming.poll();
            if (p.priority == 1) q1.addLast(p);
            else q2.addLast(p);
        }
    }

    private void admitUnblocked(Map<Process, Integer> blocked, Deque<Process> q1, Deque<Process> q2, int clock) {
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
        for (Process p : unblocked) {
            if (p.priority == 1) q1.addLast(p);
            else q2.addLast(p);
        }
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
