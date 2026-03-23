package io.parser.useraggregator.repository;

import io.parser.useraggregator.config.SourceContext;
import io.parser.useraggregator.dto.UserResponse;
import io.parser.useraggregator.exception.SourceAccessException;
import io.parser.useraggregator.specification.SqlFilterClause;
import io.parser.useraggregator.specification.UserFilterSpecification;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class AggregatedUserRepository {

    private static final RowMapper<UserResponse> USER_ROW_MAPPER = (rs, rowNum) -> new UserResponse(
            rs.getString("id"),
            rs.getString("username"),
            rs.getString("name"),
            rs.getString("surname"));

    public List<UserResponse> findUsers(SourceContext source, UserFilterSpecification specification) {
        SqlFilterClause filterClause = specification.toSql(source.properties().mapping(), source.queryStrategy());
        String sql = source.queryStrategy().buildSelectUsersQuery(source.properties(), filterClause);

        try {
            log.debug("Fetching users from source '{}'", source.properties().name());
            return source.jdbcTemplate().query(sql, USER_ROW_MAPPER, filterClause.parameters().toArray());
        } catch (DataAccessException exception) {
            throw new SourceAccessException(source.properties().name(), exception);
        }
    }
}
