<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.finance.models.*" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("../login.jsp");
        return;
    }
    
    String username = (String) session.getAttribute("username");
    List<Transaction> transactions = (List<Transaction>) request.getAttribute("transactions");
    String selectedMonth = (String) request.getAttribute("selectedMonth");
    String selectedYear = (String) request.getAttribute("selectedYear");
    
    double totalIncome = 0;
    double totalExpense = 0;
    Map<String, Double> categoryTotals = new HashMap<String, Double>();
    
    if (transactions != null) {
        for (Transaction t : transactions) {
            if ("INCOME".equals(t.getType())) {
                totalIncome += t.getAmount();
            } else {
                totalExpense += t.getAmount();
                Double current = categoryTotals.get(t.getCategory());
                categoryTotals.put(t.getCategory(), (current != null ? current : 0.0) + t.getAmount());
            }
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reports - Personal Finance Tracker</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
</head>
<body>
    <div class="container">
        <nav class="navbar">
            <div class="nav-brand">Finance Tracker</div>
            <ul class="nav-menu">
                <li><a href="<%=request.getContextPath()%>/dashboard">Dashboard</a></li>
                <li><a href="<%=request.getContextPath()%>/transaction?action=list">Transactions</a></li>
                <li><a href="<%=request.getContextPath()%>/budget">Budget</a></li>
                <li><a href="<%=request.getContextPath()%>/report" class="active">Reports</a></li>
                <li><a href="<%=request.getContextPath()%>/logout">Logout (<%=username%>)</a></li>
            </ul>
        </nav>
        
        <div class="page-header">
            <h1>Financial Reports</h1>
            <div class="report-actions">
                <button class="btn btn-secondary" onclick="downloadXML()">📄 Download XML</button>
                <button class="btn btn-secondary" onclick="downloadCSV()">📊 Download CSV</button>
            </div>
        </div>
        
        <div class="card">
            <h2>Select Report Period</h2>
            <form method="get" action="<%=request.getContextPath()%>/report" class="report-filter">
                <div class="form-group">
                    <label for="month">Month</label>
                    <select id="month" name="month">
                        <option value="01" <%="01".equals(selectedMonth) ? "selected" : ""%>>January</option>
                        <option value="02" <%="02".equals(selectedMonth) ? "selected" : ""%>>February</option>
                        <option value="03" <%="03".equals(selectedMonth) ? "selected" : ""%>>March</option>
                        <option value="04" <%="04".equals(selectedMonth) ? "selected" : ""%>>April</option>
                        <option value="05" <%="05".equals(selectedMonth) ? "selected" : ""%>>May</option>
                        <option value="06" <%="06".equals(selectedMonth) ? "selected" : ""%>>June</option>
                        <option value="07" <%="07".equals(selectedMonth) ? "selected" : ""%>>July</option>
                        <option value="08" <%="08".equals(selectedMonth) ? "selected" : ""%>>August</option>
                        <option value="09" <%="09".equals(selectedMonth) ? "selected" : ""%>>September</option>
                        <option value="10" <%="10".equals(selectedMonth) ? "selected" : ""%>>October</option>
                        <option value="11" <%="11".equals(selectedMonth) ? "selected" : ""%>>November</option>
                        <option value="12" <%="12".equals(selectedMonth) ? "selected" : ""%>>December</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="year">Year</label>
                    <select id="year" name="year">
                        <%for (int i = 2020; i <= 2030; i++) {%>
                            <option value="<%=i%>" <%=String.valueOf(i).equals(selectedYear) ? "selected" : ""%>><%=i%></option>
                        <%}%>
                    </select>
                </div>
                
                <button type="submit" class="btn btn-primary">Generate Report</button>
            </form>
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
            
            <div class="stat-card savings <%=(totalIncome - totalExpense) >= 0 ? "positive" : "negative"%>">
                <h3>Net Savings</h3>
                <p class="stat-value">₹<%=String.format("%.2f", totalIncome - totalExpense)%></p>
            </div>
        </div>
        
        <div class="content-grid">
            <div class="card">
                <h2>Expense Distribution</h2>
                <canvas id="expenseChart"></canvas>
            </div>
            
            <div class="card">
                <h2>Income vs Expense</h2>
                <canvas id="comparisonChart"></canvas>
            </div>
        </div>
        
        <div class="card">
            <h2>Detailed Transaction Report</h2>
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
                        <%for (Transaction transaction : transactions) {%>
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
            <%} else {%>
                <p class="empty-state">No transactions found for the selected period.</p>
            <%}%>
        </div>
    </div>
    
    <script>
        var selectedMonth = '<%=selectedMonth%>';
        var selectedYear = '<%=selectedYear%>';
        var contextPath = '<%=request.getContextPath()%>';
        
        function downloadXML() {
            window.location.href = contextPath + '/report?action=downloadXML&month=' + selectedMonth + '&year=' + selectedYear;
        }
        
        function downloadCSV() {
            window.location.href = contextPath + '/report?action=downloadCSV&month=' + selectedMonth + '&year=' + selectedYear;
        }
        
        // Expense Distribution Chart
        var expenseLabels = [];
        var expenseData = [];
        var expenseColors = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#E7E9ED', '#C9CBCF'];
        
        <%if (!categoryTotals.isEmpty()) {
            int colorIndex = 0;
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
        %>
            expenseLabels.push('<%=entry.getKey()%>');
            expenseData.push(<%=entry.getValue()%>);
        <%
                colorIndex++;
            }
        } else {
        %>
            expenseLabels.push('No Data');
            expenseData.push(1);
            expenseColors = ['#CCCCCC'];
        <%}%>
        
        var ctx1 = document.getElementById('expenseChart');
        if (ctx1) {
            new Chart(ctx1, {
                type: 'doughnut',
                data: {
                    labels: expenseLabels,
                    datasets: [{
                        data: expenseData,
                        backgroundColor: expenseColors
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
        
        // Income vs Expense Chart
        var comparisonData = {
            labels: ['Income', 'Expense'],
            datasets: [{
                data: [<%=totalIncome%>, <%=totalExpense%>],
                backgroundColor: ['#4CAF50', '#F44336']
            }]
        };
        
        var ctx2 = document.getElementById('comparisonChart');
        if (ctx2) {
            new Chart(ctx2, {
                type: 'bar',
                data: comparisonData,
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return context.label + ': ₹' + context.parsed.y.toFixed(2);
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                callback: function(value) {
                                    return '₹' + value;
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