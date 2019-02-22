[void] [System.Reflection.Assembly]::LoadWithPartialName(“System.Windows.Forms”)
[System.Windows.Forms.Application]::EnableVisualStyles()

Function GetEmployees
{
    $listview_Employees.Items.Clear()
    $listview_Employees.Columns.Clear()
    $global:Employees = Get-ADUser -SearchBase “OU=Employees,DC=vetlux,DC=lan” -Filter * -Properties * | Select Name,SamAccountName,Department,MobilePhone,cNINumber,rIBNumber,StreetAddress
    $EmployeeProperties = $Employees[0].psObject.Properties
    $EmployeeProperties | ForEach-Object {
        $AttributeName = $_.Name
        $FriendlyName = switch ($AttributeName)
        {
            "Name"           { "Full Name" }
            "SamAccountName" { "Logon Name" }
            "MobilePhone"    { "Phone Number" }
            "cNINumber"      { "CNI Number" }
            "rIBNumber"      { "RIB Number" }
            "StreetAddress"  { "Address" }
            default          { $AttributeName }
        }
        $listview_Employees.Columns.Add($FriendlyName) | Out-Null
    }
    ForEach ($Employee in $global:Employees)
    {
        $EmployeeListViewItem = New-Object System.Windows.Forms.ListViewItem($Employee.Name)
        $Employee.psObject.Properties | Where {$_.Name -ne "Name"} | ForEach-Object {
            $ColumnName = $_.Name
            $EmployeeListViewItem.SubItems.Add("$($Employee.$ColumnName)") | Out-Null
        }
        $listview_Employees.Items.Add($EmployeeListViewItem) | Out-Null
    }
    $listview_Employees.AutoResizeColumns("HeaderSize")
}

#### EMPLOYEE LISTING FORM ####

$Form_EmployeeListing = New-Object System.Windows.Forms.Form
$Form_EmployeeListing.Text = "Employee Manager"
$Form_EmployeeListing.Size = New-Object System.Drawing.Size(832,528)
$Form_EmployeeListing.FormBorderStyle = "FixedDialog"
$Form_EmployeeListing.MaximizeBox  = $true
$Form_EmployeeListing.MinimizeBox  = $true
$Form_EmployeeListing.ControlBox = $true
$Form_EmployeeListing.StartPosition = "CenterScreen"
$Form_EmployeeListing.Font = "Segoe UI"

$label_EmployeeListing = New-Object System.Windows.Forms.Label
$label_EmployeeListing.Location = New-Object System.Drawing.Size(8,8)
$label_EmployeeListing.Size = New-Object System.Drawing.Size(150,32)
$label_EmployeeListing.TextAlign = "MiddleLeft"
$label_EmployeeListing.Text = "VETLUX Employee List"
$Form_EmployeeListing.Controls.Add($label_EmployeeListing)

$label_EmployeeFilter = New-Object System.Windows.Forms.Label
$label_EmployeeFilter.Location = New-Object System.Drawing.Size(600,8)
$label_EmployeeFilter.Size = New-Object System.Drawing.Size(40,32)
$label_EmployeeFilter.Anchor = [System.Windows.Forms.AnchorStyles]::Top -bor
[System.Windows.Forms.AnchorStyles]::Right
$label_EmployeeFilter.TextAlign = "MiddleLeft"
$label_EmployeeFilter.Text = "Filter"
$Form_EmployeeListing.Controls.Add($label_EmployeeFilter)

