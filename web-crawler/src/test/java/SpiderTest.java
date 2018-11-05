import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.hogetvedt.crawler.models.WebLink;
import com.hogetvedt.crawler.spider.Spider;
import com.hogetvedt.crawler.spider.SpiderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class SpiderTest {
    private final String MOCK_STATUS_URL = "https://httpbin.org/status/";
    private Spider spider;

    @Rule
    public ExpectedException failure = ExpectedException.none();

    @Before
    public void setup() {
        this.spider = new Spider();
    }

    // test null endpoint
    @Test
    public void crawlNullEndpoint() throws SpiderException {
        failure.expect(SpiderException.class);
        failure.expectMessage("Invalid URL");

        WebLink link = new WebLink(null);

        spider.crawl(link);
    }

    // test empty endpoint
    @Test
    public void crawlEmptyEndpoint() throws SpiderException {
        failure.expect(SpiderException.class);
        failure.expectMessage("Invalid URL");

        WebLink link = new WebLink("");

        spider.crawl(link);
    }

    // crawl good status
    @Test
    public void crawlStatusCode200() {
        testGoodRequest("200");
    }

    @Test
    public void crawlStatusCode201() {
        testGoodRequest("201");
    }

    // crawl bad status
    @Test
    public void crawlStatusCode400() {
        testBadRequest("400");
    }

    @Test
    public void crawlStatusCode401() {
        testBadRequest("401");
    }

    @Test
    public void crawlStatusCode403() {
        testBadRequest("403");
    }

    @Test
    public void crawlStatusCode404() {
        testBadRequest("404");
    }

    @Test
    public void crawlStatusCode500() {
        testBadRequest("500");
    }

    @Test
    public void crawlStatusCode501() {
        testBadRequest("501");
    }

    @Test
    public void crawlStatusCode502() {
        testBadRequest("502");
    }

    @Test
    public void crawlStatusCode503() {
        testBadRequest("503");
    }

    @Test
    public void crawlStatusCode504() {
        testBadRequest("504");
    }



    private void testGoodRequest(String statusCode) {
        WebLink link = new WebLink(MOCK_STATUS_URL + statusCode);

        try {
            spider.crawl(link);
        } catch (SpiderException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(1, spider.getHttpRequests().longValue());
        Assert.assertEquals(1, spider.getSuccessfulRequests().longValue());
        Assert.assertEquals(0, spider.getFailedRequests().longValue());
    }

    private void testBadRequest(String statusCode) {
        WebLink link = new WebLink(MOCK_STATUS_URL + statusCode);

        try {
            spider.crawl(link);
        } catch (SpiderException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(1, spider.getHttpRequests().longValue());
        Assert.assertEquals(0, spider.getSuccessfulRequests().longValue());
        Assert.assertEquals(1, spider.getFailedRequests().longValue());
    }



    private void createUrlEndpoint(String jsonBody) {
        stubFor(get(urlEqualTo("/test"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-type", "application/json")
                                .withBody(jsonBody)
                )
        );
    }
}
