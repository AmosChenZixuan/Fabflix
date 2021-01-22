package main.java;

public class Star {
    String name;
    int brith;

    public Star(String n, int b){
        name = n;
        brith = b;
    }
    public String toString(){
        return "<-- Star name:"+name +" birth:" +brith+"--->";
    }
    public boolean is_valid(){
        return !name.equals("undefine");
    }
}
