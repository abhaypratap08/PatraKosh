# **PatraKosh - Desktop File Storage Application**

**PatraKosh** is a modern, desktop file storage solution inspired by Dropbox, built with **JavaFX** and **MySQL**.  
Enjoy a beautiful, intuitive interface for uploading, downloading, searching, and securely managing your files.

---

## **✨ Features**

### **User Authentication**
- **Secure Login System:** Username/email and password authentication  
- **User Registration:** Create new accounts with validation  
- **Password Security:** SHA-256 password hashing  
- **Session Management:** Robust session handling across the application  

### **File Management**
- **Upload Files:** Drag-and-drop file uploads  
- **Download Files:** Download any file to your system  
- **Delete Files:** Remove files with confirmation  
- **Search Functionality:** Instantly search files by name  
- **File Metadata:** Track size, upload date, and more  

---

## **📋 Prerequisites**

- **Java 17** or higher  
- **Maven 3.6+**  
- **MySQL 8.0+**  
- **JavaFX 21** (managed via Maven dependencies)  

---

## **🛠 Installation & Setup**

### **1. Clone the Repository**

git clone https://github.com/abhaypratap08/PatraKosh.git

cd PatraKosh


### **2. Set Up the MySQL Database**

- Start your MySQL server.
- Using a client or MySQL Workbench, execute the `database_setup.sql` script:

mysql -u <your_mysql_user> -p < database_setup.sql

text

### **3. Configure Database Connection**

Edit `src/main/resources/application.properties`:

db.url=jdbc:mysql://localhost:3306/patrakosh_db
db.username=your_mysql_user
db.password=your_mysql_password
db.driver=com.mysql.cj.jdbc.Driver

text

### **4. Build the Project**

mvn clean install

text

### **5. Run the Application**

**A) With Maven**
mvn javafx:run

text

**B) With Java**
java -jar target/patrakosh-1.0.0.jar

text

**C) From IDE**  
Run the `Launcher.java` class as a Java application.

---

## **📁 Project Structure**

PatraKosh/
├── src/main/
│ ├── java/com/patrakosh/
│ │ ├── controller/
│ │ ├── dao/
│ │ ├── model/
│ │ ├── service/
│ │ ├── util/
│ │ ├── Launcher.java
│ │ └── MainApp.java
│ └── resources/
│ ├── fxml/
│ └── application.properties
├── storage/
├── database_setup.sql
├── pom.xml
└── README.md

text

---

## **🔧 Technologies Used**

- **Java 17**
- **JavaFX 21**
- **MySQL 8+**
- **Maven**
- **Apache Commons IO**
- **SLF4J**

---

## **📝 Usage Guide**

- **Create Account:** Use the signup form on first launch
- **Login:** Enter your registered credentials
- **Upload File:** Click Upload, select file, upload completes instantly
- **Download File:** Find your file, click download, and save locally
- **Delete File:** Remove file with a single click and confirmation
- **Search:** Start typing to auto-filter files by name

---

## **🔐 Security**

- **SHA-256 Password Hashing**
- **SQL Injection Protection:** Prepared statements for all queries  
- **Input Validation:** On client- and server-side  
- **Session Management:** Secure handling

---

## **🐛 Troubleshooting**

- **Database Issues:** Check MYSQL is running & credentials are correct
- **JavaFX Errors:** Confirm Java 17+ and dependencies are installed
- **File System Errors:** Ensure the `storage/` directory is writable and exists

---

## **📦 Packaging as JAR**

mvn clean package

text
Output: `target/patrakosh-1.0.0.jar`

---

## **🤝 Contributing**

Contributions are welcome!  
- Fork, branch, commit, and submit a pull request.
- Open an issue for major changes or suggestions.

---

## **📄 License**

This project is for educational use.

---

## **👥 Team**

Built by **TeamAlgoNauts**.

---

## **📞 Support**

For help or questions, [open an issue](https://github.com/abhaypratap08/PatraKosh/issues).

---

> **Note:**  
> Don’t forget to update your MySQL credentials in `application.properties` before running PatraKosh!
