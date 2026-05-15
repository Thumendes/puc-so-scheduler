package scheduler;

import model.Process;
import metrics.Metrics;
import java.util.List;

/**
 * Classe base abstrata para todos os algoritmos de escalonamento.
 * Define a estrutura comum e constantes globais da simulação.
 */
public abstract class Scheduler {

    /** Nome identificador do algoritmo (ex: "FCFS", "SRTF"). */
    protected final String name;

    /** * Duração fixa de cada operação de I/O em unidades de tempo.
     * Conforme o enunciado: cada operação bloqueia o processo por 5 unidades.
     */
    public static final int IO_DURATION = 5;

    /**
     * Construtor protegido para ser utilizado pelas subclasses.
     * @param name O nome amigável do algoritmo de escalonamento.
     */
    protected Scheduler(String name) {
        this.name = name;
    }

    /**
     * Método central da simulação. Deve ser implementado por cada algoritmo
     * para ditar como os processos são escolhidos para a CPU.
     * @param processes Lista de processos a serem escalonados.
     * @return Objeto Metrics contendo o desempenho da simulação.
     */
    public abstract Metrics simulate(List<Process> processes);

    /** @return O nome do algoritmo utilizado. */
    public String getName() {
        return name;
    }
}