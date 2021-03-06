package org.hyperfit;


import lombok.NonNull;
import org.hyperfit.content.ContentRegistry;
import org.hyperfit.content.ContentType;
import org.hyperfit.content.ContentTypeHandler;
import org.hyperfit.errorhandler.DefaultErrorHandler;
import org.hyperfit.errorhandler.ErrorHandler;
import org.hyperfit.exception.HyperfitException;
import org.hyperfit.handlers.Java8DefaultMethodHandler;
import org.hyperfit.methodinfo.ConcurrentHashMapResourceMethodInfoCache;
import org.hyperfit.methodinfo.ResourceMethodInfoCache;
import org.hyperfit.net.*;
import org.hyperfit.resource.HyperResource;
import org.hyperfit.resource.InterfaceSelectionStrategy;
import org.hyperfit.resource.SimpleInterfaceSelectionStrategy;
import org.hyperfit.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;

import static org.hyperfit.utils.MoreObjects.firstNonNull;

/**
 * Processes a request returning a type specified by the
 * classToReturn parameter in #processRequest by proxifying
 * the resulting hyper resource
 */
public class HyperfitProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(HyperfitProcessor.class);


    private final RequestInterceptors requestInterceptors;
    private final ResourceMethodInfoCache resourceMethodInfoCache;
    //TODO: make this protected hack non-sense go away...something is wrong with our class layout
    protected final ContentRegistry contentRegistry;
    private final ErrorHandler errorHandler;
    private final InterfaceSelectionStrategy interfaceSelectionStrategy;
    private final HyperClientSelectionStrategy clientSelectionStrategy;
    private final Java8DefaultMethodHandler java8DefaultMethodHandler;
    private final ResponseInterceptors responseInterceptors;
    private final List<Pipeline.Step<Response, HyperResource>> responseToResourcePipelineSteps;

    private HyperfitProcessor(Builder builder) {

        contentRegistry = Preconditions.checkNotNull(builder.contentRegistry);
        errorHandler = firstNonNull(builder.errorHandler, new DefaultErrorHandler());
        resourceMethodInfoCache = firstNonNull(builder.resourceMethodInfoCache, new ConcurrentHashMapResourceMethodInfoCache());
        requestInterceptors = firstNonNull(builder.requestInterceptors, new RequestInterceptors());
        responseInterceptors = firstNonNull(builder.responseInterceptors, new ResponseInterceptors());
        interfaceSelectionStrategy =  Preconditions.checkNotNull(builder.interfaceSelectionStrategy);
        java8DefaultMethodHandler = Preconditions.checkNotNull(builder.java8DefaultMethodHandler);

        /*
         * Don't tie ourselves to the actual List in the Builder. If a Step is added/removed from the Pipeline in
         * the builder it will be changed in the Pipeline of all existing HyperfitProcessors (since it's the same List
         * in memory).
         */
        //TODO: check for bad null steps
        //TOOD: should this just be a stack?
        responseToResourcePipelineSteps = new ArrayList<Pipeline.Step<Response, HyperResource>>(builder.responseToResourcePipelineBuilder.steps);

        if(builder.schemeClientMap == null || builder.schemeClientMap.size() == 0){
            throw new IllegalArgumentException("at least one scheme client mapping must be registered");
        }

        /*
         * Like the PipelineSteps above, we don't want to tie ourselves to the actual list in the Builder. If a
         * HyperClient is added/removed from the Map in the builder it will be changed in the Map of all existing
         * HyperfitProcessors (since it's the same Map in memory).
         */
        clientSelectionStrategy = new SchemeBasedHyperClientSelectionStrategy(
            builder.schemeClientMap,
            contentRegistry.getResponseParsingContentTypes()
        );

    }


    /**
     * <p>Obtains a specific resource by going directly to its source.</p>
     *
     * @param classToReturn  the class that the resource should be returned as
     * @param entryPointURL a url to an entry point of the RESTful service
     * @return resource with same type specified in the resource class.
     */
    public <T> T processRequest(Class<T> classToReturn, String entryPointURL){
        if(StringUtils.isEmpty(entryPointURL)){
            throw new IllegalArgumentException("entryPointURL can not be null or empty");
        }
        return processRequest(classToReturn, BoringRequestBuilder.get(entryPointURL));
    }


    /**
     * <p>Obtains a specific resource by going directly to its source.</p>
     *
     * @param classToReturn  the class that the resource should be returned as
     * @param requestBuilder request object
     * @return resource with same type specified in the resource class.
     */
    public <T> T processRequest(Class<T> classToReturn, RequestBuilder requestBuilder){
        return processRequest(classToReturn, requestBuilder, null);
    }

    /**
     * <p>Obtains a specific resource by going directly to its source using super type tokens so a generic can be returned.</p>
     *
     * @param typeToReturn a super type token
     * @param requestBuilder request object
     * @return resource with same type specified in the resource class.
     */
    public <T> T processRequest(TypeRef<T> typeToReturn, RequestBuilder requestBuilder){
        if(typeToReturn == null){
            throw new IllegalArgumentException("typeToReturn can not be null");
        }
        return processRequest(typeToReturn.getClazz(), requestBuilder, new TypeInfo().make(typeToReturn.getType()));
    }

    /**
     * <p>Obtains a specific resource by going directly to its source using super type tokens so a generic can be returned.</p>
     *
     * @param typeToReturn a super type token
     * @param entryPointURL request object
     * @return resource with same type specified in the resource class.
     */
    public <T> T processRequest(TypeRef<T> typeToReturn, String entryPointURL){
        if(StringUtils.isEmpty(entryPointURL)){
            throw new IllegalArgumentException("entryPointURL can not be null or empty");
        }
        return processRequest(typeToReturn, BoringRequestBuilder.get(entryPointURL));
    }


    /**
     * <p>Obtains a specific resource by going directly to its source.</p>
     *
     * @param classToReturn  the class that the resource should be returned as
     * @param requestBuilder request object
     * @return resource with same type specified in the resource class.
     */
    @SuppressWarnings("unchecked")
    public <T> T processRequest(Class<T> classToReturn, RequestBuilder requestBuilder, TypeInfo typeInfo) {

        if(classToReturn == null){
            throw new IllegalArgumentException("classToReturn can not be null");
        }

        if(requestBuilder == null){
            throw new IllegalArgumentException("requestBuilder can not be null");
        }


        requestInterceptors.intercept(requestBuilder);

        Request request = requestBuilder.build();


        return processResponse(
            classToReturn,
            clientSelectionStrategy.chooseClient(request).execute(request),
            typeInfo
        );
    }

    public <T> T processResponse(
        Class<T> classToReturn,
        Response response,
        TypeInfo typeInfo
    ) {

        responseInterceptors.intercept(response);

        //Special case, if what they want is the Response in a raw format...well they can have it!
        if (Response.class.isAssignableFrom(classToReturn)) {
            return classToReturn.cast(response);
        }

        //Another special case, if what they want is a string we give them response body
        //before we process it
        //TODO: remove this in v2...if you want the body just get the response
        if (String.class.isAssignableFrom(classToReturn)) {
            return classToReturn.cast(response.getBody());
        }

        if(HyperResource.class.isAssignableFrom(classToReturn)){
            return classToReturn.cast(
                new ResponseToHyperResourcePipeline(
                    responseToResourcePipelineSteps,
                    this,
                    contentRegistry,
                    errorHandler,
                    (Class<? extends HyperResource>)classToReturn,
                    typeInfo
                ).run(response)
            );
        }

        throw new HyperfitException(
            "Return type of " + classToReturn + " is not supported"
        );


    }

    /**
     * Creates a dynamic proxy that wraps a hyper resource.
     *
     * @param classToReturn the interface the proxy should implement
     * @param hyperResource resource to proxify
     * @return resource with same type specified in the resource class.
     */
    public <T extends HyperResource> T processResource(
        Class<T> classToReturn,
        HyperResource hyperResource,
        TypeInfo typeInfo
    ) {

        //TODO: if they just want a hyper resource, give it to them


        InvocationHandler handler = new HyperResourceInvokeHandler(
            hyperResource,
            this,
            this.resourceMethodInfoCache.get(classToReturn),
            typeInfo,
            java8DefaultMethodHandler
        );


        Object proxy = Proxy.newProxyInstance(
            classToReturn.getClassLoader(),
            interfaceSelectionStrategy.determineInterfaces(classToReturn, hyperResource),
            handler
        );


        return ReflectUtils.cast(classToReturn, proxy);
    }






    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {


        public static class PipelineBuilder<I,O> {
            final List<Pipeline.Step<I,O>> steps = new ArrayList<Pipeline.Step<I,O>>();
            private final Builder builder;

            PipelineBuilder(
                Builder builder
            ){

                this.builder = builder;
            }


            public PipelineBuilder addSteps(Pipeline.Step<I,O> ...pipelineStep) {
                this.steps.addAll(Arrays.asList(pipelineStep));
                return this;
            }

            public PipelineBuilder removeSteps(Pipeline.Step<I,O>... pipelineStep) {
                this.steps.removeAll(Arrays.asList(pipelineStep));
                return this;
            }

            public PipelineBuilder removeSteps(Class<? extends Pipeline.Step<I,O>> ... typesToRemove) {
                for(Class<? extends Pipeline.Step<I,O>> typeToRemove : typesToRemove) {
                    for (java.util.Iterator<Pipeline.Step<I,O>> i = steps.iterator(); i.hasNext(); ) {

                        Pipeline.Step element = i.next();
                        if (typeToRemove.isInstance(element)) {
                            i.remove();
                        }
                    }
                }

                return this;
            }

            public PipelineBuilder resetSteps() {
                this.steps.clear();
                return this;
            }

            public Builder done(){
                return builder;
            }
        }


        private ContentRegistry contentRegistry = new ContentRegistry();
        private ErrorHandler errorHandler;
        private ResourceMethodInfoCache resourceMethodInfoCache;
        private RequestInterceptors requestInterceptors = new RequestInterceptors();
        private ResponseInterceptors responseInterceptors = new ResponseInterceptors();
        private InterfaceSelectionStrategy interfaceSelectionStrategy = new SimpleInterfaceSelectionStrategy();
        private Map<String, HyperClient> schemeClientMap = new HashMap<String, HyperClient>();
        private final PipelineBuilder<Response, HyperResource> responseToResourcePipelineBuilder = new PipelineBuilder<Response, HyperResource>(
            this
        );

        private Java8DefaultMethodHandler java8DefaultMethodHandler = new Java8DefaultMethodHandler() {
            public Object invoke(@NonNull DefaultMethodContext context, Object[] args) {
                throw new HyperfitException("No Java8DefaultMethodHandler implementation specified.  Are you missing a call to the HyperfitProcessor builder?");
            }
        };

        public Builder addContentTypeHandler(ContentTypeHandler handler) {
            this.contentRegistry.add(handler);
            return this;
        }

        public Builder addContentTypeHandler(ContentTypeHandler handler, double q) {
            this.contentRegistry.add(handler, handler.getDefaultContentType().withQ(q));
            return this;
        }

        public Builder removeContentTypeHandler(ContentTypeHandler handler) {
            this.contentRegistry.remove(handler);
            return this;
        }

        public Builder addContentTypeHandler(ContentTypeHandler handler, ContentType...types) {
            this.contentRegistry.add(handler, types);
            return this;
        }

        public Builder removeContentType(ContentType type) {
            this.contentRegistry.remove(type);
            return this;
        }

        /**
         * A HyperClient will be registered in schemeClientMap based on the schemes it can handle, which is defined in getSchemes()
         * @param hyperClient HyperClient {@link org.hyperfit.net.HyperClient}
         * @return {@link org.hyperfit.HyperfitProcessor.Builder}
         */
        public Builder hyperClient(HyperClient hyperClient) {
            if( hyperClient == null){
                throw new IllegalArgumentException("HyperClient can not be null");
            }
            for(String scheme: hyperClient.getSchemes()){
                schemeClientMap.put(scheme, hyperClient);
            }
            return this;
        }

        /**
         *  A HyperClient will be registered in schemeClientMap based on the schemes it provided in the parameters
         *   and ignores the default schemes registered by getSchemes()
         * @param hyperClient {@link org.hyperfit.net.HyperClient}
         * @param schemes {@link java.lang.String}
         * @return {@link org.hyperfit.HyperfitProcessor.Builder}
         */
        public Builder hyperClient(HyperClient hyperClient, String... schemes){
            boolean isSchemeValid = false;
            for(String scheme: schemes){
                if(!StringUtils.isEmpty(scheme)) {
                    isSchemeValid = true;
                    break;
                }
            }
            if(!isSchemeValid) {
                throw new IllegalArgumentException("HyperClient has to have schemes defined");
            }

            for(String scheme: schemes){
                schemeClientMap.put(scheme, hyperClient);
            }
            return this;
        }

        public Builder errorHandler(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public Builder resourceMethodInfoCache(ResourceMethodInfoCache resourceMethodInfoCache) {
            this.resourceMethodInfoCache = resourceMethodInfoCache;
            return this;
        }

        public Builder addRequestInterceptor(RequestInterceptor requestInterceptor) {
            this.requestInterceptors.add(requestInterceptor);
            return this;
        }

        public Builder removeRequestInterceptors(RequestInterceptor... requestInterceptor) {
            this.requestInterceptors.remove(requestInterceptor);
            return this;
        }

        public Builder removeRequestInterceptors(Class<? extends RequestInterceptor> typeToRemove) {
            this.requestInterceptors.remove(typeToRemove);
            return this;
        }

        public Builder clearInterceptors() {
            this.requestInterceptors.clear();
            return this;
        }

        public Builder clearResponseInterceptors() {
            this.responseInterceptors.clear();
            return this;
        }

        public Builder interfaceSelectionStrategy(InterfaceSelectionStrategy selectionStrategy) {
            this.interfaceSelectionStrategy = selectionStrategy;
            return this;
        }

        public Builder addResponseInterceptor(ResponseInterceptor responseInterceptor) {
            this.responseInterceptors.add(responseInterceptor);
            return this;
        }

        public Builder removeResponseInterceptor(Class<? extends ResponseInterceptor> typeToRemove) {
            this.responseInterceptors.remove(typeToRemove);
            return this;
        }

        public Builder defaultMethodInvoker(Java8DefaultMethodHandler methodInvoker) {
            if( methodInvoker == null){
                throw new IllegalArgumentException("methodInvoker can not be null");
            }

            this.java8DefaultMethodHandler = methodInvoker;

            return this;
        }


        public PipelineBuilder<Response, HyperResource> responseToResourcePipeline(){
            return responseToResourcePipelineBuilder;
        }


        public HyperfitProcessor build() {
            return new HyperfitProcessor(this);
        }
    }


    //TODO: make public when builder can take a strategy
    interface HyperClientSelectionStrategy {

        HyperClient chooseClient(
            Request request
        );

    }

}



