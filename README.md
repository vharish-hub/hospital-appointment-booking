# MediCare: Hospital Appointment Booking System

MediCare is a production-ready, enterprise-grade clinical scheduling and hospital management application built using a Spring Boot backend and an interactive Thymeleaf + Bootstrap 5 frontend. 

It features Role-Based Access Control (RBAC) for Patients, Doctors, and Administrators, featuring responsive dashboards, glassmorphic layout assets, live WebSocket-based appointment queue trackers, asynchronous email notifications, and automated audit reporting (PDF & Microsoft Excel sheets).

---

## 🛠️ Technology Stack

- **Backend Core**: Java 21, Spring Boot 3.3.x, Spring Data JPA, Hibernate, Maven
- **Security**: Spring Security 6.x, JSON Web Token (JWT) stateless verification, BCrypt password hashing
- **Frontend Views**: Thymeleaf templates, Bootstrap 5, Vanilla CSS3 (Custom Glassmorphism), AJAX (fetch API)
- **Live Updates**: Spring WebSockets (STOMP + SockJS) for active clinical queue tokens
- **Audits & Exports**: OpenPDF (for A6 receipts & A4 logs), Apache POI (for XLSX databases)
- **Email**: Spring Boot Starter Mail (JavaMailSender)
- **Databases**: H2 (In-memory development) and MySQL 8 (Production profile)

---

## 📂 Project Structure

```text
medicare/
├── pom.xml                                # Maven dependencies
├── run.bat                                # CLI project launcher script
├── mvnw / mvnw.cmd                        # Maven wrapper scripts
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── medicare/
        │           ├── MedicareApplication.java     # Boot entrypoint
        │           ├── controller/                  # Web Views & REST APIs
        │           ├── dto/                         # Data transfer layouts
        │           ├── model/                       # JPA Database Entities
        │           ├── repository/                  # Database Query interfaces
        │           ├── security/                    # Spring Security & JWT filters
        │           └── websocket/                   # STOMP socket configurations
        └── resources/
            ├── application.properties     # System settings (H2/MySQL, JWT)
            ├── schema.sql                 # Database Schema definitions
            ├── data.sql                   # Database Initial seed entries
            ├── static/
            │   └── css/
            │       └── style.css          # Design tokens & glassmorphic styling
            └── templates/
                ├── fragments/
                │   └── layout.html        # Header, navbar, footer modules
                ├── patient/
                │   └── dashboard.html     # Patient booking ledger & forms
                ├── doctor/
                │   └── dashboard.html     # Doctor workflow & live queue
                ├── admin/
                │   ├── dashboard.html     # Analytics Chart.js summary
                │   ├── doctors.html       # Doctor CRUD views
                │   ├── patients.html      # Patient registry
                │   ├── appointments.html  # Search & filters
                │   └── reports.html       # Export tools
                ├── index.html             # Healthcare visitor home page
                ├── login.html             # AJAX JWT login card
                ├── register.html          # Patient sign-up form
                ├── doctors.html           # Doctor catalog directory
                ├── doctor-profile.html    # Specialized stats & reviews
                ├── about.html             # Clinic overview
                └── contact.html           # 24/7 help forms
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Version 21 or later installed.
- **Maven**: (Optional, the wrapper redirects automatically)
- **MySQL**: (Optional, H2 runs out of the box. See the MySQL profile section)

### Running the Application

1. Double-click the `run.bat` file in the root folder, or execute the following maven command in your terminal:
   ```cmd
   mvn spring-boot:run
   ```
2. Wait for the server to load (indicated by `Started MedicareApplication in X seconds` log).
3. Open your browser and navigate to: **`http://localhost:8080`**
4. To check database tables directly, open the H2 Database Console:
   - **Console URL**: `http://localhost:8080/h2-console`
   - **JDBC URL**: `jdbc:h2:mem:medicaredb`
   - **User Name**: `sa`
   - **Password**: *(leave blank)*

---

## 🔑 Sample Seed Accounts

All pre-configured seed accounts have the default password: **`password`**

| Role | Email Address | Description |
|:---|:---|:---|
| **Admin** | `admin@medicare.com` | Access to metrics, doctor/patient registries, report downloads. |
| **Doctor** | `sarah.connor@medicare.com` | Cardiology specialist. Access to live queue and workflow shifts. |
| **Doctor** | `charles.xavier@medicare.com` | Neurology specialist. Access to schedule hours. |
| **Patient** | `john.doe@gmail.com` | Sample patient. Has appointment logs and slip download history. |
| **Patient** | `jane.smith@gmail.com` | Sample patient. |

---

## 📊 Database Schema Details

The schema creates 5 relational tables:
1. **`users`**: Manages all registered members (Admin, Doctor, Patient roles).
2. **`departments`**: Clinics information (Cardiology, Neurology, etc.).
3. **`doctors`**: Detailed clinical specifications (Specialization, fee, schedule, linked via email to users).
4. **`appointments`**: Logs bookings status, slot time, and token number queue.
5. **`reviews`**: Ratings and comment ledger for doctor profiles.

---

## 🛠️ Configuring Production MySQL Database

To switch the database engine from H2 to MySQL:
1. Open [application.properties](file:///C:/Users/Admin/.gemini/antigravity/scratch/medicare/src/main/resources/application.properties)
2. Comment out the **H2 Datasource** block (lines 6-11).
3. Comment out the **H2 Dialect** (line 16).
4. Uncomment the **MySQL Configuration Profile** block (lines 47-52) and modify the MySQL URL, username, and password parameters according to your local instance.
5. Restart the server. Spring Boot will automatically run `schema.sql` and `data.sql` to initialize your MySQL database.
