package eu.etaxonomy.cdm.common;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class UriUtilsTest {

    private static final Logger logger = Logger.getLogger(UriUtilsTest.class);

//********************* TESTS **********************************************/

    @Test
    public void testCreateUri() {
        try {
            URL baseUrl = new URL("http://www.abc.de");
            String subPath = "fgh";
            String fragment = "frag";
            URI uri = UriUtils.createUri(baseUrl, subPath, null, fragment);
            Assert.assertEquals("http://www.abc.de/fgh#frag", uri.toString());
            List<NameValuePair> qparams = new ArrayList<>(0);
            NameValuePair pair1 = new BasicNameValuePair("param1","value1");
            qparams.add(pair1);
            uri = UriUtils.createUri(baseUrl, subPath, qparams, fragment);
            Assert.assertEquals("http://www.abc.de/fgh?param1=value1#frag", uri.toString());

        } catch (MalformedURLException e) {
            Assert.fail(e.getMessage());
        } catch (URISyntaxException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testGetResourceLength() throws ClientProtocolException, IOException, HttpException{
        if(UriUtils.isInternetAvailable(null)){
            URI uri = URI.create("http://dev.e-taxonomy.eu/trac_htdocs/logo_edit.png");
            Assert.assertEquals(9143, UriUtils.getResourceLength(uri, null));
        } else {
            logger.warn("Test: testGetResourceLength() skipped, since internet is not available");
        }
    }

    @Test
    public void testGetResourceLengthNull2() throws ClientProtocolException, IOException, HttpException{
        if(UriUtils.isInternetAvailable(null)){
			try {
				@SuppressWarnings("unused")
                URI uri = new URI("http:/www.abc.de:8080/xyz");
				System.out.println("  sdf");
//				Assert.assertEquals(9143, UriUtils.getResourceLength(uri, null));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        } else {
            logger.warn("Test: testGetResourceLength() skipped, since internet is not available");
        }
    }

    @Test
    public void testGetResourceLengthMissingProtocol() throws ClientProtocolException, HttpException{
    	URI uri;
		try {
			uri = URI.create("www.abc.de");
			UriUtils.getResourceLength(uri, null);
			Assert.fail("getResourceLength works only on absolute URIs providing a protocol/scheme");
		} catch (Exception e) {
			Assert.assertEquals(IOException.class, e.getClass());
			Assert.assertNotNull(e.getMessage().equals(UriUtils.URI_IS_NOT_ABSOLUTE));
		}
    }

    @Test
    public void testGetResourceLengthUnknownProtocol() throws ClientProtocolException, HttpException{
    	URI uri;
		try {
			uri = URI.create("xxx://www.abc.de");
			UriUtils.getResourceLength(uri, null);
			Assert.fail("getResourceLength works only on absolute URIs with supported protocols 'http(s):' and 'file:'");
		} catch (Exception e) {
			Assert.assertEquals(RuntimeException.class, e.getClass());
			Assert.assertNotNull(e.getMessage().startsWith("Protocol not handled yet"));
		}
    }

    @Test
    public void testIsInternetAvailable() {
        URI firstUri = URI.create("http://www.gmx.de/");
        boolean isAvailable = UriUtils.isInternetAvailable(firstUri);
        if (isAvailable == false){
            logger.warn("Internet is not available!");
        }
    }

    @Test
    public void testIsRootServerAvailable() {
        boolean isAvailable = UriUtils.isRootServerAvailable("www.gmx.de");
        if (isAvailable == false){
            logger.warn("RootServer is not available!");
        }
    }

}
