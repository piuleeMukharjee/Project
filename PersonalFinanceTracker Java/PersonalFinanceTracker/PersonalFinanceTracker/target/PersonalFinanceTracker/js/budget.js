// Budget Management JavaScript

// Get context path
const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2));

// Add Budget Modal Functions
function showAddBudgetModal() {
    document.getElementById('addBudgetModal').style.display = 'block';
}

function closeAddBudgetModal() {
    document.getElementById('addBudgetModal').style.display = 'none';
    document.getElementById('addBudgetForm').reset();
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('addBudgetModal');
    if (event.target == modal) {
        closeAddBudgetModal();
    }
}

// Handle Add Budget Form Submit
document.addEventListener('DOMContentLoaded', function() {
    const addForm = document.getElementById('addBudgetForm');
    
    if (addForm) {
        addForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            const data = {
                category: formData.get('category'),
                budgetAmount: formData.get('budgetAmount'),
                month: formData.get('month'),
                year: formData.get('year')
            };
            
            fetch(contextPath + '/budget?action=add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams(data)
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    alert('Budget set successfully!');
                    closeAddBudgetModal();
                    window.location.reload();
                } else {
                    alert('Error: ' + result.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred while setting the budget.');
            });
        });
    }
});

// Delete Budget Function
function deleteBudget(id) {
    if (confirm('Are you sure you want to delete this budget?')) {
        fetch(contextPath + '/budget?action=delete&budgetId=' + id, {
            method: 'POST'
        })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                alert('Budget deleted successfully!');
                window.location.reload();
            } else {
                alert('Error: ' + result.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while deleting the budget.');
        });
    }
}