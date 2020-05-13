package flightReservation;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
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
        ResvCanceled resvCanceled = new ResvCanceled();
        BeanUtils.copyProperties(this, resvCanceled);
        resvCanceled.publishAfterCommit();


        Requested requested = new Requested();
        BeanUtils.copyProperties(this, requested);
        requested.publishAfterCommit();


    }

    @PostUpdate
    public void onPostUpdate(){
        Reserved reserved = new Reserved();
        BeanUtils.copyProperties(this, reserved);
        reserved.publishAfterCommit();


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
