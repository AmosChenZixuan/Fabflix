package edu.uci.ics.fabflixmobile;

public class WebpageURL {
//    static public String login_url ="https://10.0.2.2:8443/project2_war/api/index";
//    static public String single_movie_url ="https://10.0.2.2:8443/project2_war/api/single-movie?id=";
//    static public String main_page_url ="https://10.0.2.2:8443/project2_war/api/main-page";
    //static public String login_url ="https://10.0.2.2:8443/project4_war/api/index";
    //static public String single_movie_url ="https://10.0.2.2:8443/project4_war/api/single-movie?id=";
    //static public String main_page_url ="https://10.0.2.2:8443/project4_war/api/main-page";

    static private String ip = "ec2-54-153-72-5.us-west-1.compute.amazonaws.com";
    static public String login_url ="https://" + ip + ":8443/Fabflix0.4/api/index";
    static public String single_movie_url ="https://" + ip + ":8443/Fabflix0.4/api/single-movie?id=";
    static public String main_page_url ="https://" + ip + ":8443/Fabflix0.4/api/main-page";

}
