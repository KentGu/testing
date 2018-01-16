package org.yiwan.webcore.bmproxy.pojo;

/**
 * Created by Kenny Wang on 4/2/2016.
 */
public class UserTransactionDetail {
    private String transactionName;
    private long userActionTimestamp;
    private long documentReadyTimestamp;

    public UserTransactionDetail(String transactionName) {
        this.transactionName = transactionName;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public long getDocumentReadyTimestamp() {
        return documentReadyTimestamp;
    }

    public void setDocumentReadyTimestamp(long documentReadyTimestamp) {
        this.documentReadyTimestamp = documentReadyTimestamp;
    }

    public long getUserActionTimestamp() {
        return userActionTimestamp;
    }

    public void setUserActionTimestamp(long userActionTimestamp) {
        this.userActionTimestamp = userActionTimestamp;
    }
}