$textBox_EmployeeFilter = New-Object System.Windows.Forms.TextBox
$textBox_EmployeeFilter.Location = New-Object System.Drawing.Size(640,12)
$textBox_EmployeeFilter.Size = New-Object System.Drawing.Size(168,32)
$textBox_EmployeeFilter.Anchor = [System.Windows.Forms.AnchorStyles]::Top -bor
[System.Windows.Forms.AnchorStyles]::Right
$textBox_EmployeeFilter.Add_TextChanged({
    $listview_Employees.Items.Clear() | Out-Null
    ForEach ($Employee in $global:Employees)
    {
        if ($this.Text.Length -eq 0 -or $Employee.Name.ToLower().Contains("$($this.Text.ToLower())")) {
            $EmployeeListViewItem = New-Object System.Windows.Forms.ListViewItem($Employee.Name)
            $Employee.psObject.Properties | Where {$_.Name -ne "Name"} | ForEach-Object {
                $ColumnName = $_.Name
                $EmployeeListViewItem.SubItems.Add("$($Employee.$ColumnName)") | Out-Null
            }
            $listview_Employees.Items.Add($EmployeeListViewItem) | Out-Null
        }
    }
    $listview_Employees.AutoResizeColumns("HeaderSize")
})
$Form_EmployeeListing.Controls.Add($textBox_EmployeeFilter)

$Global:listview_Employees = New-Object System.Windows.Forms.ListView
$listview_Employees.Location = New-Object System.Drawing.Size(8,40)
$listview_Employees.Size = New-Object System.Drawing.Size(800,402)
$listview_Employees.Anchor = [System.Windows.Forms.AnchorStyles]::Bottom -bor
[System.Windows.Forms.AnchorStyles]::Right -bor 
[System.Windows.Forms.AnchorStyles]::Top -bor
[System.Windows.Forms.AnchorStyles]::Left
$listview_Employees.View = "Details"
$listview_Employees.FullRowSelect = $true
$listview_Employees.MultiSelect = $false
$listview_Employees.AllowColumnReorder = $true
$listview_Employees.GridLines = $true
$Form_EmployeeListing.Controls.Add($listview_Employees)

$button_GetEmployees = New-Object System.Windows.Forms.Button
$button_GetEmployees.Location = New-Object System.Drawing.Size(8,450)
$button_GetEmployees.Size = New-Object System.Drawing.Size(80,32)
$button_GetEmployees.Anchor = [System.Windows.Forms.AnchorStyles]::Bottom -bor
[System.Windows.Forms.AnchorStyles]::Left
$button_GetEmployees.TextAlign = "MiddleCenter"
$button_GetEmployees.Text = “Add New”
$button_GetEmployees.Add_Click({
    $Form_EditEmployee.Text = "New Employee"
    $textBox_EmployeeFirstName.Text = ""
    $textBox_EmployeeLastName.Text = ""
    $textBox_EmployeeLogonName.Text = ""
    $comboBox_EmployeeDept.SelectedIndex = 0
    $textBox_EmployeePhone.Text = ""
    $textBox_EmployeeCNI.Text = ""
    $textBox_EmployeeRIB.Text = ""
    $textBox_EmployeeAddress.Text = ""
    $dateTimePicker_EmployeeHireDate.Enabled = $true
    $dateTimePicker_EmployeeHireDate.Value = [DateTime]::Today
    $dateTimePicker_EmployeeHireDate.MinDate = [DateTime]::Today
    $checkBox_EmployeeEndDate.Checked = $false
    $dateTimePicker_EmployeeEndDate.Enabled = $false
    $dateTimePicker_EmployeeEndDate.Value = $([DateTime]::Today).AddDays(1)
    $dateTimePicker_EmployeeEndDate.MinDate = $([DateTime]::Today).AddDays(1)
    $Form_EditEmployee.Add_Shown({$Form_EditEmployee.Activate()})
    [Void] $Form_EditEmployee.ShowDialog()
})
$Form_EmployeeListing.Controls.Add($button_GetEmployees)

