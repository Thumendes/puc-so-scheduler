import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Process;

public class FileParser {
    public static List<Process> parse(String filename) {
        List<Process> processes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");

                int pid = Integer.parseInt(parts[0]);
                int arrivalTime = Integer.parseInt(parts[1]);
                int burstTotal = Integer.parseInt(parts[2]);
                int priority = Integer.parseInt(parts[3]);

                String[] ioInstants = parts[4].split(",");
                List<Integer> ioInstantsList = new ArrayList<>();
                for (String ioInstant : ioInstants) {
                    ioInstantsList.add(Integer.parseInt(ioInstant));
                }

                Process newProcess = new Process(pid, arrivalTime, burstTotal, priority, ioInstantsList);
                processes.add(newProcess);
            }
        } catch (IOException e) {
            System.err.println("Error parsing file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return processes;
    }
}
