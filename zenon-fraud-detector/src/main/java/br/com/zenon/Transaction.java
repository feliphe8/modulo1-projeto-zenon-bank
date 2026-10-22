package br.com.zenon;

import java.math.BigDecimal;
import java.util.Objects;

public record Transaction(int step, TransactionType type, BigDecimal amount, TransactionCustomer origin,
                          TransactionCustomer recipient, boolean isFraud, boolean isFlaggedFraud) {

    public Transaction{

        // Obriga valores NÃO NULOS
        Objects.requireNonNull(type);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(origin);
        Objects.requireNonNull(recipient);

        //imprimi o step de todas as instancias de Transaction
        //IO.println(step);
        if(step <= 0) throw new IllegalArgumentException("O valor de step deve ser positivo: " + step);
        // signum retorna -1 caso seja negativo, retorna 0 caso seja 0 e retorna 1 caso seja positivo
        if (amount.signum() < 0) throw new IllegalArgumentException("O valor de step deve ser positivo ou zero: " + amount);



    }
}
