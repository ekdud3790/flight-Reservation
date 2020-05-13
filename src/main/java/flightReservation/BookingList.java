package flightReservation;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="BookingList_table")
public class BookingList {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private String userId;
        private String reserveStatus;
        private String reserveId;


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
        public String getReserveStatus() {
            return reserveStatus;
        }

        public void setReserveStatus(String reserveStatus) {
            this.reserveStatus = reserveStatus;
        }
        public String getReserveId() {
            return reserveId;
        }

        public void setReserveId(String reserveId) {
            this.reserveId = reserveId;
        }

}
