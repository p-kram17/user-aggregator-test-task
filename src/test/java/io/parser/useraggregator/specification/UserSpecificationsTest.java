package io.parser.useraggregator.specification;

import static org.assertj.core.api.Assertions.assertThat;

import io.parser.useraggregator.config.AppDataSourceProperties;
import io.parser.useraggregator.dto.UserFilterRequest;
import io.parser.useraggregator.strategy.MySqlDatabaseQueryStrategy;
import org.junit.jupiter.api.Test;

class UserSpecificationsTest {

    private static final AppDataSourceProperties.Mapping MAPPING =
            new AppDataSourceProperties.Mapping("person_id", "login", "first_name", "last_name");

    @Test
    void shouldBuildComposableSqlClauseForAllFilters() {
        UserFilterSpecification specification = UserSpecifications.fromRequest(
                new UserFilterRequest("jdoe", "John", "Doe"));

        SqlFilterClause filterClause = specification.toSql(MAPPING, new MySqlDatabaseQueryStrategy());

        assertThat(filterClause.sql()).isEqualTo(
                "(LOWER(`login`) = ?) AND (LOWER(`first_name`) = ?) AND (LOWER(`last_name`) = ?)");
        assertThat(filterClause.parameters()).containsExactly("jdoe", "john", "doe");
    }

    @Test
    void shouldReturnEmptyClauseWhenFiltersAreMissing() {
        UserFilterSpecification specification = UserSpecifications.fromRequest(
                new UserFilterRequest(null, " ", null));

        SqlFilterClause filterClause = specification.toSql(MAPPING, new MySqlDatabaseQueryStrategy());

        assertThat(filterClause.isEmpty()).isTrue();
        assertThat(filterClause.parameters()).isEmpty();
    }
}
