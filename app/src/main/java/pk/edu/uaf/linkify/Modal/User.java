package pk.edu.uaf.linkify.Modal;

public class User {

    private int userImg;
    private String name;
    private String text;

    public User(int userImg, String name, String text) {
        this.userImg = userImg;
        this.name = name;
        this.text = text;
    }

    public int getUserImg() {
        return userImg;
    }

    public void setUserImg(int userImg) {
        this.userImg = userImg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}