package com.github.cukespace.examples;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * @author Edward Mann <ed.mann@edmann.com>
 *         <p>
 *         Created: Mar 20, 2021
 */
@RunWith(Arquillian.class)
@RunAsClient
public class StaticApplicationIT extends AbstractIntegrationTest {

    @Drone
    WebDriver browser;

    @Test
    public void testIt() throws Exception {
        browser.navigate().to("http://localhost:8080/");
        assertThat(browser.getPageSource()).contains("Howdy!");
    }

    @Deployment
    public static WebArchive war() {
        final File[]     libs = Maven.resolver().loadPomFromFile("pom.xml")
                .importDependencies(ScopeType.TEST, ScopeType.COMPILE, ScopeType.RUNTIME).resolve().withTransitivity()
                .asFile();
        final WebArchive war  = ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebResource(new File("src/main/webapp/index.html"))
                .addPackages(true, Filters.includeAll(), "com.github.cukespace").addAsLibraries(libs);
        System.out.println(war.toString(true));
        return war;
    }
}
