package metrics;

import model.Process;
import java.util.List;

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
        IO.println(schedulerName);
        IO.println("  espera:     " + String.format("%.2f ms", avgWaitingTime));
        IO.println("  turnaround: " + String.format("%.2f ms", avgTurnaround));
        IO.println("  throughput: " + String.format("%.4f proc/ms", throughput));
    }
}