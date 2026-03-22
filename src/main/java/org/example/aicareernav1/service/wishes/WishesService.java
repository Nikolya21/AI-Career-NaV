package org.example.aicareernav1.service.wishes;

import lombok.RequiredArgsConstructor;

import org.example.aicareernav1.dto.wishes.WishesRequest;
import org.example.aicareernav1.dto.wishes.WishesResponse;
import org.example.aicareernav1.repository.WishesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // import из Spring

@Service
@RequiredArgsConstructor
public class WishesService implements WishesRepository {

  // Когда появится БД, раскомментируй это:
  // private final UserRepository userRepository;

  @Override
  @Transactional // Оставляем аннотацию, она не помешает, даже если БД пока нет
  public WishesResponse saveAndProcessWishes(WishesRequest request) {

    // 1. ВАЛИДАЦИЯ
    if (request.getDesiredProfession() == null || request.getDesiredProfession().isBlank()) {
      return new WishesResponse(false, "Укажите профессию!", null);
    }

    // 2. ЛОГИКА СОХРАНЕНИЯ (Заглушка для БД)
    try {
      System.out.println("--- СОХРАНЕНИЕ В БД (ИМИТАЦИЯ) ---");
      System.out.println("ID Пользователя: " + request.getUserId());
      System.out.println("Выбранная профессия: " + request.getDesiredProfession());
      System.out.println("Выжимка пожеланий: " + request.getAdditionalComments());

            /* КОГДА ПОДКЛЮЧИШЬ БД, КОД БУДЕТ ТАКИМ:

               User user = userRepository.findById(request.getUserId())
                                         .orElseThrow(() -> new EntityNotFoundException("Юзер не найден"));

               user.setTargetProfession(request.getDesiredProfession());
               user.setCareerWishes(request.getAdditionalComments());

               userRepository.save(user);
            */

      System.out.println("--- ДАННЫЕ УСПЕШНО ЗАПИСАНЫ ---");

    } catch (Exception e) {
      // Если что-то пойдет не так при работе с БД
      return new WishesResponse(false, "Ошибка при сохранении: " + e.getMessage(), null);
    }

    // 3. ПЕРЕХОД К СЛЕДУЮЩЕМУ ЭТАПУ
    // Здесь мы возвращаем URL страницы, где пользователь увидит результаты парсинга
    return new WishesResponse(
      true,
      "Пожелания учтены. Начинаем поиск вакансий для вас!",
      "/vacancies/selection"
    );
  }
}