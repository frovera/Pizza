package it.fr.awesomepizza.service;

import it.fr.awesomepizza.model.Pizza;
import it.fr.awesomepizza.repository.PizzaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private PizzaRepository pizzaRepository;

    @InjectMocks
    private CacheService cacheService;

    private Pizza margherita;

    @BeforeEach
    void setUp() {
        margherita = Pizza.of("Margherita", "Pomodoro, mozzarella", new BigDecimal("6.00"));
        margherita.setId(1L);
    }

    @Test
    void findPizzaById_hitsRepositoryOnFirstCall() {
        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(margherita));

        Optional<Pizza> result = cacheService.findPizzaById(1L);

        assertThat(result).contains(margherita);
        verify(pizzaRepository, times(1)).findById(1L);
    }

    @Test
    void findPizzaById_secondCallDoesNotHitRepositoryAgain() {
        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(margherita));

        cacheService.findPizzaById(1L);
        Optional<Pizza> secondResult = cacheService.findPizzaById(1L);

        assertThat(secondResult).contains(margherita);
        verify(pizzaRepository, times(1)).findById(1L);
    }

    @Test
    void findPizzaById_returnsEmptyWhenPizzaNotFound() {
        when(pizzaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Pizza> result = cacheService.findPizzaById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findPizzaById_doesNotCacheEmptyResults() {
        when(pizzaRepository.findById(99L)).thenReturn(Optional.empty());

        cacheService.findPizzaById(99L);
        cacheService.findPizzaById(99L);

        verify(pizzaRepository, times(2)).findById(99L);
    }

    @Test
    void findPizzaById_differentPizzasAreCachedSeparately() {
        Pizza marinara = Pizza.of("Marinara", "Pomodoro, aglio", new BigDecimal("5.00"));
        marinara.setId(2L);
        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(margherita));
        when(pizzaRepository.findById(2L)).thenReturn(Optional.of(marinara));

        assertThat(cacheService.findPizzaById(1L)).contains(margherita);
        assertThat(cacheService.findPizzaById(2L)).contains(marinara);

        verify(pizzaRepository, times(1)).findById(1L);
        verify(pizzaRepository, times(1)).findById(2L);
    }
}
