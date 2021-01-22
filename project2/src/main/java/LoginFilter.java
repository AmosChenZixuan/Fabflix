package main.java;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter{
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        allowedURIs.add("index.html");
        allowedURIs.add("index.js");
        allowedURIs.add("api/index");
        allowedURIs.add("home_style.css");
        allowedURIs.add("style.css");
        allowedURIs.add("https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css");
        //allowedURIs.add("image/logo.png");
        allowedURIs.add("image/logo.gif");
        allowedURIs.add("image/icon.png");
        //allowedURIs.add("image/icon.gif");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession();

        String uri = httpRequest.getRequestURI();
        //System.out.println("LoginFilter: " + uri);

        if (uri.matches(".*/(index.html)?$") &&
                session.getAttribute("user") != null)
            httpResponse.sendRedirect("main-page.html");

        if (isAllowedWithouLogin(uri)) {
            chain.doFilter(request, response);
            return;
        }
        if (session.getAttribute("user") == null) {
            String loc = (uri.matches(".*/api/.*")) ? "../index.html" : "index.html";
            httpResponse.sendRedirect(loc);
        }
        else if (uri.matches(".*/single-(movie|star).html$") &&
                httpRequest.getParameter("id") == null)
            httpResponse.sendRedirect("main-page.html");
        else
            chain.doFilter(request, response);
    }

    private boolean isAllowedWithouLogin(String uri) {
        return allowedURIs.stream().anyMatch(uri.toLowerCase()::endsWith);
    }

    @Override
    public void destroy() {
    }
}
