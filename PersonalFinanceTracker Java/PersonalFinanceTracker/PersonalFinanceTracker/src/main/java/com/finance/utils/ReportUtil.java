package com.finance.utils;

import com.finance.models.Transaction;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportUtil {

    public static String generateXMLReport(List<Transaction> transactions, String month, String year) {
        try {
            Element root = new Element("FinancialReport");
            root.setAttribute("month", month);
            root.setAttribute("year", year);

            double totalIncome = 0;
            double totalExpense = 0;
            Map<String, Double> categoryExpenses = new HashMap<>();

            for (Transaction transaction : transactions) {
                if ("INCOME".equals(transaction.getType())) {
                    totalIncome += transaction.getAmount();
                } else {
                    totalExpense += transaction.getAmount();
                    categoryExpenses.put(transaction.getCategory(),
                        categoryExpenses.getOrDefault(transaction.getCategory(), 0.0) + transaction.getAmount());
                }
            }

            // Summary
            Element summary = new Element("Summary");
            summary.addContent(new Element("TotalIncome").setText(String.format("%.2f", totalIncome)));
            summary.addContent(new Element("TotalExpense").setText(String.format("%.2f", totalExpense)));
            summary.addContent(new Element("NetSavings").setText(String.format("%.2f", totalIncome - totalExpense)));
            root.addContent(summary);

            // Category Breakdown
            Element categories = new Element("CategoryBreakdown");
            for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
                Element category = new Element("Category");
                category.setAttribute("name", entry.getKey());
                category.setText(String.format("%.2f", entry.getValue()));
                categories.addContent(category);
            }
            root.addContent(categories);

            // All Transactions
            Element transactionsElement = new Element("Transactions");
            for (Transaction transaction : transactions) {
                Element transElement = new Element("Transaction");
                transElement.addContent(new Element("Date").setText(transaction.getDate()));
                transElement.addContent(new Element("Type").setText(transaction.getType()));
                transElement.addContent(new Element("Category").setText(transaction.getCategory()));
                transElement.addContent(new Element("Amount").setText(String.format("%.2f", transaction.getAmount())));
                transElement.addContent(new Element("Description").setText(transaction.getDescription()));
                transactionsElement.addContent(transElement);
            }
            root.addContent(transactionsElement);

            Document doc = new Document(root);
            XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
            StringWriter writer = new StringWriter();
            xmlOutput.output(doc, writer);
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateCSVReport(List<Transaction> transactions) {
        StringBuilder csv = new StringBuilder();
        csv.append("Date,Type,Category,Amount,Description\n");

        for (Transaction transaction : transactions) {
            csv.append(transaction.getDate()).append(",");
            csv.append(transaction.getType()).append(",");
            csv.append(transaction.getCategory()).append(",");
            csv.append(String.format("%.2f", transaction.getAmount())).append(",");
            csv.append("\"").append(transaction.getDescription()).append("\"\n");
        }

        return csv.toString();
    }

    public static Map<String, Double> calculateCategoryTotals(List<Transaction> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();

        for (Transaction transaction : transactions) {
            if ("EXPENSE".equals(transaction.getType())) {
                categoryTotals.put(transaction.getCategory(),
                    categoryTotals.getOrDefault(transaction.getCategory(), 0.0) + transaction.getAmount());
            }
        }

        return categoryTotals;
    }
}
