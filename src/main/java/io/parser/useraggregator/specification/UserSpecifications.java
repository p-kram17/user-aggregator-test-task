package io.parser.useraggregator.specification;

import io.parser.useraggregator.dto.UserFilterRequest;
import java.util.ArrayList;
import java.util.List;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static UserFilterSpecification fromRequest(UserFilterRequest request) {
        List<UserFilterSpecification> specifications = new ArrayList<>();
        specifications.add(new UsernameSpecification(request.username()));
        specifications.add(new NameSpecification(request.name()));
        specifications.add(new SurnameSpecification(request.surname()));
        return new AndUserFilterSpecification(specifications);
    }
}
