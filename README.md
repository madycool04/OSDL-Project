# 🏨 Lime Tree Resort Management System

A full-featured JavaFX desktop application for hotel management, built with Maven.

---

## Features

| Feature | Details |
|---|---|
| **Dashboard** | Live stats, occupancy rate, recent bookings |
| **Room Management** | Add / Edit / Delete rooms, search & filter |
| **Booking System** | Full guest registration + room booking form |
| **Check-Out** | Process checkout, view/save formatted bill |
| **Guest Registry** | All guests with booking history |
| **Billing** | Revenue overview, filter by status, save bills |
| **Data Persistence** | File-based (Java Serialization) in `~/.hotelms/` |

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 17+ |
| UI Framework | JavaFX 21 |
| Build Tool | **Maven** |
| Storage | File Persistence (Java Serialization) |
| Styling | Custom CSS (dark theme) |
| Architecture | MVC + Service Layer |

---

## Project Structure

```
HotelManagementSystem/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   ├── module-info.java
        │   └── com/hotel/
        │       ├── MainApp.java                   ← Entry point
        │       ├── model/
        │       │   ├── Room.java
        │       │   ├── Guest.java
        │       │   └── Booking.java
        │       ├── service/
        │       │   ├── RoomService.java
        │       │   ├── GuestService.java
        │       │   ├── BookingService.java
        │       │   └── BillService.java
        │       ├── util/
        │       │   ├── FileStorageUtil.java
        │       │   ├── IdGeneratorUtil.java
        │       │   └── AlertUtil.java
        │       └── view/
        │           ├── MainView.java              ← App shell + sidebar nav
        │           ├── DashboardView.java
        │           ├── RoomsView.java
        │           ├── RoomFormDialog.java
        │           ├── BookingView.java
        │           ├── CheckoutView.java
        │           ├── GuestsView.java
        │           └── BillingView.java
        └── resources/
            └── com/hotel/view/
                └── styles.css                     ← Dark theme CSS
```

## Data Storage

All data is stored permanently in:

```
~/.hotelms/
├── rooms.dat         ← Room data
├── guests.dat        ← Guest registry
├── bookings.dat      ← All bookings
├── Bill_BK*.txt      ← Generated bill files
└── *_counter.dat     ← ID counters
```

Data persists across all application restarts automatically.

---

## Billing

Each bill includes:
- Hotel header (name, address, GST number)
- Guest details (name, ID, phone)
- Booking details (room, dates, nights)
- Itemized charges:
  - Room charges (nights × rate)
  - Service charges (10%)
  - GST (18%)
  - **Total Amount**
- Payment mode and status

Bills are saved as `.txt` files in `~/.hotelms/`.

---

## Room Types & Pricing (defaults)

| Type | Default Price/Night |
|---|---|
| Single | ₹ 1,500 |
| Double | ₹ 2,500 – 2,800 |
| Deluxe | ₹ 4,000 – 4,500 |
| Suite | ₹ 7,000 – 7,500 |

Prices can be customized per room.