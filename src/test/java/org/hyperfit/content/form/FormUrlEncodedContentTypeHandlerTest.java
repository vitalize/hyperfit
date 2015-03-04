package org.hyperfit.content.form;

import org.hyperfit.content.ContentType;
import org.hyperfit.net.Request;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class FormUrlEncodedContentTypeHandlerTest {

    @Mock
    Request.RequestBuilder mockReqBuilder;

    FormURLEncodedContentTypeHandler handler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        handler = new FormURLEncodedContentTypeHandler();
    }

    @Test
    public void testGetDefaultHandledMediaType() {
        assertEquals(new ContentType("application", "x-www-form-urlencoded"), handler.getDefaultContentType());
    }


    static class RequestData {

        private String stringVal = "StringVal";
        private String stringWithChars = "!@#$%^&*()";
        private String stringUrl = "http://host/path?xxx=yy&bbb";

        private Double DoubleValue = 66.77d;
        private double doubleValue = 55.77d;

        private Integer IntegerValue = 66;
        private int intValue = 55;

        private Boolean BooleanValue = Boolean.FALSE;
        private boolean booleanValue = true;


    }

    @Test
    public void testEncodeRequest(){
        RequestData content = new RequestData();

        handler.encodeRequest(mockReqBuilder, content);

        verify(mockReqBuilder).setContentType(handler.getDefaultContentType().toString(false));

        verify(mockReqBuilder).setContent("stringVal=StringVal&stringWithChars=%21%40%23%24%25%5E%26*%28%29&stringUrl=http%3A%2F%2Fhost%2Fpath%3Fxxx%3Dyy%26bbb&DoubleValue=66.77&doubleValue=55.77&IntegerValue=66&intValue=55&BooleanValue=false&booleanValue=true");
    }




}
