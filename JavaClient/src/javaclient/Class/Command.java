package javaclient.Class;

public class Command {

    private String name;
    private String cmdByte;
    private String payload;
    private String reserve;
    private String recipient;

    public Command() {
    }

    public Command(String name, String cmdByte, String payload, String reserve, String recipient) {
        this.name = name;
        this.cmdByte = cmdByte;
        this.payload = payload;
        this.reserve = reserve;
        this.recipient = recipient;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCmdByte() {
        return cmdByte;
    }

    public void setCmdByte(String cmdByte) {
        this.cmdByte = cmdByte;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getReserve() {
        return reserve;
    }

    public void setReserve(String reserve) {
        this.reserve = reserve;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

}
