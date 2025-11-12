<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.finance.models.*" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("../login.jsp");
        return;
    }
    
    String username = (String) session.getAttribute("username");
    List<Transaction> transactions = (List<Transaction>) request.getAttribute("transactions");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Transactions - Personal Finance Tracker</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body>
    <div class="container">
        <nav class="navbar">
            <div class="nav-brand">Finance Tracker</div>
            <ul class="nav-menu">
                <li><a href="<%=request.getContextPath()%>/dashboard">Dashboard</a></li>
                <li><a href="<%=request.getContextPath()%>/transaction?action=list" class="active">Transactions</a></li>
                <li><a href="<%=request.getContextPath()%>/budget">Budget</a></li>
                <li><a href="<%=request.getContextPath()%>/report">Reports</a></li>
                <li><a href="<%=request.getContextPath()%>/logout">Logout (<%=username%>)</a></li>
            </ul>
        </nav>
        
        <div class="page-header">
            <h1>Manage Transactions</h1>
            <button class="btn btn-primary" onclick="showAddTransactionModal()">+ Add Transaction</button>
        </div>
        
        <div class="card">
            <div class="filter-section">
                <input type="text" id="searchInput" placeholder="Search transactions..." class="search-input">
                
                <select id="typeFilter" class="filter-select">
                    <option value="">All Types</option>
                    <option value="INCOME">Income</option>
                    <option value="EXPENSE">Expense</option>
                </select>
                
                <select id="categoryFilter" class="filter-select">
                    <option value="">All Categories</option>
                    <option value="Salary">Salary</option>
                    <option value="Freelance">Freelance</option>
                    <option value="Investment">Investment</option>
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
            
            <%if (transactions != null && !transactions.isEmpty()) {%>
                <table class="transaction-table">
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Type</th>
                            <th>Category</th>
                            <th>Description</th>
                            <th>Amount</th>
                            <th>Actions</th>
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
                                <td>
                                    <button class="btn-icon" onclick="showEditTransactionModal('<%=transaction.getTransactionId()%>', '<%=transaction.getType()%>', '<%=transaction.getAmount()%>', '<%=transaction.getCategory()%>', '<%=transaction.getDescription()%>', '<%=transaction.getDate()%>')">✏️</button>
                                    <button class="btn-icon" onclick="deleteTransaction('<%=transaction.getTransactionId()%>')">🗑️</button>
                                </td>
                            </tr>
                        <%}%>
                    </tbody>
                </table>
            <%} else {%>
                <p class="empty-state">No transactions yet. Click "Add Transaction" to create one!</p>
            <%}%>
        </div>
    </div>
    
    <!-- Add Transaction Modal -->
    <div id="addTransactionModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeAddTransactionModal()">&times;</span>
            <h2>Add Transaction</h2>
            <form id="addTransactionForm">
                <div class="form-group">
                    <label for="type">Type</label>
                    <select id="type" name="type" required>
                        <option value="INCOME">Income</option>
                        <option value="EXPENSE">Expense</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="amount">Amount</label>
                    <input type="number" id="amount" name="amount" step="0.01" min="0" required>
                </div>
                
                <div class="form-group">
                    <label for="category">Category</label>
                    <select id="category" name="category" required>
                        <optgroup label="Income">
                            <option value="Salary">Salary</option>
                            <option value="Freelance">Freelance</option>
                            <option value="Investment">Investment</option>
                            <option value="Other">Other</option>
                        </optgroup>
                        <optgroup label="Expense">
                            <option value="Food">Food</option>
                            <option value="Travel">Travel</option>
                            <option value="Bills">Bills</option>
                            <option value="Shopping">Shopping</option>
                            <option value="Entertainment">Entertainment</option>
                            <option value="Healthcare">Healthcare</option>
                            <option value="Education">Education</option>
                            <option value="Other">Other</option>
                        </optgroup>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="description">Description</label>
                    <input type="text" id="description" name="description" required>
                </div>
                
                <div class="form-group">
                    <label for="date">Date</label>
                    <input type="date" id="date" name="date" required>
                </div>
                
                <button type="submit" class="btn btn-primary">Add Transaction</button>
            </form>
        </div>
    </div>
    
    <!-- Edit Transaction Modal -->
    <div id="editTransactionModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeEditTransactionModal()">&times;</span>
            <h2>Edit Transaction</h2>
            <form id="editTransactionForm">
                <input type="hidden" id="editTransactionId" name="transactionId">
                
                <div class="form-group">
                    <label for="editType">Type</label>
                    <select id="editType" name="type" required>
                        <option value="INCOME">Income</option>
                        <option value="EXPENSE">Expense</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="editAmount">Amount</label>
                    <input type="number" id="editAmount" name="amount" step="0.01" min="0" required>
                </div>
                
                <div class="form-group">
                    <label for="editCategory">Category</label>
                    <select id="editCategory" name="category" required>
                        <optgroup label="Income">
                            <option value="Salary">Salary</option>
                            <option value="Freelance">Freelance</option>
                            <option value="Investment">Investment</option>
                            <option value="Other">Other</option>
                        </optgroup>
                        <optgroup label="Expense">
                            <option value="Food">Food</option>
                            <option value="Travel">Travel</option>
                            <option value="Bills">Bills</option>
                            <option value="Shopping">Shopping</option>
                            <option value="Entertainment">Entertainment</option>
                            <option value="Healthcare">Healthcare</option>
                            <option value="Education">Education</option>
                            <option value="Other">Other</option>
                        </optgroup>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="editDescription">Description</label>
                    <input type="text" id="editDescription" name="description" required>
                </div>
                
                <div class="form-group">
                    <label for="editDate">Date</label>
                    <input type="date" id="editDate" name="date" required>
                </div>
                
                <button type="submit" class="btn btn-primary">Update Transaction</button>
            </form>
        </div>
    </div>
    
    <script src="<%=request.getContextPath()%>/js/transactions.js"></script>
</body>
</html>