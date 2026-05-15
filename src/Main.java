import java.io.File;
import java.util.List;

import model.Process;
import metrics.Metrics;
import scheduler.Scheduler;
import scheduler.FCFS;
import scheduler.SRTF;
import scheduler.RoundRobin;
import scheduler.MLQ;

/**
 * Ponto de entrada do simulador de escalonamento.
 * Coordena a leitura de dados, execução dos algoritmos e exibição de métricas.
 */
public class Main {

    /** Nome do arquivo de entrada conforme especificado no projeto. */
    private static final String INPUT_FILE = "processos.txt";

    public static void main(String[] args) {
        // --- FASE 1: VALIDAÇÃO DE INFRAESTRUTURA ---
        // Verifica se o arquivo físico existe no diretório raiz para evitar erros de leitura.
        if (!new File(INPUT_FILE).exists()) {
            IO.println("Erro: arquivo nao encontrado: " + INPUT_FILE);
            System.exit(1);
        }

        // --- FASE 2: TRADUÇÃO DE DADOS (PARSING) ---
        // Converte as strings do arquivo texto em instâncias da classe Process.
        List<Process> processes = FileParser.parse(INPUT_FILE);

        // Tratamento de erros para arquivos corrompidos ou vazios.
        if (processes == null) {
            IO.println("Erro: falha ao ler o arquivo (verifique o formato).");
            System.exit(1);
        }

        if (processes.isEmpty()) {
            IO.println("Erro: nenhum processo encontrado no arquivo.");
            System.exit(1);
        }

        // --- FASE 3: DEFINIÇÃO DO ESCOPO DE TESTE ---
        // Lista polimórfica contendo as quatro estratégias de escalonamento exigidas[cite: 10].
        List<Scheduler> schedulers = List.of(
            new FCFS(),
            new SRTF(),
            new RoundRobin(),
            new MLQ()
        );

        IO.println("Simulacao com " + processes.size() + " processos\n");

        // --- FASE 4: EXECUÇÃO DA SIMULAÇÃO ---
        for (Scheduler scheduler : schedulers) {
            /** * MECANISMO DE ISOLAMENTO (Deep Copy):
             * Aqui reside a segurança da simulação. O uso de .map(Process::copy) garante que
             * cada escalonador receba objetos Process novos, com o tempo restante e 
             * estado de I/O zerados, impedindo que o FCFS altere os dados do SRTF, por exemplo.
             */
            List<Process> copy = processes.stream()
                .map(Process::copy)
                .toList();

            // Executa a lógica de simulação e captura os resultados.
            Metrics metrics = scheduler.simulate(copy);
            
            // Exibe o relatório formatado no console.
            metrics.print();
            IO.println("");
        }
    }
}