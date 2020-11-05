import java.util.Date;

public class Transaction {
    Date transactionTime; // Time transaction is done
    String transactionType; // The type of transaction
    String url; // String format of a URL

    public Transaction(Date tTime, String tType, String link) {
        transactionTime = tTime;
        transactionType = tType;
        url = link;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getUrl() {
        return url;
    }
}
