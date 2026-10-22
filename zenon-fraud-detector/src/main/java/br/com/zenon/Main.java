package br.com.zenon;

import java.math.BigDecimal;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    // git checkout -b nome-branch para trocar pra branch
    // git add . para adicionar os novos arquivos para serem commitados
    // git commit -m "mensagem" para fazer o commit
    // git push -u origin nome-branch para fazer o push pra branch

    static void main() {

        Transaction firstTransaction = new Transaction(1, TransactionType.PAYMENT, new BigDecimal("9838.64"),
                new TransactionCustomer("C1231006815", new BigDecimal("170136.0"), new BigDecimal("160296.36")),
                new TransactionCustomer("M1979787155", new BigDecimal("0.0"), new BigDecimal("0.0")),
                false, false);

        Transaction secondTransaction = new Transaction(743, TransactionType.CASH_OUT, new BigDecimal("850002.52"),
                new TransactionCustomer("C1280323807", new BigDecimal("850002.52"), new BigDecimal("0.0")),
                new TransactionCustomer("C873221189", new BigDecimal("6510099.11"), new BigDecimal("7360101.63")),
                true, false);

        IO.println(firstTransaction);
        IO.println(secondTransaction);

        IO.println("------------------------------------------------------------------------------");

        TransactionIngestor transactionIngestor = new TransactionIngestor();
        List<Transaction> transactions = transactionIngestor.read("data/PS_20174392719_1491204439457_log.csv");
        IO.println(transactions.size());

        // para o fluxo transactions, limita a 10 registro e imprime cada um
        transactions.stream().limit(10).forEach(IO::println);

        List<Transaction> transactionsBadData = transactionIngestor.read("data/paysim_with_bad_data.csv");
        transactionsBadData.forEach(IO::println);

    }
}
