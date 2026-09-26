package com.hotel.booking.service;

import com.hotel.booking.dto.ImageDTO;
import com.hotel.booking.dto.RoomWithImagesDTO;
import com.hotel.booking.model.Image;
import com.hotel.booking.model.Room;
import com.hotel.booking.model.enums.RoomType;
import com.hotel.booking.repository.BookingMongoRepository;
import com.hotel.booking.repository.ImageMongoRepository;
import com.hotel.booking.repository.RoomMongoRepository;
import com.hotel.booking.repository.UserMongoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Свободните стаи за чата идват със снимките си – в реда на imageIds, без изтритите
class HotelServiceImagesTest {

    private final RoomMongoRepository roomRepo = mock(RoomMongoRepository.class);
    private final ImageMongoRepository imageRepo = mock(ImageMongoRepository.class);
    private final HotelService service = new HotelService(roomRepo, mock(UserMongoRepository.class),
            mock(BookingMongoRepository.class), imageRepo);

    @Test
    void roomsComeWithTheirImagesInOrderWithOneImageQuery() {
        when(roomRepo.findAll()).thenReturn(List.of(
                room("r-1", 12, List.of("img-2", "deleted", "img-1")),
                room("r-2", 14, List.of())));
        when(imageRepo.findAllById(any())).thenReturn(List.of(
                image("img-1", "https://cdn.example.com/1.jpg"),
                image("img-2", "https://cdn.example.com/2.jpg")));

        List<RoomWithImagesDTO> rooms = service.findAvailableRoomsWithImages(
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), null);

        assertThat(rooms).hasSize(2);
        assertThat(rooms.get(0).images()).extracting(ImageDTO::getUrl)
                .containsExactly("https://cdn.example.com/2.jpg", "https://cdn.example.com/1.jpg");
        assertThat(rooms.get(1).images()).isEmpty();
        verify(imageRepo, times(1)).findAllById(any());
    }

    private static Room room(String id, int number, List<String> imageIds) {
        Room room = new Room();
        ReflectionTestUtils.setField(room, "id", id);
        room.setRoomNumber(number);
        room.setType(RoomType.DOUBLE);
        room.setPricePerNight(100);
        room.setImageIds(imageIds);
        return room;
    }

    private static Image image(String id, String url) {
        Image image = new Image();
        image.setId(id);
        image.setUrl(url);
        return image;
    }
}
