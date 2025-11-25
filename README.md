# Notification Service

The Notification Service is a Spring Boot microservice responsible for sending user notifications
and managing user notification preferences. It integrates with other services
(such as Task Service or Project Service) to notify users about important events.

---

## Features

- Send email notifications
- Store and update user notification preferences
- Support for multiple notification types (email, summary, reminders, deadlines)
- Exposes REST API endpoints for other services
- Works with Spring Application Events for internal event-based triggers

---

## Technologies

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Mail
- MySQL
- Lombok

---

## API Endpoints

### **POST /notifications/send**
Sends an email notification.

### **PUT /notifications/preferences/{userId}**
Creates or updates a user's notification preferences.

### **GET /notifications/preferences/{userId}**
Returns the current user notification preferences.

---

## Email Configuration

Add the following to `application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true