$button_Edit = New-Object System.Windows.Forms.Button
$button_Edit.Location = New-Object System.Drawing.Size(96,450)
$button_Edit.Size = New-Object System.Drawing.Size(100,32)
$button_Edit.Anchor = [System.Windows.Forms.AnchorStyles]::Bottom -bor
[System.Windows.Forms.AnchorStyles]::Left
$button_Edit.TextAlign = "MiddleCenter"
$button_Edit.Text = "Edit Selected"
$button_Edit.Add_Click({
    $selection = $listview_Employees.SelectedIndices
    if ($selection.Count -eq 1) {
        $LogonNameColumnIndex = ($listview_Employees.Columns | Where {$_.Text -eq "Logon Name"}).Index
        $textBox_EmployeeLogonName.Text = ($listview_Employees.Items[$selection[0]].SubItems[$LogonNameColumnIndex]).Text
        $Employee = Get-ADUser -Identity "$($textBox_EmployeeLogonName.Text)" -Properties *
        $Form_EditEmployee.Text = "Edit Employee"
        $dateTimePicker_EmployeeHireDate.Enabled = $false
        $dateTimePicker_EmployeeHireDate.MinDate = [DateTime]::Today
        $dateTimePicker_EmployeeHireDate.Value = [DateTime]::Today
        $dateTimePicker_EmployeeEndDate.MinDate = $([DateTime]::Today).AddDays(1)
        $dateTimePicker_EmployeeEndDate.Value = $([DateTime]::Today).AddDays(1)
        if ($Employee.employeeHireDate) {
            if ($(New-TimeSpan $([DateTime]::Today) $employee.employeeHireDate).Days -lt 0) {
                $dateTimePicker_EmployeeHireDate.MinDate = $employee.employeeHireDate
            } else {
                $dateTimePicker_EmployeeHireDate.MinDate = [DateTime]::Today
                if (-not $employee.employeeFirstActivationDone) {
                    $dateTimePicker_EmployeeHireDate.Enabled = $true
                }
            }
            $dateTimePicker_EmployeeHireDate.Value = $employee.employeeHireDate
        }
        if (-not ($Employee.accountExpires -eq 0 -or $Employee.accountExpires -eq 9223372036854775807)) {
            if ($(New-TimeSpan $(Get-Date) $([DateTime]::FromFileTimeUTC($employee.accountExpires))).Days -lt 0) {
                $dateTimePicker_EmployeeEndDate.MinDate = [DateTime]::FromFileTimeUTC($Employee.accountExpires)
            } else {
                $dateTimePicker_EmployeeEndDate.MinDate = $([DateTime]::Today).AddDays(1)
            }
            $dateTimePicker_EmployeeEndDate.Value = [DateTime]::FromFileTimeUTC($Employee.accountExpires)
            $checkBox_EmployeeEndDate.Checked = $true
            $dateTimePicker_EmployeeEndDate.Enabled = $true
        } else {
            $checkBox_EmployeeEndDate.Checked = $false
            $dateTimePicker_EmployeeEndDate.Enabled = $false
        }
        $textBox_EmployeeFirstName.Text = $Employee.GivenName
        $textBox_EmployeeLastName.Text = $Employee.Surname
        $comboBox_EmployeeDept.SelectedIndex = 0
        if ($Employee.CanonicalName.StartsWith("vetlux.lan/Employees/Marketing/")) {
            $comboBox_EmployeeDept.SelectedIndex = 1
        } elseif ($Employee.CanonicalName.StartsWith("vetlux.lan/Employees/Sales/")) {
            $comboBox_EmployeeDept.SelectedIndex = 2
        }
        $textBox_EmployeePhone.Text = $Employee.MobilePhone
        $textBox_EmployeeCNI.Text = $Employee.cNINumber
        $textBox_EmployeeRIB.Text = $Employee.rIBNumber
        $textBox_EmployeeAddress.Text = $Employee.StreetAddress
        $Form_EditEmployee.Add_Shown({$Form_EditEmployee.Activate()})
        [Void] $Form_EditEmployee.ShowDialog()
    }
})
$Form_EmployeeListing.Controls.Add($button_Edit)

