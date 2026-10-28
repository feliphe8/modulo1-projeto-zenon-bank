package br.com.zenon;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TransactionSQLRepository implements TransactionRepository {


    public static final int JDBC_BATCH_SIZE = 1_000;

    @Override
    public void save(Transaction transaction) {
        String sql = """
                insert into transactions
                (step, `type`, amount, name_origin , old_balance_origin, new_balance_origin , name_recipient, old_balance_recipient , new_balance_recipient, is_fraud, is_flagged_fraud)
                values
                (?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, transaction.step());
            preparedStatement.setString(2, transaction.type().name());
            preparedStatement.setBigDecimal(3, transaction.amount());

            preparedStatement.setString(4, transaction.origin().name());
            preparedStatement.setBigDecimal(5, transaction.origin().oldBalance());
            preparedStatement.setBigDecimal(6, transaction.origin().newBalance());

            preparedStatement.setString(7, transaction.recipient().name());
            preparedStatement.setBigDecimal(8, transaction.recipient().oldBalance());
            preparedStatement.setBigDecimal(9, transaction.recipient().newBalance());

            preparedStatement.setBoolean(10, transaction.isFraud());
            preparedStatement.setBoolean(11, transaction.isFlaggedFraud());

            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Transaction> findByOriginName(String originName) {

        String sql = """
                SELECT id, step, `type`, amount, name_origin, old_balance_origin, new_balance_origin, name_recipient, old_balance_recipient, new_balance_recipient, is_fraud, is_flagged_fraud
                FROM zenon_frauds.transactions WHERE name_origin = ? ORDER BY step LIMIT 1;
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, originName);

            try (ResultSet rs = preparedStatement.executeQuery()) {

                if (rs.next()) {
                    //IO.println(rs.getString("name_origin"));

                    Transaction transaction = mapResultSetToTransaction(rs);

                    return Optional.of(transaction);
                } else {
                    IO.println("Transaction origin não encontrada para origin: ".concat(originName));
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) {
        try {
            int step = rs.getInt("step");
            TransactionType type = TransactionType.valueOf(rs.getString("type"));
            BigDecimal amount = rs.getBigDecimal("amount");

            String originName = rs.getString("name_origin");
            BigDecimal originOldBalance = rs.getBigDecimal("old_balance_origin");
            BigDecimal originNewBalance = rs.getBigDecimal("new_balance_origin");
            TransactionCustomer origin = new TransactionCustomer(originName, originOldBalance, originNewBalance);

            String recipientName = rs.getString("name_recipient");
            BigDecimal recipientOldBalance = rs.getBigDecimal("old_balance_recipient");
            BigDecimal recipientNewBalance = rs.getBigDecimal("new_balance_recipient");
            TransactionCustomer recipient = new TransactionCustomer(recipientName, recipientOldBalance, recipientNewBalance);

            boolean isFraud = rs.getBoolean("is_fraud");
            boolean isFlaggedFraud = rs.getBoolean("is_flagged_fraud");

            return new Transaction(step, type, amount, origin, recipient, isFraud, isFlaggedFraud);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void saveAll(List<Transaction> transactions) {
        String sql = """
                insert into transactions
                (step, `type`, amount, name_origin , old_balance_origin, new_balance_origin , name_recipient, old_balance_recipient , new_balance_recipient, is_fraud, is_flagged_fraud)
                values
                (?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (Connection connection = ConnectionFactory.getConnection()) {

            // settando o commit como false, é necessario fazer o rollback na mão
            connection.setAutoCommit(false);

            int count = 0;
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (Transaction transaction : transactions) {

                    preparedStatement.setInt(1, transaction.step());
                    preparedStatement.setString(2, transaction.type().name());
                    preparedStatement.setBigDecimal(3, transaction.amount());

                    preparedStatement.setString(4, transaction.origin().name());
                    preparedStatement.setBigDecimal(5, transaction.origin().oldBalance());
                    preparedStatement.setBigDecimal(6, transaction.origin().newBalance());

                    preparedStatement.setString(7, transaction.recipient().name());
                    preparedStatement.setBigDecimal(8, transaction.recipient().oldBalance());
                    preparedStatement.setBigDecimal(9, transaction.recipient().newBalance());

                    preparedStatement.setBoolean(10, transaction.isFraud());
                    preparedStatement.setBoolean(11, transaction.isFlaggedFraud());

                    // Não manda pro banco de imediato, ele vai acumular um lote
                    preparedStatement.addBatch();
                    IO.println("Adicionando transação no batch. Count: " + count);
                    count++;
                    // Se for multiplo de 1000, ou dito de outro modo, de 1000 em 1000. Acumula os inserts até 1000 para mandar pro banco
                    if (count % JDBC_BATCH_SIZE == 0) {
                        IO.println("Executando BATCH...");
                        preparedStatement.executeBatch();
                        connection.commit();
                    }

                }

                IO.println("Executando BATCH final....");
                preparedStatement.executeBatch();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (Exception ex) {
                    throw new RuntimeException("Erro ao executar rollback: ", ex);
                }
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro na conexao com o banco de dados", e);
        }
    }
}
