package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Hashtable;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class WcampDocumentResourceFactoryTest {

    private Hashtable<String,String> browserRequestHeaders = new Hashtable<>();
    private Hashtable<String,String> apiRequestHeaders = new Hashtable<>();
    private WcampDocUploadedFromWeb uploadFromBrowser;
    private WcampDocUploadedFromAPI uploadFromAPI;
    private HttpServletRequest mockRequest;

    @Before
    public void setUp() throws Exception {

        browserRequestHeaders.put(HttpHeaders.X_REQUESTED_WITH, "MockedHttpRequest");
        browserRequestHeaders.put(HttpHeaders.USER_AGENT, "Mockito Test");
        browserRequestHeaders.put(HttpHeaders.REFERER, "http://localhost:8080/klaatu");
        browserRequestHeaders.put(HttpHeaders.PRAGMA, "no-cache");
        browserRequestHeaders.put(HttpHeaders.HOST, "localhost:8080");
        browserRequestHeaders.put(HttpHeaders.CONTENT_TYPE,
                "multipart/form-data; boundary=---------------------------15849383646719263601597316594");
        browserRequestHeaders.put(HttpHeaders.CONTENT_LENGTH, "91698");
        browserRequestHeaders.put(HttpHeaders.CONNECTION, "keep-alive");
        browserRequestHeaders.put(HttpHeaders.CACHE_CONTROL, "no-cache");
        browserRequestHeaders.put(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.5");
        browserRequestHeaders.put(HttpHeaders.ACCEPT_ENCODING, "Accept-Encoding:gzip, deflate");
        browserRequestHeaders.put(HttpHeaders.ACCEPT, "application/json, text/javascript, */*; q=0.01");
        browserRequestHeaders.put(HttpHeaders.COOKIE,
                "rack.session=102d0b1b6cc1adca0921ac65d49ff1b4cfd1b16d87905b454db9ed710919baa0");

        apiRequestHeaders.put(CustomHttpHeaders.X_TRANTOR_CLIENT_ID, "c0ffeeb4b3");
        apiRequestHeaders.put(CustomHttpHeaders.X_TRANTOR_ASSIGNED_UPLOAD_ID, "temp-identifier");
        apiRequestHeaders.put(HttpHeaders.HOST, "klendathu.example.net");
        apiRequestHeaders.put(HttpHeaders.CONTENT_TYPE, "application/pdf");
        apiRequestHeaders.put(HttpHeaders.CONTENT_LENGTH, "91698");
        apiRequestHeaders.put(HttpHeaders.CONNECTION, "keep-alive");
        apiRequestHeaders.put(HttpHeaders.ACCEPT_ENCODING, "Accept-Encoding:gzip, deflate");
        apiRequestHeaders.put(HttpHeaders.ACCEPT, "application/json");
        apiRequestHeaders.put(HttpHeaders.AUTHORIZATION, "deadbeef");

        mockRequest = mock(HttpServletRequest.class);
        uploadFromBrowser = mock(WcampDocUploadedFromWeb.class);
        uploadFromAPI = mock(WcampDocUploadedFromAPI.class);

    }

    @Test
    public void testCreateWcampDocUploadedFromWeb() throws Exception {

        when(mockRequest.getHeader(eq(CustomHttpHeaders.X_TRANTOR_UPLOAD_TYPE))).thenReturn(null);
        when(mockRequest.getHeader(eq(CustomHttpHeaders.X_TRANTOR_DOCUMENT_ID))).thenReturn(null);
        when(mockRequest.getHeader(eq(HttpHeaders.COOKIE))).thenReturn(browserRequestHeaders.get(HttpHeaders.COOKIE));

        assertThat(WcampDocumentResourceFactory.create(mockRequest), instanceOf(WcampDocUploadedFromWeb.class));

    }

    @Test
    public void testCreateWcampDocUploadedFromAPI() throws Exception {

        when(mockRequest.getHeader(eq(CustomHttpHeaders.X_TRANTOR_UPLOAD_TYPE))).thenReturn(null);

        when(mockRequest.getHeader(eq(CustomHttpHeaders.X_TRANTOR_CLIENT_ID))).
                thenReturn(apiRequestHeaders.get(CustomHttpHeaders.X_TRANTOR_CLIENT_ID));

        when(mockRequest.getHeader(eq(CustomHttpHeaders.X_TRANTOR_ASSIGNED_UPLOAD_ID))).
                thenReturn(apiRequestHeaders.get(CustomHttpHeaders.X_TRANTOR_ASSIGNED_UPLOAD_ID));

        when(mockRequest.getHeader(eq(HttpHeaders.AUTHORIZATION))).
                thenReturn(apiRequestHeaders.get(HttpHeaders.AUTHORIZATION));

        assertThat(WcampDocumentResourceFactory.create(mockRequest), instanceOf(WcampDocUploadedFromAPI.class));

    }

    @Test
    public void testCreateWcampDocVersionedFromAPI() throws Exception {

        when(mockRequest.getHeader(eq(CustomHttpHeaders.X_TRANTOR_UPLOAD_TYPE))).thenReturn("version");
        when(mockRequest.getHeader(eq(CustomHttpHeaders.X_TRANTOR_DOCUMENT_ID))).thenReturn("existing-doc-docId");

        when(mockRequest.getHeader(eq(CustomHttpHeaders.X_TRANTOR_CLIENT_ID))).
                thenReturn(apiRequestHeaders.get(CustomHttpHeaders.X_TRANTOR_CLIENT_ID));

        when(mockRequest.getHeader(eq(CustomHttpHeaders.X_TRANTOR_ASSIGNED_UPLOAD_ID))).
                thenReturn(null);

        when(mockRequest.getHeader(eq(HttpHeaders.AUTHORIZATION))).
                thenReturn(apiRequestHeaders.get(HttpHeaders.AUTHORIZATION));

        assertThat(WcampDocumentResourceFactory.create(mockRequest), instanceOf(WcampDocUploadedFromAPI.class));
    }
}
