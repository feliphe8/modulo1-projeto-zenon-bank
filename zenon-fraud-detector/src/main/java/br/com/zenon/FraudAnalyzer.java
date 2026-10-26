package br.com.zenon;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FraudAnalyzer {


    private final List<Transaction> transactions;

    public FraudAnalyzer(List<Transaction> transactions){
        Objects.requireNonNull(transactions);
        this.transactions = transactions;
    }

    public long countFrauds() {
        return fraudStream()// ou transaction -> transaction.isFraud()
                .count();
    }

    public List<BigDecimal> findHighestValueFraudsAmounts(int limit) {
        return highestValueFraudStream() // ordena o valor do campo amount com a ordem reversa, ou seja, do MAIOR para o MENOR
                .map(Transaction::amount)
                .limit(limit)
                .toList();

    }

    public List<String> findTopSuspiciousClients(int limit) {
        return highestValueFraudStream() // ordena o valor do campo amount com a ordem reversa, ou seja, do MAIOR para o MENOR
                .map(transaction -> transaction.origin().name())
                .distinct() // pega os nomes distintos, ou seja, sem repetição
                .limit(limit)
                .toList();
    }

    public BigDecimal calculateTotalFraudLoss() {
        return fraudStream()
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // reduce reduz todos os valores a um único valor, neste caso ele vai somar os amounts
    }

    public Map<TransactionType, Long> countFraudsByType() {
        return fraudStream()
                .collect(Collectors.groupingBy(Transaction::type, Collectors.counting())); // conta quantas fraudes tem por tipo

    }

    private Stream<Transaction> fraudStream() {
        return transactions
                .stream()
                .filter(Transaction::isFraud);
    }

    private Stream<Transaction> highestValueFraudStream() {
        return fraudStream()
                .sorted(Comparator.comparing(Transaction::amount).reversed());
    }
}
