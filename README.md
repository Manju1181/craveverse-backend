# 🍽️ CraveVerse Backend — Food Delivery REST API

**Java | Spring Boot | MySQL | REST APIs**

CraveVerse Backend is the server-side component of the CraveVerse food discovery and delivery application. It provides REST APIs for food listings, customer orders, and administrative order management, with MySQL used for data persistence.

## ✨ Features

- **Food Management:** Retrieve available food items.
- **Order Management:** Create and retrieve customer orders.
- **Customer Order History:** Retrieve orders associated with the logged-in customer.
- **Admin Dashboard:** Retrieve all customer orders for administrative management.
- **Order Status Updates:** Update the status of an order.
- **Session-Based Access Control:** Restrict selected administrative operations to authenticated administrators.
- **Database Integration:** Store and retrieve application data using MySQL.
- **REST API Architecture:** Communicate with the React frontend through HTTP endpoints.

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java | Backend programming |
| Spring Boot | Application framework |
| Spring Web | REST API development |
| Spring Data JPA | Database access |
| Hibernate | ORM |
| MySQL | Relational database |
| Maven | Build and dependency management |
| Git and GitHub | Version control |

## 🏗️ Architecture

```text
React Frontend
      |
      | HTTP Requests
      v
Spring Boot REST API
      |
      | Spring Data JPA / Hibernate
      v
MySQL Database
```

## 📌 API Endpoints

The following endpoints describe the application's main API routes. Some endpoints require an authenticated customer or administrator session.

### Food APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/foods` | Retrieve available food items |
| GET | `/api/foods/{id}` | Retrieve an available food item by ID |
| POST | `/api/foods` | Add a food item (admin access required) |
| DELETE | `/api/foods/{id}` | Delete a food item (admin access required) |

### Order APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/orders` | Retrieve the logged-in customer's orders |
| GET | `/api/orders/admin` | Retrieve all orders (admin access required) |
| PUT | `/api/orders/{orderId}/status` | Update an order's status, subject to authorization |

**Note:** Verify the HTTP method and request-body requirements for the order creation and status-update endpoints against the current controller implementation.

## 🚀 Getting Started

### Prerequisites

Install the following:

- Java Development Kit (JDK) compatible with the project
- MySQL Server
- Git

### 1. Clone the repository

```bash
git clone https://github.com/Manju1181/craveverse-backend.git
cd craveverse-backend
```

### 2. Create the database

Open your MySQL client and execute:

```sql
CREATE DATABASE craveverse;
```

If the database already exists, you do not need to create it again.

### 3. Configure database credentials

Open:

```text
src/main/resources/application.properties
```

The application expects the database password through the `DB_PASSWORD` environment variable.

On macOS or Linux, set it in your terminal:

```bash
export DB_PASSWORD='your_mysql_password'
```

Replace the example value with your local MySQL password. Do not commit real passwords or secret files to GitHub.

### 4. Start the backend

Run:

```bash
./mvnw spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

For the food listing endpoint, open:

```text
http://localhost:8080/api/foods
```

The response depends on the data available in your database.

## ⚙️ Configuration

The application's main configuration is located in:

```text
src/main/resources/application.properties
```

Important settings include:

- Database URL: `jdbc:mysql://localhost:3306/craveverse`
- Database username: `root`
- Database password: supplied using `DB_PASSWORD`
- Server port: `8080`
- JPA schema management: configured through `spring.jpa.hibernate.ddl-auto`

Adjust these settings for your local environment as needed.

## 🔐 Security Considerations

- Keep database passwords and secrets out of version control.
- Protect administrative endpoints with server-side authorization.
- Validate customer access when retrieving or modifying orders.
- Configure appropriate CORS and session settings for production.
- Use HTTPS and secure session-cookie settings when deploying publicly.

## 🔮 Future Improvements

- Add automated unit and integration tests.
- Improve request validation and exception handling.
- Strengthen authentication and authorization.
- Deploy the backend and configure production database access.
- Add API documentation using OpenAPI/Swagger.

## 👨‍💻 Author

**Manjunath Uppar**

- GitHub: [@Manju1181](https://github.com/Manju1181)
- Frontend Repository: [CraveVerse Frontend](https://github.com/Manju1181/craveverse-frontend)

---

*Built with Java, Spring Boot, and MySQL.*
