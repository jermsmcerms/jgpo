package lib.network.udppeerevents;

public class UdpPeerEvent {
    public enum EventType {
       Unknown,
       Connected,
       Synchronizing,
       Synchronzied,
       Input,
       Disconnected,
       NetworkInterrupted,
       NetworkResumed,
    };

    public EventType eventType;


    public UdpPeerEvent(EventType t) { this.eventType = t; }
 };