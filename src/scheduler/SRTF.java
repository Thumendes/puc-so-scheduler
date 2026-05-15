package scheduler;

import model.Process;
import metrics.Metrics;
import java.util.*;

/**
 * Escalonamento Shortest Remaining Time First (SRTF).
 * Política preemptiva que sempre entrega a CPU ao processo com o menor tempo restante.
 */
public class SRTF extends Scheduler {

    public SRTF() {
        super("SRTF");
    }

    @Override
    public Metrics simulate(List<Process> processes) {
        List<Process> pool = new ArrayList<>(processes);
        pool.sort(Comparator.comparingInt((Process p) -> p.arrivalTime)
                            .thenComparingInt(p -> p.pid));

        int clock = 0;
        List<Process> finished = new ArrayList<>();
        Queue<Process> incoming = new LinkedList<>(pool);

        // Seleção sempre pelo menor remainingBurst (empate: menor pid)
        PriorityQueue<Process> ready = new PriorityQueue<>(
            Comparator.comparingInt((Process p) -> p.remainingBurst)
                      .thenComparingInt(p -> p.pid)
        );

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

            // Roda até o próximo I/O ou conclusão, mas pára antes se chegar
            // um processo ou desbloquear um que possa ter burst menor (preempção).
            int runFor = nextRunLength(p);

            if (!incoming.isEmpty()) {
                runFor = Math.min(runFor, incoming.peek().arrivalTime - clock);
            }
            for (int unblockTime : blocked.values()) {
                runFor = Math.min(runFor, unblockTime - clock);
            }

            clock += runFor;
            p.cpuAccumulated += runFor;
            p.remainingBurst -= runFor;

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
                // Preemptado — volta à fila para reavaliação
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

    private void admitArrivals(Queue<Process> incoming, PriorityQueue<Process> ready, int clock) {
        while (!incoming.isEmpty() && incoming.peek().arrivalTime <= clock) {
            ready.add(incoming.poll());
        }
    }

    private void admitUnblocked(Map<Process, Integer> blocked, PriorityQueue<Process> ready, int clock) {
        Iterator<Map.Entry<Process, Integer>> it = blocked.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Process, Integer> e = it.next();
            if (e.getValue() <= clock) {
                ready.add(e.getKey());
                it.remove();
            }
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
