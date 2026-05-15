package scheduler;

import model.Process;
import metrics.Metrics;
import java.util.List;

/**
 * Classe base para todos os schedulers.
 * Cada subclasse implementa simulate() com sua própria política de escalonamento.
 * IO_DURATION é compartilhado: toda operação de I/O bloqueia o processo por 5 ms fixos.
 */
public abstract class Scheduler {

    protected final String name;

    // Duração fixa de cada operação de I/O, conforme especificado no enunciado
    public static final int IO_DURATION = 5;

    protected Scheduler(String name) {
        this.name = name;
    }

    /**
     * Executa a simulação completa sobre a lista de processos e retorna as métricas.
     * Cada chamada deve receber cópias independentes dos processos (via Process.copy()).
     */
    public abstract Metrics simulate(List<Process> processes);

    public String getName() {
        return name;
    }
}