package com.apigateway.service;

import com.apigateway.entity.Route;
import com.apigateway.exception.DuplicateRouteException;
import com.apigateway.exception.RouteNotFoundException;
import com.apigateway.repository.RouteRepository;
import com.apigateway.security.TargetUrlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;
    @Mock
    private TargetUrlValidator targetUrlValidator;

    @InjectMocks
    private RouteService routeService;


    private Route route;

    @BeforeEach
    void setUp() {
        route = mock(Route.class);
    }

    @Test
    void findRouteForPath_returnsExactMatch() {
        when(routeRepository.findByPath("/products"))
                .thenReturn(Optional.of(route));

        Route result = routeService.findRouteForPath("/products");

        assertSame(route, result);
        verify(routeRepository).findByPath("/products");
    }

    @Test
    void findRouteForPath_returnsParentRouteForDynamicPath() {
        when(routeRepository.findByPath("/products/42"))
                .thenReturn(Optional.empty());
        when(routeRepository.findByPath("/products"))
                .thenReturn(Optional.of(route));

        Route result = routeService.findRouteForPath("/products/42");

        assertSame(route, result);
        verify(routeRepository).findByPath("/products/42");
        verify(routeRepository).findByPath("/products");
    }

    @Test
    void findRouteForPath_throwsWhenRouteDoesNotExist() {
        when(routeRepository.findByPath("/unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                RouteNotFoundException.class,
                () -> routeService.findRouteForPath("/unknown")
        );
    }

    @Test
    void createRoute_throwsWhenPathAlreadyExists() {
        when(route.getPath()).thenReturn("/products");
        when(routeRepository.findByPath("/products"))
                .thenReturn(Optional.of(route));

        assertThrows(
                DuplicateRouteException.class,
                () -> routeService.createRoute(route)
        );

        verify(routeRepository, never()).save(any(Route.class));
    }
    @Test
    void createRoute_savesAndReturnsRouteWhenPathIsNew() {
        when(route.getPath()).thenReturn("/products");
        when(routeRepository.findByPath("/products"))
                .thenReturn(Optional.empty());

        when(routeRepository.save(route)).thenReturn(route);

        Route result = routeService.createRoute(route);

        assertSame(route, result);
        verify(routeRepository).save(route);
    }
    @Test
    void findRouteForPath_throwsWhenParentRouteDoesNotExist() {
        when(routeRepository.findByPath("/products/42"))
                .thenReturn(Optional.empty());

        when(routeRepository.findByPath("/products"))
                .thenReturn(Optional.empty());

        assertThrows(
                RouteNotFoundException.class,
                () -> routeService.findRouteForPath("/products/42")
        );
    }
    @Test
    void findRouteForPath_whenRouteDoesNotExist_throwsException() {
        when(routeRepository.findByPath("/unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                RouteNotFoundException.class,
                () -> routeService.findRouteForPath("/unknown")
        );
    }
    @Test
    void findRouteForPath_whenExactRouteMissing_usesParentRoute() {
        Route parentRoute = mock(Route.class);

        when(routeRepository.findByPath("/products/42"))
                .thenReturn(Optional.empty());

        when(routeRepository.findByPath("/products"))
                .thenReturn(Optional.of(parentRoute));

        Route result = routeService.findRouteForPath("/products/42");

        assertEquals(parentRoute, result);
    }
    @Test
    void findRouteForPath_whenExactRouteExists_returnsExactRoute() {
        Route exactRoute = mock(Route.class);

        when(routeRepository.findByPath("/products/42"))
                .thenReturn(Optional.of(exactRoute));

        Route result = routeService.findRouteForPath("/products/42");

        assertEquals(exactRoute, result);
    }
    @Test
    void createRoute_whenPathAlreadyExists_throwsException() {
        Route route = mock(Route.class);

        when(route.getPath()).thenReturn("/products");
        when(routeRepository.findByPath("/products"))
                .thenReturn(Optional.of(route));

        assertThrows(
                DuplicateRouteException.class,
                () -> routeService.createRoute(route)
        );
    }
    @Test
    void createRoute_whenPathDoesNotExist_savesAndReturnsRoute() {
        Route route = mock(Route.class);

        when(route.getPath()).thenReturn("/orders");
        when(routeRepository.findByPath("/orders"))
                .thenReturn(Optional.empty());
        when(routeRepository.save(route))
                .thenReturn(route);

        Route result = routeService.createRoute(route);

        assertEquals(route, result);
        verify(routeRepository).save(route);
    }

    @Test
    void createRoute_validatesTargetUrl() {
        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081/products");

        when(routeRepository.findByPath("/products"))
                .thenReturn(Optional.empty());

        when(routeRepository.save(route))
                .thenReturn(route);

        routeService.createRoute(route);

        verify(targetUrlValidator).validate(
                "http://localhost:8081/products"
        );
    }
    @Test
    void createRoute_withInvalidTargetUrl_throwsException() {
        when(route.getTargetUrl())
                .thenReturn("ftp://example.com/products");

        doThrow(new IllegalArgumentException("Invalid target URL"))
                .when(targetUrlValidator)
                .validate("ftp://example.com/products");

        assertThrows(
                IllegalArgumentException.class,
                () -> routeService.createRoute(route)
        );

        verify(routeRepository, never())
                .findByPath(anyString());

        verify(routeRepository, never())
                .save(any(Route.class));
    }
}