package com.finance.servlets;

import com.finance.dao.TransactionDAO;
import com.finance.models.Transaction;
import com.finance.utils.ReportUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;

public class ReportServlet extends HttpServlet {
    private TransactionDAO transactionDAO;

    @Override
    public void init() throws ServletException {
        transactionDAO = new TransactionDAO();
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
        String action = request.getParameter("action");

        // Get month and year from request or use current
        Calendar cal = Calendar.getInstance();
        String month = request.getParameter("month");
        String year = request.getParameter("year");
        
        if (month == null) {
            month = String.format("%02d", cal.get(Calendar.MONTH) + 1);
        }
        if (year == null) {
            year = String.valueOf(cal.get(Calendar.YEAR));
        }

        List<Transaction> transactions = transactionDAO.getTransactionsByUserIdAndMonth(userId, month, year);

        if ("downloadXML".equals(action)) {
            String xmlReport = ReportUtil.generateXMLReport(transactions, month, year);
            
            response.setContentType("application/xml");
            response.setHeader("Content-Disposition", 
                "attachment; filename=financial_report_" + year + "_" + month + ".xml");
            
            PrintWriter out = response.getWriter();
            out.print(xmlReport);
            out.flush();
        } else if ("downloadCSV".equals(action)) {
            String csvReport = ReportUtil.generateCSVReport(transactions);
            
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", 
                "attachment; filename=transactions_" + year + "_" + month + ".csv");
            
            PrintWriter out = response.getWriter();
            out.print(csvReport);
            out.flush();
        } else {
            request.setAttribute("transactions", transactions);
            request.setAttribute("selectedMonth", month);
            request.setAttribute("selectedYear", year);
            request.getRequestDispatcher("views/reports.jsp").forward(request, response);
        }
    }
}
