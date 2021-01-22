## CS 122B Project 5 Tomcat Pooling example

This example shows how to use Connection Pooling with JDBC.

### To run this example: 
1. Clone this repository using `git clone https://github.com/UCI-Chenli-teaching/cs122b-spring20-project5-TomcatPooling-example.git`
2. Open IntelliJ -> Import Project -> Choose the project you just cloned (The root path must contain the pom.xml!) -> Choose Import project from external model -> choose Maven -> Click on Finish -> The IntelliJ will load automatically
3. For "Root Directory", right click "cs122b-spring20-project5-TomcatPooling-example" -> Mark Directory as -> sources root
4. In `WebContent/META-INF/context.xml`, make sure the mysql username is `mytestuser` and password is `mypassword`.
5. Also make sure you have the `moviedb` database.
6. You can run this project on Tomcat now.
7. Navigate to http://localhost:8080/cs122b-spring20-project5-TomcatPooling-example/tomcat-pooling to see a list of stars.

### Brief Explanation
- `TomcatPoolingServlet.java` is a Java servlet that showcases connection pooling. The username and password of the MySQL is given to JDBC through a context.xml file. JDBC uses the credentials to create a connection pool. The servlet leases connections from this pool when needed and returns when the task is done.

- `META-INF/context.xml` includes important information about the database, user name, password, and pooling configuration. You would have to redeploy after changing this file. Otherwise, the Tomcat server may still use the previous context.xml. Check [this](http://tomcat.apache.org/tomcat-8.5-doc/jndi-datasource-examples-howto.html) for more information about how to configure Connection Pooling.

- Look at the following files for the main changes in this war file compared to the previous war file without Connection Pooling:
  - `/META-INF/context.xml`.
  - `/src/TomcatPoolingServlet.java`
  - `/WEB-INF/web.xml` (see the `resource-ref` tag)

