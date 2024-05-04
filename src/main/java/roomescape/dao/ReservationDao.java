package roomescape.dao;

import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoomTheme;

@Repository
public class ReservationDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Reservation> reservationMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("reservation_id"),
            rs.getString("reservation_name"),
            rs.getDate("reservation_date").toLocalDate(),
            new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getTime("time_value").toLocalTime()
            ),
            new RoomTheme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail")
            )
    );

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public List<Reservation> findAll() {
        String selectSql = """
                SELECT
                    r.ID AS reservation_id,
                    r.NAME AS reservation_name,
                    r.DATE AS reservation_date,
                    t.ID AS time_id,
                    t.START_AT AS time_value,
                    th.ID AS theme_id,
                    th.NAME AS theme_name,
                    th.DESCRIPTION AS theme_description,
                    th.THUMBNAIL AS theme_thumbnail
                FROM reservation AS r
                INNER JOIN reservation_time AS t
                ON r.time_id = t.id
                INNER JOIN theme AS th
                ON r.theme_id = th.id
                """;
        return jdbcTemplate.query(selectSql, reservationMapper);
    }

    public List<Reservation> findByTheme(Long themeId) {
        String SELECT_SQL = """
                SELECT
                    r.ID AS reservation_id,
                    r.NAME AS reservation_name,
                    r.DATE AS reservation_date,
                    t.ID AS time_id,
                    t.START_AT AS time_value,
                    th.ID AS theme_id,
                    th.NAME AS theme_name,
                    th.DESCRIPTION AS theme_description,
                    th.THUMBNAIL AS theme_thumbnail
                FROM reservation AS r
                INNER JOIN reservation_time AS t
                ON r.time_id = t.id
                INNER JOIN theme AS th
                ON r.theme_id = th.id
                WHERE r.THEME_ID = ?
                """;
        return jdbcTemplate.query(SELECT_SQL, reservationMapper, themeId);
    }

    public boolean existsByDateTime(LocalDate date, Long timeId, Long themeId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT * FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?)",
                Boolean.class, date, timeId, themeId));
    }

    public Reservation save(Reservation reservation) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameterSource).longValue();
        return reservation.setId(id);
    }

    public boolean deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id) > 0;
    }
}
