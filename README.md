# Smart Parking Ticketing System

A Java-based console application for managing parking sessions, tickets, and fines.

## Features
- User registration and login (Driver, Officer, Admin roles)
- Vehicle management
- Parking session tracking with fee calculation
- Ticket issuance and fine management
- Zone and slot management for admins
- Notification system
- Payment framework integration with VAT and discount support
- Charged transactions for parking sessions and fines
- Income statement and payment history reports
- Bug fixes: Prevent duplicate parking sessions, improved vehicle feedback, role input normalization

## Recent Updates (M4 Integration)
- Integrated payment framework from M4 group code
- Added PaymentFramework.java (abstract class for payment processing with VAT 12% and discount logic)
- Added ReservationPayment.java (concrete implementation for reservation payments)
- Added PaymentService.java (handles transaction charging and database recording)
- Added FinanceService.java (provides income statement and payment history reports)
- Updated ParkingSessionService.java to charge payments on session end
- Updated FineService.java to charge payments for fine settlements
- Added Payments table to database schema
- Enhanced driver menu with "View My Payments" option
- Enhanced admin menu with "View Income Statement" and "View Payment History" options
- Fixed bug: Prevent starting duplicate parking sessions
- Fixed bug: Improved feedback when no vehicles are registered
- Fixed bug: Normalized role input during registration (e.g., "driver" -> "Driver")

## Project Structure
```
src/
├── Main.java              - Entry point and menu system
├── DBConnection.java     - SQLite database connection
├── DatabaseSetup.java    - Database schema initialization
├── UserService.java      - User management (register, login, profile)
├── VehicleService.java    - Vehicle CRUD operations
├── ParkingAreaService.java - Zone and slot management
├── ParkingSessionService.java - Parking session tracking
├── TicketService.java     - Ticket issuance and management
├── FineService.java      - Fine and penalty management
├── NotificationService.java - Notification handling
├── PaymentFramework.java - Abstract payment framework with VAT/discount
├── ReservationPayment.java - Concrete payment class for reservations
├── PaymentService.java    - Transaction charging and recording
└── FinanceService.java    - Financial reports (income statement, payment history)
```

## Prerequisites
- Java Development Kit (JDK)
- SQLite JDBC library (included in lib/)

## How to Run
```bash
cd src
javac -d . *.java
java Main
```

## Default Accounts
| Role    | Username  | Password |
|---------|-----------|----------|
| Admin   | admin     | admin123 |
| Officer | officer1  | 1234     |

## User Roles
- **Driver**: Register vehicles, start/end parking sessions, view tickets, pay fines, view my payments
- **Officer**: Issue tickets, view tickets, manage fines
- **Admin**: Manage zones/slots, view all sessions/tickets, apply penalties, manage users, view income statement, view payment history

## Tech Stack
- Java
- SQLite (JDBC)

## Database
The system automatically creates the following tables on first run:
- Users, Vehicles, Zones, Slots, Sessions, Tickets, Notifications, Payments