package main.java;

public class Admin implements User{
    // employees
    public String email, password, fullname;

    Admin(String email, String password, String fullname){
        this.email = email;
        this.password = password;
        this.fullname = fullname;
    }

    @Override
    public String getType() {
        return "Admin";
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return fullname;
    }
}
