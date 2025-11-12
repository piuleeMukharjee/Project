<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.finance.models.*" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("../login.jsp");
        return;
    }
    
    String username = (String) session.getAttribute("username");
    List<Budget> budgets = (List<Budget>) request.getAttribute("budgets");
    String currentMonth = (String) request.getAttribute("currentMonth");
    String currentYear = (String) request.getAttribute("currentYear");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Budget - Personal Finance Tracker</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body>
    <div class="container">
        <nav class="navbar">
            <div class="nav-brand">Finance Tracker</div>
            <ul class="nav-menu">
                <li><a href="<%=request.getContextPath()%>/dashboard">Dashboard</a></li>
                <li><a href="<%=request.getContextPath()%>/transaction?action=list">Transactions</a></li>
                <li><a href="<%=request.getContextPath()%>/budget" class="active">Budget</a></li>
                <li><a href="<%=request.getContextPath()%>/report">Reports</a></li>
                <li><a href="<%=request.getContextPath()%>/logout">Logout (<%=username%>)</a></li>
            </ul>
        </nav>
        
        <div class="page-header">
            <h1>Budget Management</h1>
            <button class="btn btn-primary" onclick="showAddBudgetModal()">+ Set Budget</button>
        </div>
        
        <div class="card">
            <h2>Current Month Budgets</h2>
            <%if (budgets != null && !budgets.isEmpty()) {%>
                <div class="budget-grid">
                    <%for (Budget budget : budgets) { 
                        double percentage = budget.getPercentageUsed();
                        String statusClass = percentage >= 100 ? "over" : (percentage >= 90 ? "warning" : "good");
                        int percentageWidth = (int)Math.min(percentage, 100);
                    %>
                        <div class="budget-card <%=statusClass%>">
                            <div class="budget-header">
                                <h3><%=budget.getCategory()%></h3>
                                <button class="btn-icon" onclick="deleteBudget('<%=budget.getBudgetId()%>')">🗑️</button>
                            </div>
                            <div class="budget-amounts">
                                <div class="amount-item">
                                    <span class="label">Budget:</span>
                                    <span class="value">₹<%=String.format("%.2f", budget.getBudgetAmount())%></span>
                                </div>
                                <div class="amount-item">
                                    <span class="label">Spent:</span>
                                    <span class="value">₹<%=String.format("%.2f", budget.getSpentAmount())%></span>
                                </div>
                                <div class="amount-item">
                                    <span class="label">Remaining:</span>
                                    <span class="value <%=budget.getRemainingAmount() < 0 ? "negative" : ""%>">
                                        ₹<%=String.format("%.2f", budget.getRemainingAmount())%>
                                    </span>
                                </div>
                            </div>
                            <div class="budget-bar">
                                <div class="budget-progress" data-width="<%=percentageWidth%>"></div>
                            </div>
                            <p class="budget-percentage"><%=String.format("%.1f", percentage)%>% used</p>
                            <%if (percentage >= 90) {%>
                                <div class="alert alert-warning">
                                    <%if (percentage >= 100) {%>
                                        ⚠️ Budget exceeded!
                                    <%} else {%>
                                        ⚠️ Approaching budget limit!
                                    <%}%>
                                </div>
                            <%}%>
                        </div>
                    <%}%>
                </div>
            <%} else {%>
                <p class="empty-state">No budgets set for this month. Click "Set Budget" to create one!</p>
            <%}%>
        </div>
        
        <div class="card">
            <h2>Budget Tips</h2>
            <ul class="tips-list">
                <li>💡 Set realistic budgets based on your income and expenses</li>
                <li>📊 Review your spending patterns regularly</li>
                <li>🎯 Aim to save at least 20% of your income</li>
                <li>⚠️ Get alerts when you're close to your budget limits</li>
                <li>📈 Adjust budgets monthly based on your needs</li>
            </ul>
        </div>
    </div>
    
    <div id="addBudgetModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeAddBudgetModal()">&times;</span>
            <h2>Set Budget</h2>
            <form id="addBudgetForm">
                <div class="form-group">
                    <label for="category">Category</label>
                    <select id="category" name="category" required>
                        <option value="Food">Food</option>
                        <option value="Travel">Travel</option>
                        <option value="Bills">Bills</option>
                        <option value="Shopping">Shopping</option>
                        <option value="Entertainment">Entertainment</option>
                        <option value="Healthcare">Healthcare</option>
                        <option value="Education">Education</option>
                        <option value="Other">Other</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="budgetAmount">Budget Amount</label>
                    <input type="number" id="budgetAmount" name="budgetAmount" step="0.01" required>
                </div>
                
                <input type="hidden" name="month" value="<%=currentMonth%>">
                <input type="hidden" name="year" value="<%=currentYear%>">
                
                <button type="submit" class="btn btn-primary">Set Budget</button>
            </form>
        </div>
    </div>
    
    <script>
        // Set budget progress bar widths from data attributes
        document.addEventListener('DOMContentLoaded', function() {
            var progressBars = document.querySelectorAll('.budget-progress');
            progressBars.forEach(function(bar) {
                var width = bar.getAttribute('data-width');
                bar.style.width = width + '%';
            });
        });
    </script>
    <script src="<%=request.getContextPath()%>/js/budget.js"></script>
</body>
</html>