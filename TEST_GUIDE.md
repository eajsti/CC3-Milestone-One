# Smart Parking System - Test Guide

## Quick Start
```bash
cd src
javac -cp "..\lib\sqlite-jdbc-3.51.3.0.jar" -d . *.java
java -cp ".:..\lib\sqlite-jdbc-3.51.3.0.jar" Main
```

OR use the batch file:
```bash
run.bat
```

## Default Accounts
| Role    | Username  | Password |
|---------|-----------|----------|
| Admin   | admin     | admin123 |
| Officer | officer1  | 1234     |

## Test Scenarios

### 1. Admin Test (Add Zone)
```
2                    → Login
admin                → Username
admin123             → Password
1                    → Manage Zones
1                    → Add Zone
Zone A               → Zone Name
50                   → Hourly Rate
6                    → Back
8                    → Logout
3                    → Exit
```

### 2. Driver Test (Register Vehicle & Park)
```
1                    → Register
driver1               → Username
pass123               → Password
driver@test.com       → Email
09123456789          → Phone
Driver                → Role

2                    → Login
driver1              → Username
pass123              → Password

1                    → Register Vehicle
ABC123              → Plate Number
Toyota              → Make
Vios                → Model
White               → Color

5                    → Start Parking
1                    → Select Zone 1
1                    → Select Vehicle 1

6                    → End Parking
1                    → Select Vehicle 1

11                   → Logout
3                    → Exit
```

### 3. Officer Test (Issue Ticket)
```
1                    → Register
officer2             → Username
pass456              → Password
off@test.com         → Email
09129876543          → Phone
Officer              → Role

2                    → Login
officer2             → Username
pass456              → Password

1                    → Issue Ticket
1                    → Select Zone
ABC123              → Plate Number
1                    → Violation 1 (Expired Meter)

4                    → Logout
3                    → Exit
```

## System Features Checklist

- [x] Register User Account
- [x] Register Vehicle
- [x] Monitor Parking Area and Slot
- [x] Monitor Parking Sessions
- [x] Detect Violation and Ticket Issuance
- [x] Compute Fine and Penalty
- [x] View Alert and Notification
- [x] Password Encryption (SHA-256)