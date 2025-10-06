package io.github.vivianagh.jobberwocky.infrastructure.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class XmlSkillsParser {

    private final DocumentBuilderFactory factory;

    public XmlSkillsParser() {
        this.factory = DocumentBuilderFactory.newInstance();
        // Disable external entity processing for security
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (Exception e) {
            log.warn("Could not set XML security features", e);
        }
    }

    /**
     * Parse skills from XML string
     *
     * @param xmlString XML string like "<skills><skill>AWS</skill></skills>"
     * @return Set of skill names, empty if parsing fails
     */
    public Set<String> parseSkills(String xmlString) {
        //null or blanck input
        if (xmlString == null || xmlString.isBlank()) {
            log.debug("XML string is null or blank");
            return new HashSet<>();
        }
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(xmlString));
            Document document = builder.parse(inputSource);

            NodeList skillNodes = document.getElementsByTagName("skill");

            Set<String> skills = new HashSet<>();
            for (int i = 0; i < skillNodes.getLength(); i++) {
                String skillText = skillNodes.item(i).getTextContent();

                if (skillText != null && !skillText.isBlank()) {
                    String trimmedSkill = skillText.trim();

                    // Only add non-empty skills
                    if (!trimmedSkill.isEmpty()) {
                        skills.add(trimmedSkill);
                    }
                }
            }
            log.debug("Parsed {} skills from XML", skills.size());
            return skills;
        } catch (Exception e) {
            log.warn("Failed to parse XML skills: {}. Input: {}", e.getMessage(), xmlString);
            // Return empty set instead of throwing - fail-safe behavior
            return new HashSet<>();
        }

    }

}
