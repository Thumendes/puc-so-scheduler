import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Process;

/**
 * Utilitário responsável por ler o arquivo de entrada e instanciar os processos.
 * Converte dados textuais em objetos da classe model.Process.
 */
public class FileParser {

    /**
     * Lê um arquivo e retorna uma lista de processos configurados.
     * @param filename Caminho do arquivo a ser lido (ex: "processos.txt").
     * @return List de Process ou null em caso de erro crítico de I/O.
     */
    public static List<Process> parse(String filename) {
        List<Process> processes = new ArrayList<>();

        // Utiliza try-with-resources para garantir que o arquivo seja fechado automaticamente.
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;

            // Lê o arquivo linha por linha até o fim (null).
            while ((line = reader.readLine()) != null) {
                // Quebra a linha em partes usando o delimitador ';' definido no projeto. 
                String[] parts = line.split(";");

                // Conversão de tipos: transforma o texto (String) em inteiros (int).
                int pid = Integer.parseInt(parts[0]);         // Identificador do processo
                int arrivalTime = Integer.parseInt(parts[1]); // Tempo de chegada no sistema
                int burstTotal = Integer.parseInt(parts[2]);  // Carga total de trabalho
                int priority = Integer.parseInt(parts[3]);   // Nível de prioridade

                // Tratamento da sub-lista de instantes de I/O (separados por vírgula). [cite: 33]
                String[] ioInstants = parts[4].split(",");
                List<Integer> ioInstantsList = new ArrayList<>();
                for (String ioInstant : ioInstants) {
                    // Adiciona cada momento de E/S à lista do processo.
                    ioInstantsList.add(Integer.parseInt(ioInstant));
                }

                // Cria o objeto Process com todos os atributos extraídos da linha.
                Process newProcess = new Process(pid, arrivalTime, burstTotal, priority, ioInstantsList);
                
                // Adiciona o novo processo à coleção final.
                processes.add(newProcess);
            }
        } catch (IOException e) {
            // Captura erros físicos (arquivo não encontrado, erro de leitura de disco).
            System.err.println("Error parsing file: " + e.getMessage());
            e.printStackTrace();
            return null; 
        }

        return processes;
    }
}