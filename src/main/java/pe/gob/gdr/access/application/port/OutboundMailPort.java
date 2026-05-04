package pe.gob.gdr.access.application.port;

public interface OutboundMailPort {

    void send(OutboundMailMessage message);
}
