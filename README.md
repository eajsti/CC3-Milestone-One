# Smart Parking Ticketing System

A Java-based console application for managing parking sessions, tickets, and fines.

## Features
- User registration and login (Driver, Officer, Admin roles)
- Vehicle management
- Parking session tracking with fee calculation
- Ticket issuance and fine management
- Zone and slot management for admins
- Notification system

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
└── NotificationService.java - Notification handling
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
- **Driver**: Register vehicles, start/end parking sessions, view tickets, pay fines
- **Officer**: Issue tickets, view tickets, manage fines
- **Admin**: Manage zones/slots, view all sessions/tickets, apply penalties, manage users

## Tech Stack
- Java
- SQLite (JDBC)

## Database
The system automatically creates the following tables on first run:
- Users, Vehicles, Zones, Slots, Sessions, Tickets, Notifications