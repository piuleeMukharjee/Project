<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.finance.models.*" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("../login.jsp");
        return;
    }
    
    String username = (String) session.getAttribute("username");
    double totalIncome = request.getAttribute("totalIncome") != null ? (Double) request.getAttribute("totalIncome") : 0.0;
    double totalExpense = request.getAttribute("totalExpense") != null ? (Double) request.getAttribute("totalExpense") : 0.0;
    double netSavings = request.getAttribute("netSavings") != null ? (Double) request.getAttribute("netSavings") : 0.0;
    Map<String, Double> categoryTotals = (Map<String, Double>) request.getAttribute("categoryTotals");
    List<Transaction> transactions = (List<Transaction>) request.getAttribute("transactions");
    List<Budget> budgets = (List<Budget>) request.getAttribute("budgets");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Personal Finance Tracker</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
</head>
<body>
    <div class="container">
        <nav class="navbar">
            <div class="nav-brand">Finance Tracker</div>
            <ul class="nav-menu">
                <li><a href="<%=request.getContextPath()%>/dashboard" class="active">Dashboard</a></li>
                <li><a href="<%=request.getContextPath()%>/transaction?action=list">Transactions</a></li>
                <li><a href="<%=request.getContextPath()%>/budget">Budget</a></li>
                <li><a href="<%=request.getContextPath()%>/report">Reports</a></li>
                <li><a href="<%=request.getContextPath()%>/logout">Logout (<%=username%>)</a></li>
            </ul>
        </nav>
        
        <div class="dashboard-header">
            <h1>Welcome, <%=username%>!</h1>
            <p class="subtitle">Here's your financial overview for this month</p>
        </div>
        
        <div class="stats-grid">
            <div class="stat-card income">
                <h3>Total Income</h3>
                <p class="stat-value">₹<%=String.format("%.2f", totalIncome)%></p>
            </div>
            
            <div class="stat-card expense">
                <h3>Total Expense</h3>
                <p class="stat-value">₹<%=String.format("%.2f", totalExpense)%></p>
            </div>
            
            <div class="stat-card savings <%=netSavings >= 0 ? "positive" : "negative"%>">
                <h3>Net Savings</h3>
                <p class="stat-value">₹<%=String.format("%.2f", netSavings)%></p>
            </div>
        </div>
        
        <div class="content-grid">
            <div class="card">
                <h2>Spending by Category</h2>
                <canvas id="categoryChart"></canvas>
            </div>
            
            <div class="card">
                <h2>Budget Overview</h2>
                <%if (budgets != null && !budgets.isEmpty()) {%>
                    <div class="budget-list">
                        <%
                        for (Budget budget : budgets) {
                            double percentage = budget.getPercentageUsed();
                            int percentageWidth = (int)Math.min(percentage, 100);
                        %>
                            <div class="budget-item">
                                <div class="budget-info">
                                    <span class="budget-category"><%=budget.getCategory()%></span>
                                    <span class="budget-amount">
                                        ₹<%=String.format("%.2f", budget.getSpentAmount())%> / 
                                        ₹<%=String.format("%.2f", budget.getBudgetAmount())%>
                                    </span>
                                </div>
                                <div class="budget-bar">
                                    <div class="budget-progress" data-width="<%=percentageWidth%>"></div>
                                </div>
                                <%if (percentage >= 90) {%>
                                    <p class="budget-warning">⚠️ Warning: <%=String.format("%.0f", percentage)%>% of budget used!</p>
                                <%}%>
                            </div>
                        <%}%>
                    </div>
                <%} else {%>
                    <p class="empty-state">No budgets set for this month. <a href="<%=request.getContextPath()%>/budget">Set a budget</a></p>
                <%}%>
            </div>
        </div>
        
        <div class="card">
            <h2>Recent Transactions</h2>
            <%if (transactions != null && !transactions.isEmpty()) {%>
                <table class="transaction-table">
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Type</th>
                            <th>Category</th>
                            <th>Description</th>
                            <th>Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                        int count = 0;
                        for (Transaction transaction : transactions) {
                            if (count >= 10) break;
                            count++;
                        %>
                            <tr>
                                <td><%=transaction.getDate()%></td>
                                <td><span class="badge <%=transaction.getType().toLowerCase()%>"><%=transaction.getType()%></span></td>
                                <td><%=transaction.getCategory()%></td>
                                <td><%=transaction.getDescription()%></td>
                                <td class="<%=transaction.getType().equals("INCOME") ? "income-text" : "expense-text"%>">
                                    <%=transaction.getType().equals("INCOME") ? "+" : "-"%>₹<%=String.format("%.2f", transaction.getAmount())%>
                                </td>
                            </tr>
                        <%}%>
                    </tbody>
                </table>
                <a href="<%=request.getContextPath()%>/transaction?action=list" class="btn btn-secondary">View All Transactions</a>
            <%} else {%>
                <p class="empty-state">No transactions yet. <a href="<%=request.getContextPath()%>/transaction?action=list">Add a transaction</a></p>
            <%}%>
        </div>
    </div>
    
    <script>
        // Set budget progress bar widths
        document.addEventListener('DOMContentLoaded', function() {
            var progressBars = document.querySelectorAll('.budget-progress');
            progressBars.forEach(function(bar) {
                var width = bar.getAttribute('data-width');
                bar.style.width = width + '%';
            });
        });
        
        // Prepare chart data
        var chartLabels = [];
        var chartData = [];
        var chartColors = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#E7E9ED', '#C9CBCF'];
        
        <%if (categoryTotals != null && !categoryTotals.isEmpty()) {
            int colorIndex = 0;
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
        %>
            chartLabels.push('<%=entry.getKey()%>');
            chartData.push(<%=entry.getValue()%>);
        <%
                colorIndex++;
            }
        } else {
        %>
            chartLabels.push('No Data');
            chartData.push(1);
            chartColors = ['#CCCCCC'];
        <%}%>
        
        // Create chart
        var ctx = document.getElementById('categoryChart');
        if (ctx) {
            new Chart(ctx, {
                type: 'pie',
                data: {
                    labels: chartLabels,
                    datasets: [{
                        data: chartData,
                        backgroundColor: chartColors
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'bottom'
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    var label = context.label || '';
                                    if (label) {
                                        label += ': ';
                                    }
                                    label += '₹' + context.parsed.toFixed(2);
                                    return label;
                                }
                            }
                        }
                    }
                }
            });
        }
    </script>
</body>
</html>