package br.com.zenon;


public class IngestionMain {
    void main(){

        TransactionSQLRepository transactionSQLRepository = new TransactionSQLRepository();

//        Transaction firstTransaction = new Transaction(1, TransactionType.PAYMENT, new BigDecimal("9838.64"),
//                new TransactionCustomer("C1231006815", new BigDecimal("170136.0"), new BigDecimal("160296.36")),
//                new TransactionCustomer("M1979787155", new BigDecimal("0.0"), new BigDecimal("0.0")),
//                false, false);
//
//        transactionSQLRepository.save(firstTransaction);


        EfficientTransactionIngestor transactionIngestor = new EfficientTransactionIngestor();


        long startTimeSQL = System.nanoTime();

        transactionIngestor.readAsBatch("data/PS_20174392719_1491204439457_log.csv",
                transactionSQLRepository::saveAll);
        //transactions
          //      .forEach(transactionSQLRepository::save);

        //transactionSQLRepository.saveAll(transactions);

        long endTimeSQL = System.nanoTime();
        IO.println("Tempo ingestão no BD - List(ms): " + (endTimeSQL - startTimeSQL) / 1_000_000.0);


    }
}
