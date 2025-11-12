// Transaction Management JavaScript

// Get context path
const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2));

// Add Transaction Modal Functions
function showAddTransactionModal() {
    document.getElementById('addTransactionModal').style.display = 'block';
}

function closeAddTransactionModal() {
    document.getElementById('addTransactionModal').style.display = 'none';
    document.getElementById('addTransactionForm').reset();
}

// Edit Transaction Modal Functions
function showEditTransactionModal(id, type, amount, category, description, date) {
    document.getElementById('editTransactionModal').style.display = 'block';
    document.getElementById('editTransactionId').value = id;
    document.getElementById('editType').value = type;
    document.getElementById('editAmount').value = amount;
    document.getElementById('editCategory').value = category;
    document.getElementById('editDescription').value = description;
    document.getElementById('editDate').value = date;
}

function closeEditTransactionModal() {
    document.getElementById('editTransactionModal').style.display = 'none';
    document.getElementById('editTransactionForm').reset();
}

// Close modal when clicking outside
window.onclick = function(event) {
    const addModal = document.getElementById('addTransactionModal');
    const editModal = document.getElementById('editTransactionModal');
    
    if (event.target == addModal) {
        closeAddTransactionModal();
    }
    if (event.target == editModal) {
        closeEditTransactionModal();
    }
}

// Handle Add Transaction Form Submit
document.addEventListener('DOMContentLoaded', function() {
    const addForm = document.getElementById('addTransactionForm');
    
    if (addForm) {
        addForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            const data = {
                type: formData.get('type'),
                amount: formData.get('amount'),
                category: formData.get('category'),
                description: formData.get('description'),
                date: formData.get('date')
            };
            
            fetch(contextPath + '/transaction?action=add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams(data)
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    alert('Transaction added successfully!');
                    closeAddTransactionModal();
                    window.location.reload();
                } else {
                    alert('Error: ' + result.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred while adding the transaction.');
            });
        });
    }
    
    // Handle Edit Transaction Form Submit
    const editForm = document.getElementById('editTransactionForm');
    
    if (editForm) {
        editForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            const data = {
                transactionId: formData.get('transactionId'),
                type: formData.get('type'),
                amount: formData.get('amount'),
                category: formData.get('category'),
                description: formData.get('description'),
                date: formData.get('date')
            };
            
            fetch(contextPath + '/transaction?action=update', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams(data)
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    alert('Transaction updated successfully!');
                    closeEditTransactionModal();
                    window.location.reload();
                } else {
                    alert('Error: ' + result.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred while updating the transaction.');
            });
        });
    }
    
    // Search functionality
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            const rows = document.querySelectorAll('.transaction-table tbody tr');
            
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                row.style.display = text.includes(searchTerm) ? '' : 'none';
            });
        });
    }
    
    // Filter functionality
    const typeFilter = document.getElementById('typeFilter');
    const categoryFilter = document.getElementById('categoryFilter');
    
    if (typeFilter) {
        typeFilter.addEventListener('change', applyFilters);
    }
    if (categoryFilter) {
        categoryFilter.addEventListener('change', applyFilters);
    }
});

function applyFilters() {
    const typeFilter = document.getElementById('typeFilter').value;
    const categoryFilter = document.getElementById('categoryFilter').value;
    const rows = document.querySelectorAll('.transaction-table tbody tr');
    
    rows.forEach(row => {
        const type = row.querySelector('.badge').textContent.trim();
        const category = row.cells[2].textContent.trim();
        
        let showRow = true;
        
        if (typeFilter && type !== typeFilter) {
            showRow = false;
        }
        
        if (categoryFilter && category !== categoryFilter) {
            showRow = false;
        }
        
        row.style.display = showRow ? '' : 'none';
    });
}

// Delete Transaction Function
function deleteTransaction(id) {
    if (confirm('Are you sure you want to delete this transaction?')) {
        fetch(contextPath + '/transaction?action=delete&transactionId=' + id, {
            method: 'POST'
        })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                alert('Transaction deleted successfully!');
                window.location.reload();
            } else {
                alert('Error: ' + result.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while deleting the transaction.');
        });
    }
}