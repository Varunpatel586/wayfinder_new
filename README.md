# WayFinder

A Spring Boot web application for finding and navigating to various locations.

## 🚀 Technologies Used

- **Backend**: 
  - Java 25
  - Spring Boot 3.5.6
  - Spring Data JPA
  - Hibernate
  - SQLite Database

- **Frontend**:
  - Thymeleaf (Server-side templating)
  - HTML5
  - CSS3
  - JavaScript

- **Development Tools**:
  - Maven (Dependency Management)
  - Lombok (Reducing Boilerplate Code)
  - Spring Boot DevTools

## 📋 Prerequisites

- Java 25 JDK or later
- Maven 3.6.0 or later
- Git (for version control)

## 🛠️ Setup & Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Varunpatel586/wayfinder_new.git
   cd wayfinder_new
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/varun/wayfinder/
│   │       ├── config/       # Configuration classes
│   │       ├── controller/   # Spring MVC Controllers
│   │       ├── model/        # Entity classes
│   │       ├── repository/   # Data access layer
│   │       └── service/      # Business logic
│   └── resources/
│       ├── static/           # Static resources (CSS, JS, images)
│       ├── templates/        # Thymeleaf templates
│       └── application.properties  # Application configuration
└── test/                     # Test files
```

## 🚦 Running Tests

```bash
mvn test
```

## 🔧 Configuration

Application properties can be configured in `src/main/resources/application.properties`.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## ✉️ Contact

LinkedIn - [Varun Patel](https://www.linkedin.com/in/varun-patel-16611631a/)

Project Link: [https://github.com/Varunpatel586/wayfinder_new](https://github.com/Varunpatel586/wayfinder_new)
