package io.axoniq.foodordering.query;

import io.axoniq.foodordering.coreapi.*;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Profile("query")
@Component
class FoodCartProjector {

    private final FoodCartViewRepository foodCartViewRepository;

    public FoodCartProjector(FoodCartViewRepository foodCartViewRepository) {
        this.foodCartViewRepository = foodCartViewRepository;
    }

    @EventHandler
    public void on(FoodCartCreatedEvent event) {
        FoodCartView foodCartView = new FoodCartView(event.getFoodCartId(), Collections.emptyMap());
        foodCartViewRepository.save(foodCartView);
    }

    @EventHandler
    public void on(ProductSelectedEvent event) {
        foodCartViewRepository.findById(event.getFoodCartId()).ifPresent(
                foodCartView -> foodCartView.addProducts(event.getProductId(), event.getQuantity())
        );
    }

    @EventHandler
    public void on(ProductDeselectedEvent event) {
        foodCartViewRepository.findById(event.getFoodCartId()).ifPresent(
                foodCartView -> foodCartView.removeProducts(event.getProductId(), event.getQuantity())
        );
    }

    @QueryHandler
    public FoodCartView handle(FindFoodCartQuery query) {
        return foodCartViewRepository.findById(query.getFoodCartId()).orElse(null);
    }

    @QueryHandler
    public List<FoodCartView> handle(RetrieveAllFoodCartsQuery query) {
        return foodCartViewRepository.findAll().stream().collect(Collectors.toList());
    }
}
