package com.finance.servlets;

import com.finance.dao.BudgetDAO;
import com.finance.dao.TransactionDAO;
import com.finance.models.Transaction;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionServlet extends HttpServlet {
    private TransactionDAO transactionDAO;
    private BudgetDAO budgetDAO;

    @Override
    public void init() throws ServletException {
        transactionDAO = new TransactionDAO();
        budgetDAO = new BudgetDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        String userId = (String) session.getAttribute("userId");

        if ("list".equals(action)) {
            List<Transaction> transactions = transactionDAO.getTransactionsByUserId(userId);
            request.setAttribute("transactions", transactions);
            request.getRequestDispatcher("views/transactions.jsp").forward(request, response);
        } else if ("edit".equals(action)) {
            String transactionId = request.getParameter("id");
            Transaction transaction = transactionDAO.getTransactionById(transactionId);
            
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(new Gson().toJson(transaction));
            out.flush();
        } else if ("delete".equals(action)) {
            String transactionId = request.getParameter("id");
            boolean success = transactionDAO.deleteTransaction(transactionId);
            
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            Map<String, Boolean> result = new HashMap<>();
            result.put("success", success);
            out.print(new Gson().toJson(result));
            out.flush();
        } else {
            request.getRequestDispatcher("views/transactions.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        String userId = (String) session.getAttribute("userId");

        if ("add".equals(action)) {
            String type = request.getParameter("type");
            double amount = Double.parseDouble(request.getParameter("amount"));
            String category = request.getParameter("category");
            String description = request.getParameter("description");
            String date = request.getParameter("date");

            Transaction transaction = new Transaction(null, userId, type, amount, category, description, date);
            boolean success = transactionDAO.addTransaction(transaction);

            // Update budget if expense
            if (success && "EXPENSE".equals(type)) {
                String[] dateParts = date.split("-");
                String month = dateParts[1];
                String year = dateParts[0];
                budgetDAO.updateBudgetSpentAmount(userId, category, month, year, amount);
            }

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            Map<String, Boolean> result = new HashMap<>();
            result.put("success", success);
            out.print(new Gson().toJson(result));
            out.flush();
        } else if ("update".equals(action)) {
            String transactionId = request.getParameter("transactionId");
            String type = request.getParameter("type");
            double amount = Double.parseDouble(request.getParameter("amount"));
            String category = request.getParameter("category");
            String description = request.getParameter("description");
            String date = request.getParameter("date");

            Transaction transaction = new Transaction(transactionId, userId, type, amount, category, description, date);
            boolean success = transactionDAO.updateTransaction(transaction);

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            Map<String, Boolean> result = new HashMap<>();
            result.put("success", success);
            out.print(new Gson().toJson(result));
            out.flush();
        }
    }
}