$button_Delete = New-Object System.Windows.Forms.Button
$button_Delete.Location = New-Object System.Drawing.Size(708,450)
$button_Delete.Size = New-Object System.Drawing.Size(100,32)
$button_Delete.Anchor = [System.Windows.Forms.AnchorStyles]::Bottom -bor
[System.Windows.Forms.AnchorStyles]::Right
$button_Delete.TextAlign = "MiddleCenter"
$button_Delete.Text = “Delete Selected”
$button_Delete.Add_Click({
    $selection = $listview_Employees.SelectedIndices
    if ($selection.Count -eq 1) {
        $LogonNameColumnIndex = ($listview_Employees.Columns | Where {$_.Text -eq "Logon Name"}).Index
        $Identity = ($listview_Employees.Items[$selection[0]].SubItems[$LogonNameColumnIndex]).Text
        $FullName = ($listview_Employees.Items[$selection[0]].SubItems[0]).Text
        $result = [System.Windows.Forms.MessageBox]::Show("Are you sure you want to delete the account of $($FullName)?" , "Warning" , "YesNo")
        if ($result -eq 'Yes') {
            Remove-ADUser -Identity $Identity -Confirm:$false
            GetEmployees
        }
    }
})
$Form_EmployeeListing.Controls.Add($button_Delete)


#### EMPLOYEE EDIT FORM ####

$Form_EditEmployee = New-Object System.Windows.Forms.Form
$Form_EditEmployee.Text = “Add Employee”
$Form_EditEmployee.Size = New-Object System.Drawing.Size(360,430)
$Form_EditEmployee.FormBorderStyle = "FixedDialog"
$Form_EditEmployee.MaximizeBox  = $false
$Form_EditEmployee.MinimizeBox  = $false
$Form_EditEmployee.ControlBox = $true
$Form_EditEmployee.StartPosition = “CenterScreen”

$label_EmployeeFirstName = New-Object System.Windows.Forms.Label
$label_EmployeeFirstName.Location = New-Object System.Drawing.Size(8,8)
$label_EmployeeFirstName.Size = New-Object System.Drawing.Size(86,32)
$label_EmployeeFirstName.TextAlign = "MiddleLeft"
$label_EmployeeFirstName.Text = “First Name”
$Form_EditEmployee.Controls.Add($label_EmployeeFirstName)
$textBox_EmployeeFirstName = New-Object System.Windows.Forms.TextBox
$textBox_EmployeeFirstName.Location = New-Object System.Drawing.Size(100,12)
$textBox_EmployeeFirstName.Size = New-Object System.Drawing.Size(230,32)
$textBox_EmployeeFirstName.Add_TextChanged({
    $curPos = $this.SelectionStart
    $this.Text = $this.Text -replace '[^- a-zA-Z]', ''
    $textBox_EmployeeFirstName.Text = (Get-Culture).TextInfo.ToTitleCase($textBox_EmployeeFirstName.Text.tolower())
    $textBox_EmployeeLastName.Text = $textBox_EmployeeLastName.Text.toupper()
    $this.SelectionStart = $curPos
    
    if (-not ($Form_EditEmployee.Text.StartsWith("Edit"))) {
        if ($textBox_EmployeeFirstName.Text.Length -gt 0 -And $textBox_EmployeeLastName.Text.Length -gt 0) {
            $textBox_EmployeeLogonName.Text = "$($textBox_EmployeeFirstName.Text.SubString(0, 1)).$($textBox_EmployeeLastName.Text -replace '[- ]', '')".ToLower()
        }
        else {
            $textBox_EmployeeLogonName.Text = ""
        }
    }
})
$Form_EditEmployee.Controls.Add($textBox_EmployeeFirstName)

