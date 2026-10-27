package br.com.zenon;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReportMain {
    void main(String[] args){
        // parâmetro passado no program arguments 
        String language = (args.length > 0) ? args[0] : "pt";

        Locale locale = Locale.of(language);

        NumberFormat integerFormatter = NumberFormat.getIntegerInstance(locale);
        NumberFormat currencyFormatter = DecimalFormat.getCurrencyInstance(locale);
        currencyFormatter.setCurrency(Currency.getInstance("USD"));

        ResourceBundle resourceBundle = ResourceBundle.getBundle("report", locale);

        TransactionReport transactionReport = new TransactionReport();
        TransactionReport.Statistics statistics = transactionReport.generateReport("data/PS_20174392719_1491204439457_log.csv");

        String formattedTotalTransactions = integerFormatter.format(statistics.totalTransactions());
        String formattedTotalFrauds = integerFormatter.format(statistics.totalFrauds());
        String formattedTotalAmount = currencyFormatter.format(statistics.totalAmount());

        String msgTotalTransactions = resourceBundle.getString("label.total.transactions");
        String msgTotalFrauds = resourceBundle.getString("label.total.frauds");
        String msgTotalAmount = resourceBundle.getString("label.total.amount");


        IO.println("""
               %s: %s
                %s: %s
                %s: %s
                """.formatted(msgTotalTransactions, formattedTotalTransactions, msgTotalFrauds, formattedTotalFrauds, msgTotalAmount, formattedTotalAmount));
    }
}
