package br.com.zenon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private ConnectionFactory(){}

    public static Connection getConnection(){
        try {
            //rewriteBatchedStatements=true - reescreve como as batchs são feitas. Ao invez de fazer 1000 inserts, faz apenas 1 insert com 1000 values diferentes
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/zenon_frauds?rewriteBatchedStatements=true", "root", "senha123");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar com o BD " + e);
        }
    }
}
