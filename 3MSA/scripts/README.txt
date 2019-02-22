1)
create the following attributes and add them to the user class (mmc.exe -> file -> add/remove snap-in -> ad schema):

cNINumber (Unicode String)
rIBNumber (Unicode String)
employeeFirstActivationDone (Boolean)
employeeHireDate (Generalized Time)

2)
schtasks.exe /create /sc daily /st 00:00 /tn "ADEmployeeAccountActivation" /tr "powershell.exe C:\Scripts\ADEmployeeAccountActivationTask.ps1"