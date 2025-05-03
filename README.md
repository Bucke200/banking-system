# Online Banking System (Spring Boot)

This project is a simple simulation of an online banking system featuring a RESTful API backend built with Spring Boot and a basic web frontend for user interaction.

## Features

*   **Backend API (Spring Boot):**
    *   User registration and secure JWT-based authentication.
    *   Bank account creation and retrieval for authenticated users.
    *   Secure fund transfers between accounts (transactional).
    *   Viewing account details and transaction history.
    *   Persistence using Spring Data JPA and PostgreSQL.
*   **Frontend UI (HTML/CSS/JavaScript):**
    *   Simple web interface served by the Spring Boot application.
    *   Forms for user registration and login.
    *   Sections to view accounts, create new accounts, transfer funds, and view transaction history.

## Technologies Used

*   Java 17+
*   Spring Boot 3.x
    *   Spring Web
    *   Spring Data JPA
    *   Spring Security
*   PostgreSQL (Database)
*   Maven (Build Tool)
*   Lombok (Code generation utility)
*   jjwt (Java JWT library)
*   HTML, CSS, JavaScript (Frontend)

## Prerequisites

*   **Java Development Kit (JDK):** Version 17 or higher installed and configured (JAVA_HOME environment variable set).
*   **PostgreSQL:** Installed and running.
*   **Apache Maven (Optional but Recommended):** If not using an IDE's built-in Maven support or the (currently missing) Maven wrapper (`mvnw`), install Maven and ensure its `bin` directory is in your system's PATH.

## Setup Instructions

1.  **Clone/Download:** Get the project code onto your local machine.
2.  **Database Setup:**
    *   Ensure your PostgreSQL server is running.
    *   Connect to PostgreSQL (e.g., using `psql` or pgAdmin) as a superuser (like `postgres`).
    *   Create the database:
        ```sql
        CREATE DATABASE online_banking_db;
        ```
    *   Create a dedicated user (replace `your_username` and `your_password`):
        ```sql
        CREATE USER your_username WITH PASSWORD 'your_password';
        ```
    *   Grant privileges to the user on the database:
        ```sql
        GRANT ALL PRIVILEGES ON DATABASE online_banking_db TO your_username;
        ```
    *   Grant schema creation privileges (needed for Hibernate's `ddl-auto=update`):
        ```sql
        GRANT CREATE ON SCHEMA public TO your_username;
        ```
3.  **Configure Application:**
    *   Open the `src/main/resources/application.properties` file.
    *   Update the following properties with your database details:
        *   `spring.datasource.username=your_username`
        *   `spring.datasource.password=your_password`
    *   **Security:** Replace the placeholder `jwt.secret` value with a strong, unique secret key.

## How to Run

1.  **Using Maven (if installed and in PATH):**
    *   Open a terminal or command prompt in the project's root directory (`c:/projects/online-banking-system`).
    *   Run the command: `mvn spring-boot:run`
2.  **Using an IDE (e.g., VS Code with Java Extension Pack):**
    *   Open the project folder in your IDE.
    *   Locate the `OnlineBankingSystemApplication.java` file (`src/main/java/com/example/onlinebankingsystem/OnlineBankingSystemApplication.java`).
    *   Run the `main` method directly from the IDE (often via a "Run" button/link near the method or through the Run/Debug panel).

The application will start, and the backend API will be available. Hibernate will automatically create the necessary database tables on the first run due to `spring.jpa.hibernate.ddl-auto=update`.

## Accessing the Frontend

Once the application is running, open your web browser and navigate to:

`http://localhost:8080`

You should see the web interface to register, log in, and use the banking features.

## API Endpoints Overview

*   **Authentication:**
    *   `POST /api/auth/register`: Register a new user.
    *   `POST /api/auth/login`: Log in and receive a JWT token.
*   **Accounts (Requires Authentication - Bearer Token):**
    *   `POST /api/accounts`: Create a new bank account for the logged-in user.
    *   `GET /api/accounts`: Get all accounts for the logged-in user.
    *   `GET /api/accounts/{accountNumber}`: Get details for a specific account owned by the logged-in user.
*   **Transactions (Requires Authentication - Bearer Token):**
    *   `POST /api/transactions/transfer`: Transfer funds from one account to another.
    *   `GET /api/transactions/{accountNumber}`: Get transaction history for an account owned by the logged-in user.
