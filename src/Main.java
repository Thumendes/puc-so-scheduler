import java.io.File;
import java.util.List;

import model.Process;
import metrics.Metrics;
import scheduler.Scheduler;
import scheduler.FCFS;
import scheduler.SRTF;
import scheduler.RoundRobin;
import scheduler.MLQ;

public class Main {

    private static final String INPUT_FILE = "processos.txt";

    public static void main(String[] args) {
        if (!new File(INPUT_FILE).exists()) {
            IO.println("Erro: arquivo nao encontrado: " + INPUT_FILE);
            System.exit(1);
        }

        List<Process> processes = FileParser.parse(INPUT_FILE);

        if (processes == null) {
            IO.println("Erro: falha ao ler o arquivo (verifique o formato).");
            System.exit(1);
        }

        if (processes.isEmpty()) {
            IO.println("Erro: nenhum processo encontrado no arquivo.");
            System.exit(1);
        }

        List<Scheduler> schedulers = List.of(
            new FCFS(),
            new SRTF(),
            new RoundRobin(),
            new MLQ()
        );

        IO.println("Simulacao com " + processes.size() + " processos\n");

        for (Scheduler scheduler : schedulers) {
            List<Process> copy = processes.stream()
                .map(Process::copy)
                .toList();

            Metrics metrics = scheduler.simulate(copy);
            metrics.print();
            IO.println("");
        }
    }
}
