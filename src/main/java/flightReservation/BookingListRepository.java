package flightReservation;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingListRepository extends CrudRepository<BookingList, Long> {

    List<BookingList> findByUserId(String userId);

        void deleteByFlightId(String flightId);
}