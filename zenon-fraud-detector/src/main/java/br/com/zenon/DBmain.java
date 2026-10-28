package br.com.zenon;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class DBmain {
    void main(){
        Connection connection = ConnectionFactory.getConnection();
        IO.println("Conexão com o BD criada!");

        TransactionSQLRepository transactionSQLRepository = new TransactionSQLRepository();
        transactionSQLRepository.findByOriginName("C1000001")
                .ifPresentOrElse(IO::println, () -> IO.println("Transação não encontrada para o valor C1000001"));;


//        Transaction firstTransaction = new Transaction(1, TransactionType.PAYMENT, new BigDecimal("9838.64"),
//                new TransactionCustomer("C1231006815", new BigDecimal("170136.0"), new BigDecimal("160296.36")),
//                new TransactionCustomer("M1979787155", new BigDecimal("0.0"), new BigDecimal("0.0")),
//                false, false);
//
//        transactionSQLRepository.save(firstTransaction);


        TransactionIngestor transactionIngestor = new TransactionIngestor();


        long startTimeSQL = System.nanoTime();

        List<Transaction> transactions = transactionIngestor.read("data/PS_20174392719_1491204439457_log.csv");
        IO.println(transactions.size());
        IO.println("Iniciando inserts");
        //transactions
          //      .forEach(transactionSQLRepository::save);

        transactionSQLRepository.saveAll(transactions);

        long endTimeSQL = System.nanoTime();
        IO.println("Tempo inserção - List(ms): " + (endTimeSQL - startTimeSQL) / 1_000_000.0);

        transactionSQLRepository.findByOriginName("C1231006815")
                .ifPresentOrElse(IO::println, () -> IO.println("Transação não encontrada para o valor C1231006815"));;

    }
}
