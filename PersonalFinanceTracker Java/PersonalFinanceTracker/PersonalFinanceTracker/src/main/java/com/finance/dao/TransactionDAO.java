package com.finance.dao;

import com.finance.models.Transaction;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TransactionDAO {
    private static final String TRANSACTIONS_XML_PATH = System.getProperty("user.home") + 
                                                         "/finance_data/transactions.xml";

    public TransactionDAO() {
        initializeTransactionsFile();
    }

    private void initializeTransactionsFile() {
        File file = new File(TRANSACTIONS_XML_PATH);
        file.getParentFile().mkdirs();
        
        if (!file.exists()) {
            try {
                Element root = new Element("transactions");
                Document doc = new Document(root);
                XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                xmlOutput.output(doc, new FileOutputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean addTransaction(Transaction transaction) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new File(TRANSACTIONS_XML_PATH));
            Element rootElement = document.getRootElement();

            String transactionId = UUID.randomUUID().toString();
            transaction.setTransactionId(transactionId);

            Element transactionElement = new Element("transaction");
            transactionElement.addContent(new Element("transactionId").setText(transactionId));
            transactionElement.addContent(new Element("userId").setText(transaction.getUserId()));
            transactionElement.addContent(new Element("type").setText(transaction.getType()));
            transactionElement.addContent(new Element("amount").setText(String.valueOf(transaction.getAmount())));
            transactionElement.addContent(new Element("category").setText(transaction.getCategory()));
            transactionElement.addContent(new Element("description").setText(transaction.getDescription()));
            transactionElement.addContent(new Element("date").setText(transaction.getDate()));

            rootElement.addContent(transactionElement);

            XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
            xmlOutput.output(document, new FileOutputStream(TRANSACTIONS_XML_PATH));

            return true;
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getTransactionsByUserId(String userId) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new File(TRANSACTIONS_XML_PATH));
            Element rootElement = document.getRootElement();

            List<Element> transactionElements = rootElement.getChildren("transaction");
            for (Element element : transactionElements) {
                if (element.getChildText("userId").equals(userId)) {
                    Transaction transaction = new Transaction(
                        element.getChildText("transactionId"),
                        element.getChildText("userId"),
                        element.getChildText("type"),
                        Double.parseDouble(element.getChildText("amount")),
                        element.getChildText("category"),
                        element.getChildText("description"),
                        element.getChildText("date")
                    );
                    transactions.add(transaction);
                }
            }
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByUserIdAndMonth(String userId, String month, String year) {
        List<Transaction> allTransactions = getTransactionsByUserId(userId);
        return allTransactions.stream()
            .filter(t -> t.getDate().startsWith(year + "-" + month))
            .collect(Collectors.toList());
    }

    public boolean updateTransaction(Transaction transaction) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new File(TRANSACTIONS_XML_PATH));
            Element rootElement = document.getRootElement();

            List<Element> transactionElements = rootElement.getChildren("transaction");
            for (Element element : transactionElements) {
                if (element.getChildText("transactionId").equals(transaction.getTransactionId())) {
                    element.getChild("type").setText(transaction.getType());
                    element.getChild("amount").setText(String.valueOf(transaction.getAmount()));
                    element.getChild("category").setText(transaction.getCategory());
                    element.getChild("description").setText(transaction.getDescription());
                    element.getChild("date").setText(transaction.getDate());

                    XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                    xmlOutput.output(document, new FileOutputStream(TRANSACTIONS_XML_PATH));
                    return true;
                }
            }
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteTransaction(String transactionId) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new File(TRANSACTIONS_XML_PATH));
            Element rootElement = document.getRootElement();

            List<Element> transactionElements = rootElement.getChildren("transaction");
            for (Element element : transactionElements) {
                if (element.getChildText("transactionId").equals(transactionId)) {
                    rootElement.removeContent(element);

                    XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                    xmlOutput.output(document, new FileOutputStream(TRANSACTIONS_XML_PATH));
                    return true;
                }
            }
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Transaction getTransactionById(String transactionId) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new File(TRANSACTIONS_XML_PATH));
            Element rootElement = document.getRootElement();

            List<Element> transactionElements = rootElement.getChildren("transaction");
            for (Element element : transactionElements) {
                if (element.getChildText("transactionId").equals(transactionId)) {
                    return new Transaction(
                        element.getChildText("transactionId"),
                        element.getChildText("userId"),
                        element.getChildText("type"),
                        Double.parseDouble(element.getChildText("amount")),
                        element.getChildText("category"),
                        element.getChildText("description"),
                        element.getChildText("date")
                    );
                }
            }
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
