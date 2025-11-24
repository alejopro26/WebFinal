package chatnexus.dto;

public class RoomMembershipDTO {
    private Long roomId;
    private String roomName;
    private String role;

    public RoomMembershipDTO() {}

    public RoomMembershipDTO(Long roomId, String roomName, String role) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.role = role;
    }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}