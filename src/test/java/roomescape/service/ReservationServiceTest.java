package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.RESERVATION_TIME_FIXTURE;
import static roomescape.TestFixture.ROOM_THEME_FIXTURE;
import static roomescape.TestFixture.VALID_STRING_DATE_FIXTURE;
import static roomescape.TestFixture.VALID_STRING_TIME_FIXTURE;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.RoomThemeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoomTheme;
import roomescape.handler.BadRequestException;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.response.ReservationResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationServiceTest {
    @LocalServerPort
    private int port;

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ReservationTimeDao reservationTimeDao;
    @Autowired
    private RoomThemeDao roomThemeDao;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        List<Reservation> reservations = reservationDao.findAll();
        for (Reservation reservation : reservations) {
            reservationDao.deleteById(reservation.getId());
        }
        List<ReservationTime> reservationTimes = reservationTimeDao.findAll();
        for (ReservationTime reservationTime : reservationTimes) {
            reservationTimeDao.deleteById(reservationTime.getId());
        }
        List<RoomTheme> roomThemes = roomThemeDao.findAll();
        for (RoomTheme roomTheme : roomThemes) {
            roomThemeDao.deleteById(roomTheme.getId());
        }
    }

    @DisplayName("모든 예약 검색")
    @Test
    void findAll() {
        assertThat(reservationService.findAll()).isEmpty();
    }

    @DisplayName("예약 저장")
    @Test
    void save() {
        // given
        ReservationRequest reservationRequest = createReservationRequest(VALID_STRING_DATE_FIXTURE);
        // when
        ReservationResponse response = reservationService.save(reservationRequest);
        // then
        assertAll(
                () -> assertThat(reservationService.findAll()).hasSize(1),
                () -> assertThat(response.name()).isEqualTo("aa"),
                () -> assertThat(response.date()).isEqualTo(VALID_STRING_DATE_FIXTURE),
                () -> assertThat(response.time().startAt()).isEqualTo(VALID_STRING_TIME_FIXTURE)
        );
    }

    @DisplayName("지난 예약을 저장하려 하면 예외가 발생한다.")
    @Test
    void pastReservationSaveThrowsException() {
        // given
        ReservationRequest reservationRequest = createReservationRequest("2000-11-09");
        // when & then
        assertThatThrownBy(() -> reservationService.save(reservationRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("지나간 날짜와 시간에 대한 예약을 생성할 수 없습니다.");
    }

    @DisplayName("삭제 테스트")
    @Test
    void deleteById() {
        // given
        createReservationRequest(VALID_STRING_DATE_FIXTURE);
        // when
        reservationService.deleteById(1);
        // then
        assertThat(reservationService.findAll()).isEmpty();
    }

    private ReservationRequest createReservationRequest(String date) {
        ReservationTime savedReservationTime = reservationTimeDao.save(
                RESERVATION_TIME_FIXTURE);
        RoomTheme savedRoomTheme = roomThemeDao.save(ROOM_THEME_FIXTURE);
        return new ReservationRequest("aa", LocalDate.parse(date),
                savedReservationTime.getId(), savedRoomTheme.getId());
    }
}
