package flightReservation;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="BookingList_table")
public class BookingList {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private String id;
        private String userId;
        private String reserveStatus;
        private String flightId;
        private Long userMoney;


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
        public String getReserveStatus() {
            return reserveStatus;
        }

        public void setReserveStatus(String reserveStatus) {
            this.reserveStatus = reserveStatus;
        }
        public String getFlightId() {
            return flightId;
        }

        public void setFlightId(String flightId) {
            this.flightId = flightId;
        }
        public Long getUserMoney() {
            return userMoney;
        }

        public void setUserMoney(Long userMoney) {
            this.userMoney = userMoney;
        }

}
