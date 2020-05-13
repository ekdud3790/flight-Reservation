package flightReservation;

import flightReservation.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCompleted_UpdateStatus(@Payload PayCompleted payCompleted){

        if(payCompleted.isMe()){
            System.out.println("##### listener UpdateStatus : " + payCompleted.toJson());
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayFailed_UpdateStatus(@Payload PayFailed payFailed){

        if(payFailed.isMe()){
            System.out.println("##### listener UpdateStatus : " + payFailed.toJson());
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_UpdateStatus(@Payload PayCanceled payCanceled){

        if(payCanceled.isMe()){
            System.out.println("##### listener UpdateStatus : " + payCanceled.toJson());
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverTicketIssued_UpdateStatus(@Payload TicketIssued ticketIssued){

        if(ticketIssued.isMe()){
            System.out.println("##### listener UpdateStatus : " + ticketIssued.toJson());
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverTicketIssueCanceled_UpdateStatus(@Payload TicketIssueCanceled ticketIssueCanceled){

        if(ticketIssueCanceled.isMe()){
            System.out.println("##### listener UpdateStatus : " + ticketIssueCanceled.toJson());
        }
    }

}
