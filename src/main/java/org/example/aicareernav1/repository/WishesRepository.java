package org.example.aicareernav1.repository;

import org.example.aicareernav1.dto.wishes.WishesRequest;
import org.example.aicareernav1.dto.wishes.WishesResponse;

public interface WishesRepository {
  WishesResponse saveAndProcessWishes(WishesRequest request);
}
