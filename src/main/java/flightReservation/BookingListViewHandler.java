package flightReservation;

import flightReservation.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class BookingListViewHandler {


    @Autowired
    private BookingListRepository bookingListRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenReserved_then_CREATE_1 (@Payload Reserved reserved) {
        try {
            if (reserved.isMe()) {
                // view 객체 생성
                BookingList bookingList = new BookingList();
                // view 객체에 이벤트의 Value 를 set 함
                bookingList.setFlightId(reserved.getFlightId());
                // view 레파지 토리에 save
                bookingListRepository.save(bookingList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayCompleted_then_UPDATE_1(@Payload PayCompleted payCompleted) {
        try {
            if (payCompleted.isMe()) {
                // view 객체 조회
                List<BookingList> bookingListList = bookingListRepository.findByUserId(payCompleted.getUserId());
                for(BookingList bookingList : bookingListList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    bookingList.setUserMoney(payCompleted.getPrice());
                    // view 레파지 토리에 save
                    bookingListRepository.save(bookingList);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayFailed_then_UPDATE_2(@Payload PayFailed payFailed) {
        try {
            if (payFailed.isMe()) {
                // view 객체 조회
                List<BookingList> bookingListList = bookingListRepository.findByUserId(payFailed.getUserId());
                for(BookingList bookingList : bookingListList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    bookingList.setUserMoney(payFailed.getPrice());
                    // view 레파지 토리에 save
                    bookingListRepository.save(bookingList);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayCanceled_then_UPDATE_3(@Payload PayCanceled payCanceled) {
        try {
            if (payCanceled.isMe()) {
                // view 객체 조회
                List<BookingList> bookingListList = bookingListRepository.findByUserId(payCanceled.getUserId());
                for(BookingList bookingList : bookingListList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    bookingList.setUserMoney(payCanceled.getPrice());
                    // view 레파지 토리에 save
                    bookingListRepository.save(bookingList);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenResvCanceled_then_DELETE_1(@Payload ResvCanceled resvCanceled) {
        try {
            if (resvCanceled.isMe()) {
                // view 레파지 토리에 삭제 쿼리
                bookingListRepository.deleteByFlightId(resvCanceled.getFlightId());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}