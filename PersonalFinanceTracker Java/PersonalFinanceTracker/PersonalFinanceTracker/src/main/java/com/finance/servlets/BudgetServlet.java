package com.finance.servlets;

import com.finance.dao.BudgetDAO;
import com.finance.models.Budget;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetServlet extends HttpServlet {
    private BudgetDAO budgetDAO;

    @Override
    public void init() throws ServletException {
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

        String userId = (String) session.getAttribute("userId");
        
        // Get current month and year
        Calendar cal = Calendar.getInstance();
        String month = String.format("%02d", cal.get(Calendar.MONTH) + 1);
        String year = String.valueOf(cal.get(Calendar.YEAR));

        List<Budget> budgets = budgetDAO.getBudgetsByUserId(userId, month, year);
        request.setAttribute("budgets", budgets);
        request.setAttribute("currentMonth", month);
        request.setAttribute("currentYear", year);

        request.getRequestDispatcher("views/budget.jsp").forward(request, response);
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
            String category = request.getParameter("category");
            double budgetAmount = Double.parseDouble(request.getParameter("budgetAmount"));
            String month = request.getParameter("month");
            String year = request.getParameter("year");

            Budget budget = new Budget(null, userId, category, budgetAmount, 0, month, year);
            boolean success = budgetDAO.addBudget(budget);

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (!success) {
                result.put("message", "Budget already exists for this category and month");
            }
            out.print(new Gson().toJson(result));
            out.flush();
        } else if ("delete".equals(action)) {
            String budgetId = request.getParameter("budgetId");
            boolean success = budgetDAO.deleteBudget(budgetId);

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            Map<String, Boolean> result = new HashMap<>();
            result.put("success", success);
            out.print(new Gson().toJson(result));
            out.flush();
        }
    }
}
