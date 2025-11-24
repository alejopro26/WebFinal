package chatnexus.dto;

import jakarta.validation.constraints.NotBlank;

public class RoomAccessDTO {
    @NotBlank
    private String password;

    public RoomAccessDTO() {}

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
