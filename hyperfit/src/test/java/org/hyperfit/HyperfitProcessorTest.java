package org.hyperfit;

import org.hyperfit.exception.ResponseException;
import org.hyperfit.net.*;
import org.hyperfit.resource.HyperResource;
import org.hyperfit.resource.InterfaceSelectionStrategy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.LinkedHashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HyperfitProcessorTest {

    public interface BaseProfileResource extends HyperResource {

    }

    public interface ProfileResource1 extends BaseProfileResource {
    }

    public interface ProfileResource2 extends BaseProfileResource {
    }


    @Mock
    private HyperResource mockHyperResource;

    @Mock
    private HyperClient mockHyperClient;

    @Mock
    protected InterfaceSelectionStrategy mockSelectionStrategy;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = ResponseException.class)
    public void testBuildResourceNoContentTypeException() {
        HyperfitProcessor hyperfitProcessor = HyperfitProcessor.builder()
                .hyperClient(mockHyperClient)
                .build();

        Request request = new RFC6570RequestBuilder().setUrlTemplate("http://here.com").build();
        Response response = new Response.ResponseBuilder()
            .addRequest(request)
            .build();

        hyperfitProcessor.buildHyperResource(response, HyperResource.class);
    }

    @Test(expected = ResponseException.class)
    public void testBuildResourceNoHyperMediaTypeHandlerException() {
        HyperfitProcessor hyperfitProcessor = HyperfitProcessor.builder()
                .hyperClient(mockHyperClient)
                .build();

        Request request = new RFC6570RequestBuilder().setUrlTemplate("http://here.com").build();
        Response response = new Response.ResponseBuilder()
            .addRequest(request)
            .addHeader(HttpHeader.CONTENT_TYPE, "someType")
            .build();

        hyperfitProcessor.buildHyperResource(response, HyperResource.class);
    }

    @Test
    public void testInvokeSingleProfileResourceTest() {


        HyperfitProcessor processor = HyperfitProcessor.builder()
            .hyperClient(mockHyperClient)
            .interfaceSelectionStrategy(mockSelectionStrategy)
            .build();

        when(mockSelectionStrategy.determineInterfaces(BaseProfileResource.class, mockHyperResource))
            .thenReturn(new Class[]{ProfileResource1.class, ProfileResource2.class});

        BaseProfileResource result = processor.processResource(BaseProfileResource.class, mockHyperResource, null);

        assertTrue(result instanceof ProfileResource1);
        assertTrue(result instanceof ProfileResource2);

    }



    @Test
    public void testProcessResourceWithArrayOfRegisteredProfiles() {



        HyperfitProcessor processor = HyperfitProcessor.builder()
            .hyperClient(mockHyperClient)
            .interfaceSelectionStrategy(mockSelectionStrategy)
            .build();

        when(mockSelectionStrategy.determineInterfaces(BaseProfileResource.class, mockHyperResource))
            .thenReturn(new Class[]{ProfileResource1.class, ProfileResource2.class});


        BaseProfileResource result = processor.processResource(BaseProfileResource.class, mockHyperResource, null);

        assertTrue(result instanceof ProfileResource1);
        assertTrue(result instanceof ProfileResource2);

    }


    @Test
    public void testProcessResourceWithUnregisteredProfile() {


        HyperfitProcessor processor = HyperfitProcessor.builder()
            .hyperClient(mockHyperClient)
            .interfaceSelectionStrategy(mockSelectionStrategy)
            .build();

        when(mockSelectionStrategy.determineInterfaces(BaseProfileResource.class, mockHyperResource))
            .thenReturn(new Class[]{BaseProfileResource.class});


        BaseProfileResource result = processor.processResource(BaseProfileResource.class, mockHyperResource, null);

        assertTrue(result instanceof BaseProfileResource);

    }
}