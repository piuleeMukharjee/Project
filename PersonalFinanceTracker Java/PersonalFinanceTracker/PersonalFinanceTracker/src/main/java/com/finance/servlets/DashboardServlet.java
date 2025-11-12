package com.finance.servlets;

import com.finance.dao.BudgetDAO;
import com.finance.dao.TransactionDAO;
import com.finance.models.Budget;
import com.finance.models.Transaction;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardServlet extends HttpServlet {
    private TransactionDAO transactionDAO;
    private BudgetDAO budgetDAO;
    private static final Logger logger = Logger.getLogger(DashboardServlet.class.getName());

    @Override
    public void init() throws ServletException {
        try {
            transactionDAO = new TransactionDAO();
            budgetDAO = new BudgetDAO();
            logger.info("DashboardServlet initialized successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize DashboardServlet", e);
            throw new ServletException("Failed to initialize DashboardServlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        HttpSession session = request.getSession(false);
        String userId = null;
        
        try {
            if (session == null || session.getAttribute("userId") == null) {
                logger.warning("Unauthorized access attempt to dashboard page");
                response.sendRedirect("login.jsp");
                return;
            }

            userId = (String) session.getAttribute("userId");
            logger.info("Loading dashboard for userId: " + userId);
            
            // Get current month and year
            Calendar cal = Calendar.getInstance();
            String month = String.format("%02d", cal.get(Calendar.MONTH) + 1);
            String year = String.valueOf(cal.get(Calendar.YEAR));

            // Get transactions for current month
            List<Transaction> transactions = new ArrayList<>();
            try {
                transactions = transactionDAO.getTransactionsByUserIdAndMonth(userId, month, year);
                if (transactions == null) {
                    logger.warning("TransactionDAO returned null for userId: " + userId);
                    transactions = new ArrayList<>();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error fetching transactions for dashboard. userId: " + userId, e);
                transactions = new ArrayList<>();
            }

            // Calculate totals
            double totalIncome = 0;
            double totalExpense = 0;
            Map<String, Double> categoryTotals = new HashMap<>();

            for (Transaction transaction : transactions) {
                if (transaction != null) {
                    try {
                        if ("INCOME".equals(transaction.getType())) {
                            totalIncome += transaction.getAmount();
                        } else {
                            totalExpense += transaction.getAmount();
                            String category = transaction.getCategory();
                            if (category != null) {
                                categoryTotals.put(category,
                                    categoryTotals.getOrDefault(category, 0.0) + transaction.getAmount());
                            }
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error processing transaction", e);
                        // Continue processing other transactions
                    }
                }
            }

            double netSavings = totalIncome - totalExpense;

            // Get budgets for current month
            List<Budget> budgets = new ArrayList<>();
            try {
                budgets = budgetDAO.getBudgetsByUserId(userId, month, year);
                if (budgets == null) {
                    logger.warning("BudgetDAO returned null for userId: " + userId);
                    budgets = new ArrayList<>();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error fetching budgets for dashboard. userId: " + userId, e);
                budgets = new ArrayList<>();
            }

            // Set attributes
            request.setAttribute("totalIncome", totalIncome);
            request.setAttribute("totalExpense", totalExpense);
            request.setAttribute("netSavings", netSavings);
            request.setAttribute("categoryTotals", categoryTotals);
            request.setAttribute("transactions", transactions);
            request.setAttribute("budgets", budgets);
            request.setAttribute("currentMonth", month);
            request.setAttribute("currentYear", year);

            request.getRequestDispatcher("views/dashboard.jsp").forward(request, response);
            
            long endTime = System.currentTimeMillis();
            logger.info("Dashboard loaded in " + (endTime - startTime) + "ms for userId: " + userId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in DashboardServlet doGet for userId: " + userId, e);
            try {
                request.setAttribute("error", "An error occurred while loading the dashboard. Please try again.");
                request.setAttribute("totalIncome", 0.0);
                request.setAttribute("totalExpense", 0.0);
                request.setAttribute("netSavings", 0.0);
                request.setAttribute("categoryTotals", new HashMap<>());
                request.setAttribute("transactions", new ArrayList<>());
                request.setAttribute("budgets", new ArrayList<>());
                request.getRequestDispatcher("views/dashboard.jsp").forward(request, response);
            } catch (Exception forwardException) {
                logger.log(Level.SEVERE, "Error forwarding to dashboard.jsp", forwardException);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading dashboard");
            }
        }
    }
}
