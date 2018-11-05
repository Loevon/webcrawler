import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.hogetvedt.crawler.models.WebLink;
import com.hogetvedt.crawler.services.CrawlerService;
import com.hogetvedt.crawler.spider.Spider;
import com.hogetvedt.crawler.spider.SpiderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class CrawlerServiceTest {
    @InjectMocks
    private CrawlerService crawlerService;

    //  private final String MOCK_STATUS_URL = "https://httpbin.org/status/";
    private final String MOCK_ENDPOINT = "http://localhost:8080/test";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    @Before
    public void setup() {

    }

    @Test
    public void testInitialEndpoint() {
        createUrlEndpoint("{ \"links\": [ \"https://httpbin.org/status/200\"] }");
        crawlerService.setEntryEndpoint(MOCK_ENDPOINT);

        crawlerService.start();

        Spider spider = crawlerService.getSpider();

        Assert.assertEquals(1, spider.getHttpRequests().longValue());
        Assert.assertEquals(1, spider.getSuccessfulRequests().longValue());
        Assert.assertEquals(0, spider.getFailedRequests().longValue());
    }

    @Test
    public void resetSpider() {
        createUrlEndpoint("{ \"links\": [ \"https://httpbin.org/status/200\"] }");
        crawlerService.setEntryEndpoint(MOCK_ENDPOINT);

        crawlerService.start();

        Spider spider = crawlerService.getSpider();

        Assert.assertEquals(1, spider.getHttpRequests().longValue());
        Assert.assertEquals(1, spider.getSuccessfulRequests().longValue());
        Assert.assertEquals(0, spider.getFailedRequests().longValue());

        crawlerService.resetSpider();

        spider = crawlerService.getSpider();

        Assert.assertEquals(0, spider.getHttpRequests().longValue());
        Assert.assertEquals(0, spider.getSuccessfulRequests().longValue());
        Assert.assertEquals(0, spider.getFailedRequests().longValue());

    }

    // test cycles
    @Test
    public void testCrawlForCycles() {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/status/200\", \"https://httpbin.org/status/200\"] }";
        createUrlEndpoint(jsonString);

        crawlerService.setEntryEndpoint(MOCK_ENDPOINT);

        crawlerService.start();

        Spider spider = crawlerService.getSpider();

        Assert.assertEquals(1, spider.getHttpRequests().longValue());
        Assert.assertEquals(1, spider.getSuccessfulRequests().longValue());
        Assert.assertEquals(0, spider.getFailedRequests().longValue());
    }

    @Test
    public void crawlLinkWithNestedLinks()  {
        String jsonString = "{ \"links\": [\"https://httpbin.org/links/2\" ] }";
        createUrlEndpoint(jsonString);

        crawlerService.setEntryEndpoint(MOCK_ENDPOINT);
        crawlerService.start();

        Spider spider = crawlerService.getSpider();

        Assert.assertEquals(3, spider.getHttpRequests().longValue());
        Assert.assertEquals(3, spider.getSuccessfulRequests().longValue());
        Assert.assertEquals(0, spider.getFailedRequests().longValue());
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
