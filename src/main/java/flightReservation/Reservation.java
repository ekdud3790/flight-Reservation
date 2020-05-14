package flightReservation;

import javax.persistence.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flightReservation.config.kafka.KafkaProcessor;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String userId;
    private String status;
    private Long userMoney;
    private String flightId;

    @PostPersist
    public void onPostPersist(){
        Requested requested = new Requested();
        requested.setUserId(this.getUserId());
        requested.setFlightId(this.getFlightId());
        requested.setUserMoney(this.getUserMoney());
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;

        try {
            json = objectMapper.writeValueAsString(requested);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }

        KafkaProcessor processor = Application.applicationContext.getBean(KafkaProcessor.class);
        MessageChannel outputChannel = processor.outboundTopic();

        outputChannel.send(MessageBuilder
                .withPayload(json)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());

    }

    @PostUpdate
    public void onPostUpdate(){
        if("payCanceled".contains(this.getStatus())){
            PayCanceled canceled = new PayCanceled();
            canceled.setFlightId(this.getFlightId());
            ObjectMapper objectMapper = new ObjectMapper();
            String json = null;

            try {
                json = objectMapper.writeValueAsString(canceled);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON format exception", e);
            }

            KafkaProcessor processor = Application.applicationContext.getBean(KafkaProcessor.class);
            MessageChannel outputChannel = processor.outboundTopic();

            outputChannel.send(MessageBuilder
                    .withPayload(json)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build());
        }else{
            Reserved reserved = new Reserved();
            reserved.setUserId(this.getUserId());
            reserved.setFlightId(this.getFlightId());
            reserved.setStatus(this.getStatus());
            ObjectMapper objectMapper = new ObjectMapper();
            String json = null;

            try {
                json = objectMapper.writeValueAsString(reserved);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON format exception", e);
            }

            KafkaProcessor processor = Application.applicationContext.getBean(KafkaProcessor.class);
            MessageChannel outputChannel = processor.outboundTopic();

            outputChannel.send(MessageBuilder
                    .withPayload(json)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build());
        }


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public Long getUserMoney() {
        return userMoney;
    }

    public void setUserMoney(Long userMoney) {
        this.userMoney = userMoney;
    }
    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }




}
