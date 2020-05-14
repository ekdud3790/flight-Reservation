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
    @Autowired
    ReservationRepository reservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCompleted_UpdateStatus(@Payload PayCompleted payCompleted){

        if(payCompleted.isMe()){
            System.out.println("##### payment Complete : " + payCompleted.toJson());
            reservationRepository.findByflightId(payCompleted.getFlightId())
                    .ifPresent(
                            reservation -> {
                                reservation.setUserMoney(reservation.getUserMoney()-100);
                                reservation.setStatus("paySucceed");
                                reservationRepository.save(reservation);
                            }
                    );
            ;
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayFailed_UpdateStatus(@Payload PayFailed payFailed){

        if(payFailed.isMe()){
            System.out.println("##### payment Fail : " + payFailed.toJson());
            reservationRepository.findByflightId(payFailed.getFlightId())
                    .ifPresent(
                            reservation -> {
                                reservation.setUserMoney(reservation.getUserMoney()+0);
                                reservation.setStatus("payFailed");
                                reservationRepository.save(reservation);
                            }
                    );
            ;

        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_UpdateStatus(@Payload PayCanceled payCanceled){

        if(payCanceled.isMe()){
            System.out.println("##### payment Cancel : " + payCanceled.toJson());
            reservationRepository.findByflightId(payCanceled.getFlightId())
                    .ifPresent(
                            reservation -> {
                                reservation.setUserMoney(reservation.getUserMoney()+100);
                                reservation.setStatus("payCanceled");
                                reservationRepository.save(reservation);
                            }
                    );
            ;
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverTicketIssued_UpdateStatus(@Payload TicketIssued ticketIssued){

        if(ticketIssued.isMe()){
            System.out.println("##### TicketIssue : " + ticketIssued.toJson());
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverTicketIssueCanceled_UpdateStatus(@Payload TicketIssueCanceled ticketIssueCanceled){

        if(ticketIssueCanceled.isMe()){
            System.out.println("##### TicketIssue Cancel : " + ticketIssueCanceled.toJson());
        }
    }

}
