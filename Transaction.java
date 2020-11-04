import java.util.Date;

public class Transaction {
    Date transactionTime;
    String transactionType;
    String url;

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
