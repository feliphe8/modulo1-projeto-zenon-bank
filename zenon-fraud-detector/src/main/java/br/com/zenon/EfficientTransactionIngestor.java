package br.com.zenon;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EfficientTransactionIngestor {

    public static final int FRAUD_LIMIT = 10_000;
    public static final int LINE_BATCH_SIZE = 2_500;

    public void readAsBatch(String fileName, Consumer<List<Transaction>> batchConsumer){
        Path path = Path.of(fileName);
        try(ExecutorService executorService = Executors.newFixedThreadPool(10);
                Stream<String> lines = Files.lines(path).skip(1)/*.limit(FRAUD_LIMIT)*/) {

            Iterator<String> iterator = lines.iterator();
            //Pula o cabeçalho(primeira linha)
            //if(iterator.hasNext()) iterator.next();

            List<String> lineBatch = new ArrayList<>(LINE_BATCH_SIZE);
            while(iterator.hasNext()){
                String line = iterator.next();
                lineBatch.add(line);


                if (lineBatch.size() >= LINE_BATCH_SIZE){

                    // manda uma copia imutavel para não dar problema entre as threads - ira utilizar mais memoria e ira perder a ordem das coisas
                    final List<String> currentLineBatch = List.copyOf(lineBatch);
                    // executa o executeBatch em paralelo
                    executorService.submit(() -> executeBatch(currentLineBatch, batchConsumer));
                    lineBatch.clear();
                }

            }

            if(!lineBatch.isEmpty()){
                IO.println("Executando batch final...");
                // manda uma copia imutavel para não dar problema entre as threads
                final List<String> currentLineBatch = List.copyOf(lineBatch);
                // executa o executeBatch em paralelo
                executorService.submit(() -> executeBatch(currentLineBatch, batchConsumer));
            }

        } catch (Exception e){
            throw new RuntimeException("Erro ao ler arquivo: " + fileName, e);
        }
    }

    private void executeBatch(List<String> lineBatch, Consumer<List<Transaction>> batchConsumer) {
        List<Transaction> transactionBatch = lineBatch.stream()
                .map(this::parseTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        // consome as 5 mil transações
        batchConsumer.accept(transactionBatch);
    }

    // Metodo mais moderno de se ler arquivos
    public void readAsStream(String fileName, Consumer<Transaction> consumer){
        Path path = Path.of(fileName);
        try(Stream<String> lines = Files.lines(path)) {

            // lê o fluxo, skipa a primeira linha pois é cabeçalho, limita a leitura a 1000
            // mapeia para cada item, a execução do metodo parseTransaction
            // filter(Objects:nonNull) filtra deixando apenas os NÃO NULOS
            // filter(Optional::isPresent) verifica se está presente
            // map(Optional::get) pega/retorna o valor
            lines
                    .skip(1)
                    //.limit(FRAUD_LIMIT)
                    .map(this::parseTransaction)
                    //.filter(Objects::nonNull)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(consumer);
        } catch (Exception e){
            throw new RuntimeException("Erro ao ler arquivo: " + fileName, e);
        }
    }


    // Jeito antigo de ler arquivos
//    public List<Transaction> readOldSchool(String fileName){
//
//        List<Transaction> transactions = new ArrayList<Transaction>();
//
//        try(FileInputStream fileInputStream = new FileInputStream(fileName);
//            Scanner scanner = new Scanner(fileInputStream)){
//
//            int lineCount = 0;
//            // Enquanto scanner tiver proxima linha
//            while (scanner.hasNextLine()){
//                // pega a proxima linha
//                String line = scanner.nextLine();
//                lineCount++;
//
//                // pula a primeira linha pois a primeira linha é o cabeçalho
//                if(lineCount == 1){
//                    continue;
//                }
//
//                // limita a leitura para 1000 linhas
//                if (lineCount > 1001){
//                    break;
//                }
//
//                //IO.println(line);
//                Transaction transaction = parseTransaction(line);
//                transactions.add(transaction);
//
//            }
//
//        } catch (Exception e){
//            throw new RuntimeException("Erro ao ler o arquivo: " + fileName, e);
//        }
//
//        return transactions;
//    }


    // Optional deixa claro na assinatura do método que é possível retornar um valor null
    private Optional<Transaction> parseTransaction(String line) {
        try {
            String[] chunks = line.split(",");
            int step = Integer.parseInt(chunks[0]);
            TransactionType type = TransactionType.valueOf(chunks[1]);

            if(chunks[2] == null || chunks[2].trim().isEmpty()) throw new IllegalArgumentException("O valor de amount não pode ser nulo nem vazio");
            BigDecimal amount = new BigDecimal(chunks[2]);

            TransactionCustomer origin = new TransactionCustomer(chunks[3], new BigDecimal(chunks[4]), new BigDecimal(chunks[5]));
            TransactionCustomer recipient = new TransactionCustomer(chunks[6], new BigDecimal(chunks[7]), new BigDecimal(chunks[8]));
            boolean isFraud = "1".equals(chunks[9]);
            boolean isFlaggedFraud = "1".equals(chunks[10]);


            return Optional.of(new Transaction(step, type, amount, origin, recipient, isFraud, isFlaggedFraud));
        }catch (Exception e){
            System.err.println("Erro ao fazer parse: " + line + " | " + e);
            return Optional.empty();
        }
    }
}
