package io.github.vivianagh.jobberwocky.infrastructure.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * The external API returns skills as XML strings:
 * "<skills><skill>AWS</skill><skill>Docker</skill></skills>"
 * This parser extracts them into a Set<String>
 */
public class XmlSkillsParserTest {
    private XmlSkillsParser parser;

    @BeforeEach
    public void setup() {
        parser = new XmlSkillsParser();
    }

    @Test
    void shouldParseValidXml() {
        //Given
        String xml = "<skills><skill>AWS</skill><skill>Docker</skill><skill>Kubernetes</skill></skills>";

        //When
        Set<String> skills = parser.parseSkills(xml);

        //Then
        assertThat(skills).containsExactlyInAnyOrder("AWS", "Docker", "Kubernetes");
    }

    @Test
    void shouldHandleEmptyXml() {
        //Given
        String xml = "<skills></skills>";

        //When
        Set<String> skills = parser.parseSkills(xml);

        //Then
        assertThat(skills).isEmpty();
    }

    @Test
    void shouldHandleNullXml() {
        //Given
        String xml = null;

        // When
        Set<String> skills = parser.parseSkills(null);

        // Then
        assertThat(skills).isEmpty();
    }

    @Test
    void shouldHandleInvalidXmlGracefully() {
        // Given
        String invalidXml = "not xml ";

        // When
        Set<String> skills = parser.parseSkills(invalidXml);

        // Then
        assertThat(skills).isEmpty();
    }

}
