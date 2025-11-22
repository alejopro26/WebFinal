package chatnexus.dto;

public class RoomDTO {

    private String name;
    private boolean isPrivate;
    private String password;

    public RoomDTO() {}

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public boolean isPrivate() {
        return isPrivate;
    }
    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
