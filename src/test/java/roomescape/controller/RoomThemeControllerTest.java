package roomescape.controller;

import static roomescape.TestFixture.ROOM_THEME_FIXTURE;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import roomescape.dao.RoomThemeDao;
import roomescape.domain.RoomTheme;
import roomescape.dto.request.RoomThemeCreateRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomThemeControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private RoomThemeDao roomThemeDao;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        List<RoomTheme> roomThemes = roomThemeDao.findAll();
        for (RoomTheme roomTheme : roomThemes) {
            roomThemeDao.deleteById(roomTheme.getId());
        }
    }

    @DisplayName("테마 추가 테스트")
    @Test
    void createTheme() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new RoomThemeCreateRequest("레벨2 탈출", "우테코 레벨2",
                        "https://i.pinimg.com/236x/6e"))
                .when().post("/themes")
                .then().log().all().assertThat().statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("테마 전체 조회 테스트")
    @Test
    void findAllThemes() {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("테마 삭제 테스트")
    @Test
    void deleteTheme() {
        // given
        RoomTheme savedRoomTheme = roomThemeDao.save(ROOM_THEME_FIXTURE);
        // when & then
        RestAssured.given().log().all()
                .when().delete("/themes/" + savedRoomTheme.getId())
                .then().log().all().assertThat().statusCode(HttpStatus.NO_CONTENT.value());
    }
}
