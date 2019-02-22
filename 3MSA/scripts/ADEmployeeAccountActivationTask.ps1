$employees = Get-ADUser -SearchBase "OU=Employees,DC=vetlux,DC=lan" -Properties * -Filter *
foreach ($employee in $employees) {
    if ($employee.employeeHireDate -and (-not $employee.employeeFirstActivationDone)) {
        if ($(New-TimeSpan $([DateTime]::Today) $employee.employeeHireDate).Days -le 1) {
            $employee | Enable-ADAccount
            $employee | Set-ADUser -Replace @{ employeeFirstActivationDone=$true }
            continue
        }
    }
}