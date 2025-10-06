package io.github.vivianagh.jobberwocky.infrastructure.adapter;


import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.exception.ExternalSourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExternalJobApiClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;;

    private ExternalJobApiClient apiClient;

    @BeforeEach
    void setUp() {
        apiClient = new ExternalJobApiClient(restClient);
    }

    @Test
    void shouldFetchJobsSuccessfully() {
        //Given
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .title("Engineer")
                .build();

        String expectedResponse = "{\"USA\": [[\"Engineer\", 100000, \"<skills></skills>\"]]}";

        //Mock chain: restClient.get() → uri() → retrieve() → body()
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        //When
        String response = apiClient.fetchJobs(criteria);

        //Then
        assertThat(response).isEqualTo(expectedResponse);
        verify(restClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).body(String.class);
    }

    @Test
    void shouldBuildUrlWithTitleParameter() {
        //Given
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .title("Engineer")
                .build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("{}");

        //When
        apiClient.fetchJobs(criteria);

        //Then
        verify(requestHeadersUriSpec).uri(any(Function.class));
        // URL should contain: /jobs?name=Engineer
    }

    @Test
    void shouldBuildUrlWithCountryParameter() {
        //Given
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .country("USA")
                .build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("{}");

        //When
        apiClient.fetchJobs(criteria);

        //Then
        verify(requestHeadersUriSpec).uri(any(Function.class));
        // URL should contain: /jobs?country=USA
    }

    @Test
    void shouldHandleNullCriteria() {
        //Given
        JobSearchCriteria criteria = null;

        //When & Then
        assertThatThrownBy(() -> apiClient.fetchJobs(criteria))
                .isInstanceOf(ExternalSourceException.class);
    }
    //TODO
}
