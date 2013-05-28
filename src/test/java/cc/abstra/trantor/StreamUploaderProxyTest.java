/*
 * Copyright abstra.cc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.abstra.trantor;

import cc.abstra.trantor.wcamp.CustomHttpHeaders;
import cc.abstra.trantor.wcamp.WcampPendingDoc;
import cc.abstra.trantor.wcamp.WcampTempDoc;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({URL.class, StreamUploaderProxy.class})
// Adding the class under test to the @PrepareForTest ensures that the StreamUploaderProxy class
// is loaded with the modified URL.class loaded provided by PowerMockito.
public class StreamUploaderProxyTest {

    @Mock private String testUrl = "http://example.com:8080/foo/bar";
    private Hashtable<String,String> requestHeaders = new Hashtable<>();
    private String clientRequestStr;
    private Map<String,List<String>> remoteResponseHeaders = new HashMap<>();
    private String remoteResponseStr;

    private URL targetUrl;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpURLConnection urlConnection;
    private OutputStream targetReqOutputStream;
    private MockServletInputStream clientRequestInputStream;
    private ServletOutputStream responseOutputStream;
    private WcampPendingDoc pendingDoc;
    private WcampTempDoc tempDoc;
    private AsyncContext asyncCtx;

    @InjectMocks private StreamUploaderProxy uploadProxy = new StreamUploaderProxy();

    @Before
    public void setUp() throws Exception {

        requestHeaders.put(HttpHeaders.X_REQUESTED_WITH, "MockedHttpRequest");
        requestHeaders.put(HttpHeaders.USER_AGENT, "Mockito Test");
        requestHeaders.put(HttpHeaders.REFERER, "http://localhost:8080/klaatu");
        requestHeaders.put(HttpHeaders.PRAGMA, "no-cache");
        requestHeaders.put(HttpHeaders.HOST, "localhost:8080");
        requestHeaders.put(HttpHeaders.CONTENT_TYPE,
                "multipart/form-data; boundary=---------------------------15849383646719263601597316594");
        requestHeaders.put(HttpHeaders.CONTENT_LENGTH, "91698");
        requestHeaders.put(HttpHeaders.CONNECTION, "keep-alive");
        requestHeaders.put(HttpHeaders.CACHE_CONTROL, "no-cache");
        requestHeaders.put(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.5");
        requestHeaders.put(HttpHeaders.ACCEPT_ENCODING, "Accept-Encoding:gzip, deflate");
        requestHeaders.put(HttpHeaders.ACCEPT, "application/json, text/javascript, */*; q=0.01");
        requestHeaders.put(HttpHeaders.COOKIE,
                "rack.session=102d0b1b6cc1adca0921ac65d49ff1b4cfd1b16d87905b454db9ed710919baa0");

        clientRequestStr = "Dummy client request body";

        remoteResponseHeaders.put(HttpHeaders.X_CONTENT_TYPE_OPTIONS, Arrays.asList("nosniff"));
        remoteResponseHeaders.put(HttpHeaders.VARY, Arrays.asList("Accept-Encoding"));
        remoteResponseHeaders.put(HttpHeaders.SERVER, Arrays.asList("WEBrick/1.3.1 (Ruby/1.9.3/2013-01-04)"));
        remoteResponseHeaders.put(HttpHeaders.DATE, Arrays.asList("Mon, 06 May 2013 10:47:07 GMT"));
        remoteResponseHeaders.put(HttpHeaders.CONTENT_TYPE, Arrays.asList("application/json;charset=utf-8"));
        remoteResponseHeaders.put(HttpHeaders.CONTENT_LENGTH, Arrays.asList("205"));
        remoteResponseHeaders.put(HttpHeaders.CONTENT_ENCODING, Arrays.asList("gzip"));

        remoteResponseStr = "Dummy remote response body";

        targetUrl = PowerMockito.mock(URL.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        urlConnection = mock(HttpURLConnection.class);
        targetReqOutputStream = mock(OutputStream.class);

        clientRequestInputStream = new MockServletInputStream(clientRequestStr);
        responseOutputStream = mock(ServletOutputStream.class);

        asyncCtx = mock(AsyncContext.class);
        pendingDoc = mock(WcampPendingDoc.class);
        tempDoc = mock(WcampTempDoc.class);
    }

    /* Mock a static method (for future reference):
      @PrepareForTest({..., ClassWith.class, ...})
      mockStatic(Static.class);
      ...
      PowerMockito.verifyStatic(); // no need to mock/verify the executor service
      ClassWith.static_method(... matchers ...);
     */

    // WARNING: PowerMock does not support Java 7exception multicatching
    // https://code.google.com/p/powermock/issues/detail?id=427

    @After
    public void tearDown() throws Exception {
        uploadProxy.destroy();
    }

    @Test
    public void testDoSuccessfulPostWithKnownContentLengthViaWebClient() throws Exception {

        whenNew(URL.class).withArguments(testUrl).thenReturn(targetUrl);
        whenNew(WcampPendingDoc.class).withArguments(anyString()).thenReturn(pendingDoc);

        when(request.getHeader(eq(HttpHeaders.COOKIE))).thenReturn(requestHeaders.get(HttpHeaders.COOKIE));
        when(targetUrl.openConnection()).thenReturn(urlConnection);

        // Ignores CustomHttpHeaders.CONTENT_LENGTH, using request.geyInputStream() byte[] length
        // See: http://docs.oracle.com/javaee/6/api/javax/servlet/ServletRequest.html#getContentLength()
        when(request.getContentLength()).thenReturn(clientRequestStr.getBytes("UTF-8").length);

        when(request.getHeaderNames()).thenReturn(requestHeaders.keys());
        when(request.getHeaders(anyString())).thenAnswer(new Answer<Enumeration>() {
            @Override
            public Enumeration answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                // TODO: Does not test multivalued headers
                return new Vector<>(Arrays.asList(requestHeaders.get((String) args[0]))).elements();
            }
        });
        when(request.getInputStream()).thenReturn(clientRequestInputStream);
        when(urlConnection.getOutputStream()).thenReturn(targetReqOutputStream);

        when(urlConnection.getInputStream()).thenReturn(new ByteArrayInputStream(remoteResponseStr.getBytes("UTF-8")));
        when(urlConnection.getHeaderField(0)).thenReturn("HTTP/1.1 201 Created");
        when(urlConnection.getHeaderFields()).thenReturn(remoteResponseHeaders);
        when(request.getHeader(HttpHeaders.COOKIE)).thenReturn(requestHeaders.get(HttpHeaders.COOKIE));
        when(urlConnection.getHeaderField(CustomHttpHeaders.X_TRANTOR_UPLOADED_FILES_INFO)).thenReturn(
                "c0ffeeb4b3/example 1 title; f00b4rb4z/title_example_2");
        when(response.getOutputStream()).thenReturn(responseOutputStream);

        when(request.startAsync()).thenReturn(asyncCtx);

        uploadProxy.doPost(request, response);

        verifyNew(URL.class).withArguments(testUrl);  // injected dependency
        verifyNew(WcampPendingDoc.class).withArguments(requestHeaders.get(HttpHeaders.COOKIE));

        // This won't work:
        // See: https://code.google.com/p/powermock/issues/detail?id=297
        //verify(targetUrl).openConnection();
        verify(urlConnection).setDoOutput(true);  //  POST request
        verify(pendingDoc, times(2)).getAuthToken();
        verify(urlConnection).setReadTimeout(anyInt());  // just in case someone deletes it ;-)

        // verify proxyed request headers
        verify(urlConnection, times(1)).setRequestProperty(eq(HttpHeaders.X_REQUESTED_WITH), eq("MockedHttpRequest"));
        verify(urlConnection, never()).setRequestProperty(eq(HttpHeaders.CONNECTION), eq("keep-alive"));  //"hop-by-hop" header
        verify(urlConnection, times(12)).setRequestProperty(anyString(),anyString());

        // verify setStreamingMode is set at proxyed request
        verify(request).getContentLength();
        verify(urlConnection).setFixedLengthStreamingMode(eq(clientRequestStr.length()));

        // verify clientRequestIS --> targetRequestOS
        verify(request).getInputStream();
        verify(urlConnection).getOutputStream();

        byte[] buffer = new byte[1024];
        int i=0;
        for (byte b : clientRequestStr.getBytes()) {
            buffer[i++] = b;
        }
        verify(targetReqOutputStream).write(aryEq(buffer), eq(0), eq(clientRequestStr.length()));

        // verify proxyed response headers
        verify(response).setStatus(eq(HttpServletResponse.SC_CREATED));
        verify(response, times(1)).setContentLength(eq(remoteResponseStr.length()));
        verify(response, never()).setIntHeader(anyString(), anyInt());
        verify(response, times(6)).setHeader(anyString(), anyString());

        // verify res.getOutputStream().write() <-- targetResponseIS (body & headers)
        verify(urlConnection).getInputStream();
        verify(response).getOutputStream();
        verify(pendingDoc).add(eq("c0ffeeb4b3/example 1 title; f00b4rb4z/title_example_2"));

        verify(responseOutputStream).write(aryEq(remoteResponseStr.getBytes()));
    }

    @Test
    @Ignore("TODO")
    public void testDoSuccessfulPostWithUnknownContentLength() throws Exception {
    }

    @Test
    @Ignore("TODO")
    public void testDoPostWithErrors() throws Exception {
    }

    @Test
    @Ignore("TODO")
    public void testDoSuccessfulPostFromAPI() throws Exception {
        //test WcampDocumentResource
    }

    @Test
    @Ignore("TODO")
    public void testDoPostFromAPIWithErrors() throws Exception {
        //test WcampDocumentResource
    }

}
