package se.sawano.spring.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebAppConfiguration
@IntegrationTest
//@IntegrationTest({"server.port:0", "management.port:0"})
public class ApplicationIntegrationTests {

    @Value("${local.server.port}")
    int port;
    @Value("${local.management.port}")
    int managementPort;
    @Autowired
    ManagementServerProperties managementServerProperties;

    @Test
    public void should_require_password_for_index_page() {
        final URI uri = indexUri();
        final ResponseEntity<String> entity = new TestRestTemplate().getForEntity(uri, String.class);

        assertThat(entity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void should_return_index_page_when_password_is_provided() {
        final URI uri = indexUri();
        final ResponseEntity<String> entity = new TestRestTemplate("user", "my-password").getForEntity(uri, String.class);

        assertThat(entity.getStatusCode(), is(HttpStatus.OK));
        assertThat(entity.getBody(), is("hello"));
    }

    @Test
    public void should_not_require_login_on_management_pages() {
        final URI uri = envEndpointUri();
        final ResponseEntity<String> entity = new TestRestTemplate().getForEntity(uri, String.class);

        assertThat(entity.getStatusCode(), is(HttpStatus.OK));
        final String body = entity.getBody();
        assertThat(body, containsString("\"profiles\":"));
    }

    private URI indexUri() {return builder().port(port).build().toUri();}

    private URI envEndpointUri() {
        final String contextPath = managementServerProperties.getContextPath();
        return builder().port(managementPort)
                        .path(contextPath)
                        .pathSegment("env")
                        .build()
                        .toUri();
    }

    private UriComponentsBuilder builder() {
        return UriComponentsBuilder.fromHttpUrl("http://localhost");
    }

}
