# cs122b-spring20-team-66
cs122b-spring20-team-66 created by GitHub Classroom

# Demo URL: 
Project5: https://youtu.be/6nbJo1KTOFI \
Project4: https://youtu.be/04txv27V78Y \
Project3: https://youtu.be/wNM4KoZDJPo \
Project2: https://youtu.be/FaIeqSc7H4k \
Project1: https://youtu.be/v400noKGxRA

# Project 5 README:
[README.md](report/README.md)

---------------------------------------------------------------------------------------------------------------------------------------
# Past Record

# Project4 Deployment:
## Build APP
cd ./project4 -> mvn clean package


# Fuzzy Search Implementation
Use the edth function from the FLAMINGO project. For the distance threshold, we allowed 1 error input per 4 characters, and lettercases are treated the same. e.g. 'trve loue' and 'True Love' are considered similar (2 diff in 9 chars), but 'trve laue' and 'True Love' are not (3 diff in 9 chars).


---------------------------------------------------------------------------------------------------------------------------------------
                                                          Work Distribution for project4:
Haodong Qi is responsible for developing the main functionalities of the Android app, designing the query for full-text search, and optimizing the UI for the full-text search bar. 

Zixuan Chen is responsible for integrating full-text search with other functionalities, implementing autocomplete and fuzzy search, and optimizing the Android UI.
                                                          
                                                          Work Distribution for project3:
Haodong Qi is responsible for adding Https, layout and visual design for dashboard, and the implementation of xml parser.

Zixuan Chen is responsible for adding reCaptcha, encryption of passwords, metadata display and stored procedures in dashboard, and the optimization of xml parser.

# Project3 Deployment:
## Build APP 
1. Run stored-procedure.sql
2. cd ./project3 -> mvn clean package
## Run Parser
cd ./xmlParser -> mvn exec:java -Dexec.mainClass="main.java.OptimizedParser" 2> parserReport.txt
## Note
We altered the table sales from project 1 by adding a column (alter table sales add column quantity INT NOT NULL DEFAULT 1).

# PreparedStatments
[Queries in WebApp](project3/src/main/java/SqlQuery.java)\
[Queries in Parser](xmlParser/src/main/java/ParserQuery.java)

# Parsing Time Optimization
In NaiveParser, we used preparedstaments and store procedure to do duplication check and data insertion. This approach took 18 minutes to finish the task on our local machine.
## Strategy 1
In BetterParser, we used batch instead of separate procedure calls. With batch, we saved a lot of time committing changes, creating, and closing statements. However, the search queries in stored procedures are still very expensive. This approach took 14 minutes on our local machine.
## Strategy 2
In OptimizedParser, we implement helper data structures to cache the movie, star, and genre tables. This saves the step of searching for id using a SQL query since we can do a constant time lookup in our cache. Now, the heaviest task is inserting data into tables, which is not very expensive compared to search. Although it consumes a lot more memory than previous approaches, its significant speed advantage is preferable. This approach only takes 30 seconds on our local machine.

# Inconsistent Data Report
[Report](xmlParser/parserReport.txt)
                                                          
                                                          Work Distribution for project2:

Haodong Qi did mostly of the layout design using bootstraps and implemented searching,sorting,browsing and jumping(next/pre page).

Zixuan Chen completed the login page, web filters, shopping cart, payment and confirmation page. He optimized the appearance of web pages and also did most of the website testing.
# substring matching design: 
We uses contain predicate (e.g. %AN%) for searching movie title, director name. 
For browsing by characters/number we uses startwith predicate(e.g. ABC%), and for browsing by non-alphanumerical characters we uses regular expression ^[^A-Za-z0-9] to search for the expected result.


                                                          Work Distribution for project1：

Haodong Qi created the script for building the movie database and uses simple css to alter the appearance of the webpage.

Zixuan Chen implemented the backend APIs and appropriately connect webpages using JavaScripts.


