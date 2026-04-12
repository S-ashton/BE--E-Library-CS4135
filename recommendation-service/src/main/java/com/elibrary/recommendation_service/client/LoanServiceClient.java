package com.elibrary.recommendation_service.client;

import java.util.List;

public interface LoanServiceClient {

    List<String> getBorrowedBookIds(String userId);
}
