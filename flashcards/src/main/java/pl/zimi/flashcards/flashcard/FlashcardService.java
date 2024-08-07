package pl.zimi.flashcards.flashcard;

import lombok.RequiredArgsConstructor;
import pl.zimi.flashcards.strategy.ExpotentialMemorizationStrategy;
import pl.zimi.flashcards.user.UserId;
import pl.zimi.repository.query.*;

import java.time.Clock;
import java.util.Optional;

@RequiredArgsConstructor
public class FlashcardService {

    final FlashcardRepository flashcardRepository;

    final Clock clock;

    public Optional<Question> next(UserId userId) {
        final var userFilter = Filters.eq(SFlashcard.flashcard.userId, userId);
        final var useAfterFilter = Filters.lt(SFlashcard.flashcard.memorizationLevel.useAfter, clock.instant());
        final var useAfterIsNullFilter = Filters.isNull(SFlashcard.flashcard.memorizationLevel.useAfter);
        final var filter = Filters.and(userFilter, Filters.or(useAfterFilter, useAfterIsNullFilter));

        final var sorter = Sorters.asc(SFlashcard.flashcard.memorizationLevel.numberOfSuccesses);
        final var query = Queries.query(filter, sorter, new LimitOffset(1L, 0L));
        final var found = flashcardRepository.find(query);
        return found.stream().findFirst().map(flashcard -> Question.builder().flashcardId(flashcard.getId()).original(flashcard.getOriginal()).build());
    }

    public Flashcard add(AddFlashcardRequest request) {
        final var flashcard = Flashcard.builder()
                .userId(request.getUserId())
                .original(request.getOriginal())
                .translation(request.getTranslation())
                .memorizationLevel(MemorizationLevel.none())
                .deckId(request.getDeckId())
                .build();
        return flashcardRepository.save(flashcard);
    }

    public AnswerResult answer(Answer answer) {
        final var memorizationStrategy = new ExpotentialMemorizationStrategy();
        final var flashcards = flashcardRepository.findById(answer.getFlashcardId());
        if (flashcards.isEmpty()) {
            return AnswerResult.failure();
        }
        final var flashcard = flashcards.get();
        if (flashcard.getTranslation().getText().equals(answer.getTranslation())) {
            flashcard.memorizationLevel.upgrade(clock.instant(), memorizationStrategy);
            flashcardRepository.save(flashcard);
            return AnswerResult.correct();
        } else {
            flashcard.memorizationLevel.downgrade(clock.instant(), memorizationStrategy);
            flashcardRepository.save(flashcard);
            return AnswerResult.mistake();
        }
    }
}
