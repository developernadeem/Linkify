package pk.edu.uaf.linkify.Modal;

public class UserCalls {
    private int userImg;
    private String name;
    private String text;
    private  int iconeImg;

    public UserCalls(int userImg, String name, String text, int iconeImg) {
        this.userImg = userImg;
        this.name = name;
        this.text = text;
        this.iconeImg = iconeImg;
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

    public int getIconeImg() {
        return iconeImg;
    }

    public void setIconeImg(int iconeImg) {
        this.iconeImg = iconeImg;
    }
}
