package scheduler;

import model.Process;
import metrics.Metrics;
import java.util.List;

public abstract class Scheduler {

    protected final String name;
    public static final int IO_DURATION = 5;

    protected Scheduler(String name) {
        this.name = name;
    }

    public abstract Metrics simulate(List<Process> processes);

    public String getName() {
        return name;
    }
}