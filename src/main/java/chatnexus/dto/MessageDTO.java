package chatnexus.dto;

public class MessageDTO {

    private Long id;
    private Long roomId;
    private String content;
    private String sender;

    public MessageDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRoomId() {
        return roomId;
    }
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
}