$label_EmployeeLastName = New-Object System.Windows.Forms.Label
$label_EmployeeLastName.Location = New-Object System.Drawing.Size(8,40)
$label_EmployeeLastName.Size = New-Object System.Drawing.Size(86,32)
$label_EmployeeLastName.TextAlign = "MiddleLeft"
$label_EmployeeLastName.Text = “Last Name”
$Form_EditEmployee.Controls.Add($label_EmployeeLastName)
$textBox_EmployeeLastName = New-Object System.Windows.Forms.TextBox
$textBox_EmployeeLastName.Location = New-Object System.Drawing.Size(100,44)
$textBox_EmployeeLastName.Size = New-Object System.Drawing.Size(230,32)
$textBox_EmployeeLastName.Add_TextChanged({
    $curPos = $this.SelectionStart
    $this.Text = $this.Text -replace '[^- a-zA-Z]', ''
    $textBox_EmployeeFirstName.Text = (Get-Culture).TextInfo.ToTitleCase($textBox_EmployeeFirstName.Text.tolower())
    $textBox_EmployeeLastName.Text = $textBox_EmployeeLastName.Text.toupper()
    $this.SelectionStart = $curPos
    
    if (-not ($Form_EditEmployee.Text.StartsWith("Edit"))) {
        if ($textBox_EmployeeFirstName.Text.Length -gt 0 -And $textBox_EmployeeLastName.Text.Length -gt 0) {
            $textBox_EmployeeLogonName.Text = "$($textBox_EmployeeFirstName.Text.SubString(0, 1)).$($textBox_EmployeeLastName.Text -replace '[- ]', '')".ToLower()
        }
        else {
            $textBox_EmployeeLogonName.Text = ""
        }
    }
})
$Form_EditEmployee.Controls.Add($textBox_EmployeeLastName)

$label_EmployeeLogonName = New-Object System.Windows.Forms.Label
$label_EmployeeLogonName.Location = New-Object System.Drawing.Size(8,72)
$label_EmployeeLogonName.Size = New-Object System.Drawing.Size(86,32)
$label_EmployeeLogonName.TextAlign = "MiddleLeft"
$label_EmployeeLogonName.Text = “Logon Name”
$Form_EditEmployee.Controls.Add($label_EmployeeLogonName)
$textBox_EmployeeLogonName = New-Object System.Windows.Forms.TextBox
$textBox_EmployeeLogonName.Location = New-Object System.Drawing.Size(100,76)
$textBox_EmployeeLogonName.Size = New-Object System.Drawing.Size(230,32)
$textBox_EmployeeLogonName.Enabled = $false
$Form_EditEmployee.Controls.Add($textBox_EmployeeLogonName)


$label_EmployeeDept = New-Object System.Windows.Forms.Label
$label_EmployeeDept.Location = New-Object System.Drawing.Size(8,104)
$label_EmployeeDept.Size = New-Object System.Drawing.Size(86,32)
$label_EmployeeDept.TextAlign = "MiddleLeft"
$label_EmployeeDept.Text = “Department”
$Form_EditEmployee.Controls.Add($label_EmployeeDept)
$comboBox_EmployeeDept = New-Object System.Windows.Forms.ComboBox
$comboBox_EmployeeDept.Location = New-Object System.Drawing.Size(100,108)
$comboBox_EmployeeDept.Size = New-Object System.Drawing.Size(230,32)
$comboBox_EmployeeDept.DropDownStyle = "DropDownList"
$comboBox_EmployeeDept.Items.Add("IT")
$comboBox_EmployeeDept.Items.Add("Marketing")
$comboBox_EmployeeDept.Items.Add("Sales")
$comboBox_EmployeeDept.SelectedIndex = 0
$Form_EditEmployee.Controls.Add($comboBox_EmployeeDept)

$label_EmployeePhone = New-Object System.Windows.Forms.Label
$label_EmployeePhone.Location = New-Object System.Drawing.Size(8,136)
$label_EmployeePhone.Size = New-Object System.Drawing.Size(86,32)
$label_EmployeePhone.TextAlign = "MiddleLeft"
$label_EmployeePhone.Text = “Phone Number”
$Form_EditEmployee.Controls.Add($label_EmployeePhone)
$textBox_EmployeePhone = New-Object System.Windows.Forms.TextBox
$textBox_EmployeePhone.Location = New-Object System.Drawing.Size(100,140)
$textBox_EmployeePhone.Size = New-Object System.Drawing.Size(230,32)
$textBox_EmployeePhone.MaxLength = 15
$textBox_EmployeePhone.Add_TextChanged({
    $curPos = $this.SelectionStart
    $this.Text = $this.Text -replace '[^0-9]', ''
    $this.SelectionStart = $curPos
})
$Form_EditEmployee.Controls.Add($textBox_EmployeePhone)

