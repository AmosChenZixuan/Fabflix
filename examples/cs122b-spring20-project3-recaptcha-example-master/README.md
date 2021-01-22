## CS 122B Project 3 reCAPTCHA example

This example shows how to add reCAPTCHA to your frontend and backend.

### To run this example: 
1. clone this repository using `git clone https://github.com/UCI-Chenli-teaching/cs122b-spring20-project3-recaptcha-example.git`
2. Open IntelliJ -> Import Project -> Choose the project you just cloned (The root path must contain the pom.xml!) -> Choose Import project from external model -> choose Maven -> Click on Finish -> The IntelliJ will load automatically.
3. For "Root Directory", right click "cs122b-spring20-project3-recaptcha-example" -> Mark Directory as -> sources root
4. In Tomcat Deployment Configuration, make sure the application context is: /cs122b-spring20-project3-recaptcha-example
5. If you haven't done so, get a [reCAPTCHA](https://www.google.com/recaptcha/intro/v3.html) from Google. 
   `Admin Console` -> `Register a new site` -> Choose `reCAPTCHA v2` -> Enter both your AWS public IP and "localhost" in "Domains" section -> Click "Submit"
6. In `src/RecaptchaConstants.java`, replace `YOUR_SECRET_KEY;` with your own reCAPTCHA `secret key`.
7. In `WebContent/index.html`, replace `data-sitekey="YOUR_SITE_KEY"` with your own reCAPTCHA `site key`.
8. In `src/FormRecaptcha.java`, change the mysql username and password.
9. You can run this project on Tomcat (both local machine and AWS).
