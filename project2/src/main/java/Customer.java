package main.java;

public class Customer {
    public String id, fname, lname, ccid, address, email, password;
    Customer(String id, String fname, String lname, String ccid, String address, String email, String password){
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.ccid = ccid;
        this.address = address;
        this.email = email;
        this.password = password;
    }

    Customer(){
        this(null, null, null, null, null, null, null);
    }

    public String getName(){
        return fname + " " + lname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCcid(String ccid) {
        this.ccid = ccid;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