$label_EmployeeCNI = New-Object System.Windows.Forms.Label
$label_EmployeeCNI.Location = New-Object System.Drawing.Size(8,168)
$label_EmployeeCNI.Size = New-Object System.Drawing.Size(86,32)
$label_EmployeeCNI.TextAlign = "MiddleLeft"
$label_EmployeeCNI.Text = “CNI Number”
$Form_EditEmployee.Controls.Add($label_EmployeeCNI)
$textBox_EmployeeCNI = New-Object System.Windows.Forms.TextBox
$textBox_EmployeeCNI.Location = New-Object System.Drawing.Size(100,172)
$textBox_EmployeeCNI.Size = New-Object System.Drawing.Size(230,32)
$Form_EditEmployee.Controls.Add($textBox_EmployeeCNI)

$label_EmployeeRIB = New-Object System.Windows.Forms.Label
$label_EmployeeRIB.Location = New-Object System.Drawing.Size(8,200)
$label_EmployeeRIB.Size = New-Object System.Drawing.Size(86,32)
$label_EmployeeRIB.TextAlign = "MiddleLeft"
$label_EmployeeRIB.Text = “RIB Number”
$Form_EditEmployee.Controls.Add($label_EmployeeRIB)
$textBox_EmployeeRIB = New-Object System.Windows.Forms.TextBox
$textBox_EmployeeRIB.Location = New-Object System.Drawing.Size(100,204)
$textBox_EmployeeRIB.Size = New-Object System.Drawing.Size(230,32)
$Form_EditEmployee.Controls.Add($textBox_EmployeeRIB)

$label_EmployeeAddress = New-Object System.Windows.Forms.Label
$label_EmployeeAddress.Location = New-Object System.Drawing.Size(8,232)
$label_EmployeeAddress.Size = New-Object System.Drawing.Size(86,32)
$label_EmployeeAddress.TextAlign = "MiddleLeft"
$label_EmployeeAddress.Text = “Address”
$Form_EditEmployee.Controls.Add($label_EmployeeAddress)
$textBox_EmployeeAddress = New-Object System.Windows.Forms.TextBox
$textBox_EmployeeAddress.Location = New-Object System.Drawing.Size(100,236)
$textBox_EmployeeAddress.Size = New-Object System.Drawing.Size(230,32)
$textBox_EmployeeAddress.Multiline = $true
$textBox_EmployeeAddress.ScrollBars = "Both"
$Form_EditEmployee.Controls.Add($textBox_EmployeeAddress)

$label_EmployeeHireDate = New-Object System.Windows.Forms.Label
$label_EmployeeHireDate.Location = New-Object System.Drawing.Size(8,272)
$label_EmployeeHireDate.Size = New-Object System.Drawing.Size(86,32)
$label_EmployeeHireDate.TextAlign = "MiddleLeft"
$label_EmployeeHireDate.Text = “Hire Date”
$Form_EditEmployee.Controls.Add($label_EmployeeHireDate)
$dateTimePicker_EmployeeHireDate = New-Object System.Windows.Forms.DateTimePicker
$dateTimePicker_EmployeeHireDate.Location = New-Object System.Drawing.Size(100,278)
$dateTimePicker_EmployeeHireDate.Size = New-Object System.Drawing.Size(230,32)
$Form_EditEmployee.Controls.Add($dateTimePicker_EmployeeHireDate)

