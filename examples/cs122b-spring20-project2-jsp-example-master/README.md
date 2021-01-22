## CS 122B Project 2 JSP example

This example shows how JSP works independently.

### Before running the example
#### If you do not have USER `mytestuser` setup in MySQL, follow the below steps to create it:

 - login to mysql as a root user 
    ```
    local> mysql -u root -p
    ```

 - create a test user and grant privileges:
    ```
    mysql> CREATE USER 'mytestuser'@'localhost' IDENTIFIED BY 'mypassword';
    mysql> GRANT ALL PRIVILEGES ON * . * TO 'mytestuser'@'localhost';
    mysql> quit;
    ```

#### prepare the database `moviedbexample`
 
```    
    local> mysql -u mytestuser -p
    mysql> CREATE DATABASE IF NOT EXISTS moviedbexample;
    mysql> USE moviedbexample;
    mysql> CREATE TABLE IF NOT EXISTS stars(
                   id varchar(10) primary key,
                   name varchar(100) not null,
                   birthYear integer
               );
    
    mysql> INSERT IGNORE INTO stars VALUES('755011', 'Arnold Schwarzeneggar', 1947);
    mysql> INSERT IGNORE INTO stars VALUES('755017', 'Eddie Murphy', 1961);
```    
### To run this example: 
1. clone this repository using `git clone https://github.com/UCI-Chenli-teaching/cs122b-spring20-project2-jsp-example.git`
2. open Intellij -> Import Project -> Import Project `(choose the project you just clone)`-> Choose `Maven` -> -> Click on `Finish`.
3. Open `src/main/webapp/index.jsp`. Make sure you have the `moviedbexample` database.
4. In Tomcat Deployment Configuration, make sure the application context is: /cs122b-spring20-project2-jsp-example
5. To run the example, follow the instructions in [canvas](https://canvas.eee.uci.edu/courses/26486/pages/intellij-idea-tomcat-configuration)

### Brief Explanation
`index.jsp` dynamically generates the html page with Java API. It is requested as a static resource, rather than a servlet sample we have seen before.
The .jsp file is requested directly.
