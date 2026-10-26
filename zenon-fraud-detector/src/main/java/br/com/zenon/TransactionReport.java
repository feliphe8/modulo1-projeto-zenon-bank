package br.com.zenon;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TransactionReport {

    private record ReportTransaction(BigDecimal amount, boolean isFraud){
    }

    public record Statistics(long totalTransactions, long totalFrauds, BigDecimal totalAmount){

        private final static Statistics ZERO = new Statistics(0, 0, BigDecimal.ZERO);

        private Statistics addReportTransaction(ReportTransaction rt){
            return new Statistics(totalTransactions + 1, totalFrauds + (rt.isFraud ? 1 : 0) , totalAmount.add(rt.amount));
        }

        private Statistics add(Statistics other){
            return new Statistics(totalTransactions + other.totalTransactions,
                    totalFrauds + other.totalFrauds,
                    totalAmount.add(other.totalAmount)
                    );
        }
    }

    // com memoria limitada, este relatorio perfoma melhor
    public Statistics generateReport(String fileName){
        Path path = Path.of(fileName);

        //Lê uma linha por vez
        try(Stream<String> lines = Files.lines(path)) {

            return lines.skip(1)
                    .map(this::parseReportTransaction)
                    .filter(Optional::isPresent) // filtra por objetos presents
                    .map(Optional::get) // pega a reporttransaction
                    .reduce(
                           Statistics.ZERO,
                            Statistics::addReportTransaction, //(acc, rt) -> acc.add(rt)
                            Statistics::add//(s1, s2) -> s1 - combina duas statistics
                    );

        } catch (Exception e){
            throw new RuntimeException("Erro ao ler arquivo: " + fileName, e);
        }
    }

    private Optional<ReportTransaction> parseReportTransaction(String line){
        try {
            String[] chunks = line.split(",");

            if(chunks[2] == null || chunks[2].trim().isEmpty()) throw new IllegalArgumentException("O valor de amount não pode ser nulo nem vazio");
            BigDecimal amount = new BigDecimal(chunks[2]);

            boolean isFraud = "1".equals(chunks[9]);


            return Optional.of(new ReportTransaction(amount,isFraud));
        }catch (Exception e){
            System.err.println("Erro ao fazer parse: " + line + " | " + e);
            return Optional.empty();
        }
    }
}