$checkBox_EmployeeEndDate = New-Object System.Windows.Forms.CheckBox
$checkBox_EmployeeEndDate.Location = New-Object System.Drawing.Size(8,304)
$checkBox_EmployeeEndDate.Size = New-Object System.Drawing.Size(86,32)
$checkBox_EmployeeEndDate.TextAlign = "MiddleLeft"
$checkBox_EmployeeEndDate.Text = “End Date”
$checkBox_EmployeeEndDate.Add_CheckedChanged({
    if ($this.Checked) {
        $dateTimePicker_EmployeeEndDate.Enabled = $true
    } else {
        $dateTimePicker_EmployeeEndDate.Enabled = $false
    }
})
$Form_EditEmployee.Controls.Add($checkBox_EmployeeEndDate)
$dateTimePicker_EmployeeEndDate = New-Object System.Windows.Forms.DateTimePicker
$dateTimePicker_EmployeeEndDate.Enabled = $false
$dateTimePicker_EmployeeEndDate.Location = New-Object System.Drawing.Size(100,310)
$dateTimePicker_EmployeeEndDate.Size = New-Object System.Drawing.Size(230,32)
$Form_EditEmployee.Controls.Add($dateTimePicker_EmployeeEndDate)

$button_Save = New-Object System.Windows.Forms.Button
$button_Save.Location = New-Object System.Drawing.Size(252,345)
$button_Save.Size = New-Object System.Drawing.Size(80,32)
$button_Save.Anchor = [System.Windows.Forms.AnchorStyles]::Bottom -bor
[System.Windows.Forms.AnchorStyles]::Left
$button_Save.TextAlign = "MiddleCenter"
$button_Save.Text = “Save”
$button_Save.Add_Click({
    if ($textBox_EmployeeFirstName.Text.Length -eq 0 -or
        $textBox_EmployeeLastName.Text.Length -eq 0 -or
        $textBox_EmployeePhone.Text.Length -eq 0 -or
        $textBox_EmployeeAddress.Text.Length -eq 0 -or
        $textBox_EmployeeCNI.Text.Length -eq 0 -or
        $textBox_EmployeeRIB.Text.Length -eq 0) {
        [System.Windows.Forms.MessageBox]::Show("All fields are mandatory" , "Warning" , "Ok")
        return
    }
    <#if ($textBox_EmployeeFirstName.Text.Length -eq 0) { $textBox_EmployeeFirstName.Text = "N\A" }
    if ($textBox_EmployeeLastName.Text.Length -eq 0) { $textBox_EmployeeLastName.Text = "N\A" }
    if ($textBox_EmployeePhone.Text.Length -eq 0) { $textBox_EmployeePhone.Text = "N\A" }
    if ($textBox_EmployeeAddress.Text.Length -eq 0) { $textBox_EmployeeAddress.Text = "N\A" }
    if ($textBox_EmployeeCNI.Text.Length -eq 0) { $textBox_EmployeeCNI.Text = "N\A" }
    if ($textBox_EmployeeRIB.Text.Length -eq 0) { $textBox_EmployeeRIB.Text = "N\A" }#>

    if ($checkBox_EmployeeEndDate.Checked) {
        $diff = $(New-TimeSpan $dateTimePicker_EmployeeHireDate.Value $dateTimePicker_EmployeeEndDate.Value).Days
        if ($diff -eq 0) {
            [System.Windows.Forms.MessageBox]::Show("An employee cannot resign the same day he's hired" , "Warning" , "Ok")
            return
        }
        elseif ($diff -lt 0) {
            [System.Windows.Forms.MessageBox]::Show("An employee cannot resign before being hired" , "Warning" , "Ok")
            return
        }
    }

    if ($Form_EditEmployee.Text.StartsWith("Edit")) {
        $ADUser = Get-ADUser -Identity "$($textBox_EmployeeLogonName.Text)"
        $ADUser | Set-ADUser -GivenName "$($textBox_EmployeeFirstName.Text)"
        $ADUser | Set-ADUser -Surname "$($textBox_EmployeeLastName.Text)"
        $ADUser | Set-ADUser -DisplayName "$($textBox_EmployeeFirstName.Text + " " + $textBox_EmployeeLastName.Text)"
        $ADUser | Set-ADUser -Department "$($comboBox_EmployeeDept.Text)"
        $ADUser | Set-ADUser -MobilePhone "$($textBox_EmployeePhone.Text)"
        $ADUser | Set-ADUser -StreetAddress "$($textBox_EmployeeAddress.Text)"
        $ADUser | Set-ADUser -Replace @{ cNINumber = "$($textBox_EmployeeCNI.Text)" }
        $ADUser | Set-ADUser -Replace @{ rIBNumber = "$($textBox_EmployeeRIB.Text)" }
        if ($checkBox_EmployeeEndDate.Checked) {
            $ADUser | Set-ADAccountExpiration -DateTime $dateTimePicker_EmployeeEndDate.Value
        } else {
            $ADUser | Clear-ADAccountExpiration
        }
        if ((-not $ADUser.employeeFirstActivationDone) -and $dateTimePicker_EmployeeHireDate.Enabled) {
            $ADUser | Set-ADUser -Replace @{ employeeHireDate = $dateTimePicker_EmployeeHireDate.Value }
        }
        $ADUser | Rename-ADObject -NewName "$($textBox_EmployeeFirstName.Text + " " + $textBox_EmployeeLastName.Text)"
        # should re-get object after rename
        $ADUser = Get-ADUser -Identity "$($textBox_EmployeeLogonName.Text)"
        $ADUser | Move-ADObject -TargetPath "OU=$($comboBox_EmployeeDept.Text),OU=Employees,DC=vetlux,DC=lan"
    }
    else {
        if ($(Get-ADUser -Filter {sAMAccountName -eq $textBox_EmployeeLogonName.Text}) -ne $null) {
            [System.Windows.Forms.MessageBox]::Show("Account already exists" , "Error" , "Ok")
            return
        }
        $Attributes = @{
           Enabled = $false
           ChangePasswordAtLogon = $true
           Name = "$($textBox_EmployeeFirstName.Text + " " + $textBox_EmployeeLastName.Text)"
           DisplayName = "$($textBox_EmployeeFirstName.Text + " " + $textBox_EmployeeLastName.Text)"
           UserPrincipalName = "$($textBox_EmployeeLogonName.Text)" + "@vetlux.lan"
           SamAccountName = "$($textBox_EmployeeLogonName.Text)"
           GivenName = "$($textBox_EmployeeFirstName.Text)"
           Surname = "$($textBox_EmployeeLastName.Text)"
           MobilePhone = "$($textBox_EmployeePhone.Text)"
           StreetAddress = "$($textBox_EmployeeAddress.Text)"
           Company = "VETLUX"
           Department = "$($comboBox_EmployeeDept.Text)"
           OtherAttributes = @{
              'cNINumber' = "$($textBox_EmployeeCNI.Text)"
              'rIBNumber' = "$($textBox_EmployeeRIB.Text)"
           }
           Path = "OU=$($comboBox_EmployeeDept.Text),OU=Employees,DC=vetlux,DC=lan"
           AccountPassword = "TotallyFakePassword123" | ConvertTo-SecureString -AsPlainText -Force
        }
        New-ADUser @Attributes
        $ADUser = Get-ADUser -Identity "$($textBox_EmployeeLogonName.Text)"
        if ($checkBox_EmployeeEndDate.Checked) {
            $ADUser | Set-ADAccountExpiration -DateTime $dateTimePicker_EmployeeEndDate.Value
        }
        $ADUser | Set-ADUser -Replace @{ employeeHireDate=$dateTimePicker_EmployeeHireDate.Value }
    }
    $ADUser = Get-ADUser -Identity "$($textBox_EmployeeLogonName.Text)" -Properties *
    if ($ADUser.employeeHireDate -and (-not $ADUser.employeeFirstActivationDone)) {
        if ($(New-TimeSpan $([DateTime]::Today) $ADUser.employeeHireDate).Days -le 1) {
            $ADUser | Enable-ADAccount
            $ADUser | Set-ADUser -Replace @{ employeeFirstActivationDone=$true }
            [System.Windows.Forms.MessageBox]::Show("Account activated" , "Info" , "Ok")
        }
    }
    $Form_EditEmployee.Close()
    GetEmployees
})
$Form_EditEmployee.Controls.Add($button_Save)


$Form_EmployeeListing.Add_Shown({$Form_EmployeeListing.Activate();GetEmployees})
[Void] $Form_EmployeeListing.ShowDialog()