package com.finance.dao;

import com.finance.models.Budget;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class BudgetDAO {
    private static final String BUDGETS_XML_PATH = System.getProperty("user.home") + 
                                                    "/finance_data/budgets.xml";
    private static final Logger logger = Logger.getLogger(BudgetDAO.class.getName());

    public BudgetDAO() {
        try {
            initializeBudgetsFile();
            logger.info("BudgetDAO initialized successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize BudgetDAO", e);
            throw new RuntimeException("Failed to initialize BudgetDAO", e);
        }
    }

    private void initializeBudgetsFile() {
        File file = new File(BUDGETS_XML_PATH);
        File parentDir = file.getParentFile();
        
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                logger.warning("Failed to create directory: " + parentDir.getAbsolutePath());
            } else {
                logger.info("Created directory: " + parentDir.getAbsolutePath());
            }
        }
        
        if (!file.exists()) {
            try {
                Element root = new Element("budgets");
                Document doc = new Document(root);
                XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                xmlOutput.output(doc, new FileOutputStream(file));
                logger.info("Created budgets.xml file at: " + BUDGETS_XML_PATH);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create budgets.xml file", e);
                throw new RuntimeException("Failed to create budgets.xml file", e);
            }
        } else {
            logger.info("Using existing budgets.xml file at: " + BUDGETS_XML_PATH);
        }
    }

    public boolean addBudget(Budget budget) {
        long startTime = System.currentTimeMillis();
        try {
            if (budget == null) {
                logger.warning("Attempted to add null budget");
                return false;
            }

            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new File(BUDGETS_XML_PATH));
            Element rootElement = document.getRootElement();

            // Check if budget already exists for this category, month, and year
            List<Element> budgets = rootElement.getChildren("budget");
            for (Element budgetElement : budgets) {
                String elementUserId = budgetElement.getChildText("userId");
                String elementCategory = budgetElement.getChildText("category");
                String elementMonth = budgetElement.getChildText("month");
                String elementYear = budgetElement.getChildText("year");
                
                if (elementUserId != null && elementCategory != null && elementMonth != null && elementYear != null &&
                    elementUserId.equals(budget.getUserId()) &&
                    elementCategory.equals(budget.getCategory()) &&
                    elementMonth.equals(budget.getMonth()) &&
                    elementYear.equals(budget.getYear())) {
                    logger.info("Budget already exists for userId: " + budget.getUserId() + 
                               ", category: " + budget.getCategory() + 
                               ", month: " + budget.getMonth() + 
                               ", year: " + budget.getYear());
                    return false; // Budget already exists
                }
            }

            String budgetId = UUID.randomUUID().toString();
            budget.setBudgetId(budgetId);

            Element budgetElement = new Element("budget");
            budgetElement.addContent(new Element("budgetId").setText(budgetId));
            budgetElement.addContent(new Element("userId").setText(budget.getUserId()));
            budgetElement.addContent(new Element("category").setText(budget.getCategory()));
            budgetElement.addContent(new Element("budgetAmount").setText(String.valueOf(budget.getBudgetAmount())));
            budgetElement.addContent(new Element("spentAmount").setText(String.valueOf(budget.getSpentAmount())));
            budgetElement.addContent(new Element("month").setText(budget.getMonth()));
            budgetElement.addContent(new Element("year").setText(budget.getYear()));

            rootElement.addContent(budgetElement);

            XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
            xmlOutput.output(document, new FileOutputStream(BUDGETS_XML_PATH));

            long endTime = System.currentTimeMillis();
            logger.info("Budget added in " + (endTime - startTime) + "ms. budgetId: " + budgetId);
            return true;
        } catch (JDOMException e) {
            logger.log(Level.SEVERE, "JDOMException while adding budget", e);
            return false;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException while adding budget", e);
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error while adding budget", e);
            return false;
        }
    }

    public List<Budget> getBudgetsByUserId(String userId, String month, String year) {
        long startTime = System.currentTimeMillis();
        List<Budget> budgets = new ArrayList<>();
        
        try {
            if (userId == null || month == null || year == null) {
                logger.warning("Null parameters in getBudgetsByUserId - userId: " + userId + 
                              ", month: " + month + ", year: " + year);
                return budgets;
            }

            File file = new File(BUDGETS_XML_PATH);
            if (!file.exists()) {
                logger.warning("Budgets file does not exist: " + BUDGETS_XML_PATH);
                return budgets;
            }

            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(file);
            Element rootElement = document.getRootElement();

            List<Element> budgetElements = rootElement.getChildren("budget");
            for (Element element : budgetElements) {
                try {
                    String elementUserId = element.getChildText("userId");
                    String elementMonth = element.getChildText("month");
                    String elementYear = element.getChildText("year");
                    
                    if (elementUserId != null && elementMonth != null && elementYear != null &&
                        elementUserId.equals(userId) &&
                        elementMonth.equals(month) &&
                        elementYear.equals(year)) {
                        
                        String budgetId = element.getChildText("budgetId");
                        String category = element.getChildText("category");
                        String budgetAmountStr = element.getChildText("budgetAmount");
                        String spentAmountStr = element.getChildText("spentAmount");
                        
                        if (budgetId != null && category != null && budgetAmountStr != null && spentAmountStr != null) {
                            Budget budget = new Budget(
                                budgetId,
                                elementUserId,
                                category,
                                Double.parseDouble(budgetAmountStr),
                                Double.parseDouble(spentAmountStr),
                                elementMonth,
                                elementYear
                            );
                            budgets.add(budget);
                        } else {
                            logger.warning("Incomplete budget data found for userId: " + userId);
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.log(Level.WARNING, "Invalid number format in budget data", e);
                    // Continue processing other budgets
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error parsing budget element", e);
                    // Continue processing other budgets
                }
            }
            
            long endTime = System.currentTimeMillis();
            logger.info("Retrieved " + budgets.size() + " budgets for userId: " + userId + 
                       " in " + (endTime - startTime) + "ms");
        } catch (JDOMException e) {
            logger.log(Level.SEVERE, "JDOMException while getting budgets for userId: " + userId, e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException while getting budgets for userId: " + userId, e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error while getting budgets for userId: " + userId, e);
        }
        
        return budgets;
    }

    public boolean updateBudgetSpentAmount(String userId, String category, String month, String year, double amount) {
        try {
            if (userId == null || category == null || month == null || year == null) {
                logger.warning("Null parameters in updateBudgetSpentAmount");
                return false;
            }

            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new File(BUDGETS_XML_PATH));
            Element rootElement = document.getRootElement();

            List<Element> budgetElements = rootElement.getChildren("budget");
            for (Element element : budgetElements) {
                String elementUserId = element.getChildText("userId");
                String elementCategory = element.getChildText("category");
                String elementMonth = element.getChildText("month");
                String elementYear = element.getChildText("year");
                
                if (elementUserId != null && elementCategory != null && elementMonth != null && elementYear != null &&
                    elementUserId.equals(userId) &&
                    elementCategory.equals(category) &&
                    elementMonth.equals(month) &&
                    elementYear.equals(year)) {
                    
                    try {
                        double currentSpent = Double.parseDouble(element.getChildText("spentAmount"));
                        element.getChild("spentAmount").setText(String.valueOf(currentSpent + amount));

                        XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                        xmlOutput.output(document, new FileOutputStream(BUDGETS_XML_PATH));
                        logger.info("Updated spent amount for budget. userId: " + userId + 
                                   ", category: " + category + ", amount: " + amount);
                        return true;
                    } catch (NumberFormatException e) {
                        logger.log(Level.WARNING, "Invalid spent amount format", e);
                        return false;
                    }
                }
            }
            logger.warning("Budget not found for update. userId: " + userId + 
                          ", category: " + category + ", month: " + month + ", year: " + year);
        } catch (JDOMException e) {
            logger.log(Level.SEVERE, "JDOMException while updating budget spent amount", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException while updating budget spent amount", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error while updating budget spent amount", e);
        }
        return false;
    }

    public boolean deleteBudget(String budgetId) {
        try {
            if (budgetId == null || budgetId.isEmpty()) {
                logger.warning("Attempted to delete budget with null or empty budgetId");
                return false;
            }

            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new File(BUDGETS_XML_PATH));
            Element rootElement = document.getRootElement();

            List<Element> budgetElements = rootElement.getChildren("budget");
            for (Element element : budgetElements) {
                String elementBudgetId = element.getChildText("budgetId");
                if (elementBudgetId != null && elementBudgetId.equals(budgetId)) {
                    rootElement.removeContent(element);

                    XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                    xmlOutput.output(document, new FileOutputStream(BUDGETS_XML_PATH));
                    logger.info("Budget deleted successfully. budgetId: " + budgetId);
                    return true;
                }
            }
            logger.warning("Budget not found for deletion. budgetId: " + budgetId);
        } catch (JDOMException e) {
            logger.log(Level.SEVERE, "JDOMException while deleting budget. budgetId: " + budgetId, e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException while deleting budget. budgetId: " + budgetId, e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error while deleting budget. budgetId: " + budgetId, e);
        }
        return false;
    }
}
