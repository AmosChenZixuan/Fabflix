## CS 122B Project 2 Session example

This example shows how session works, including saving data to session and retrieve data from session.

### To run this example: 
1. Clone this repository using `git clone https://github.com/UCI-Chenli-teaching/cs122b-spring20-project2-session-example.git`
2. Open IntelliJ -> Import Project -> Choose the project you just cloned (The root path must contain the pom.xml!) -> Choose Import project from external model -> choose Maven -> Click on Finish -> The IntelliJ will load automatically
3. For "Root Directory", right click "cs122b-spring20-project1-star-example" -> Mark Directory as -> sources root
4. In Tomcat Deployment Configuration, make sure the application context is: /cs122b-spring20-project2-session-example
5. To run the example, follow the instructions in [canvas](https://canvas.eee.uci.edu/courses/26486/pages/intellij-idea-tomcat-configuration)

### Brief Explanation
`SessionServlet.java` is a Java servlet that keeps tracking user information. It creates a new session for each new coming user, it also recognize the previous-visited user and count for visit times.
Try use different names in the url and see the difference.

`ItemsServlet.java` is a Java servlet that keeps tracking items. It maintains a session for all the previous items that logged on to it.
Try use different items names in the url and see the difference.
