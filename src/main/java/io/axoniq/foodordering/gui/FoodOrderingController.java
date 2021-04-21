package io.axoniq.foodordering.gui;

import io.axoniq.foodordering.coreapi.CreateFoodCartCommand;
import io.axoniq.foodordering.coreapi.DeselectProductCommand;
import io.axoniq.foodordering.coreapi.FindFoodCartQuery;
import io.axoniq.foodordering.coreapi.SelectProductCommand;
import io.axoniq.foodordering.query.FoodCartView;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Profile("gui")
@RequestMapping("/foodCart")
@RestController
class FoodOrderingController {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final EventStore eventStore;

    public FoodOrderingController(CommandGateway commandGateway, QueryGateway queryGateway, EventStore eventStore) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.eventStore = eventStore;
    }


    @PostMapping("/create")
    public CompletableFuture<UUID> createFoodCart() {
        return commandGateway.send(new CreateFoodCartCommand(UUID.randomUUID()));
    }

    @PostMapping("/{foodCartId}/select/{productId}/quantity/{quantity}")
    public void selectProduct(@PathVariable("foodCartId") String foodCartId,
                              @PathVariable("productId") String productId,
                              @PathVariable("quantity") Integer quantity) {
        commandGateway.send(new SelectProductCommand(
                UUID.fromString(foodCartId), productId, quantity
        ));
    }

    @PostMapping("/{foodCartId}/deselect/{productId}/quantity/{quantity}")
    public void deselectProduct(@PathVariable("foodCartId") String foodCartId,
                                @PathVariable("productId") String productId,
                                @PathVariable("quantity") Integer quantity) {
        commandGateway.send(new DeselectProductCommand(
                UUID.fromString(foodCartId), productId, quantity
        ));
    }

    @GetMapping("/{foodCartId}")
    public CompletableFuture<FoodCartView> findFoodCart(@PathVariable("foodCartId") String foodCartId) {
        return queryGateway.query(
                new FindFoodCartQuery(UUID.fromString(foodCartId)),
                ResponseTypes.instanceOf(FoodCartView.class)
        );
    }

    @GetMapping("{foodCartId}/foodCartHistory")
    public List<Object> getFoodCartHistory(String foodCartId) {
        return eventStore.readEvents(foodCartId).asStream().map( s -> s.getPayload().toString()).collect(Collectors.toList());
    }

}
