/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.jsfunit.example.hellojsf;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;

/**
 * Deployments
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class Deployments {
    // property surefire sys prop setting
    public static final boolean IS_JETTY = (System.getProperty("jetty-embedded") != null);
    public static final boolean IS_TOMCAT = (System.getProperty("tomcat") != null);
    public static final boolean IS_JBOSS_51 = (System.getProperty("jbossas-remote-51") != null);

    public static final boolean IS_JSF_1_2 = IS_JBOSS_51;

    public static WebArchive createDeployment() {
        WebArchive war = createBaseDeployment();
        war.setWebXML(new StringAsset(createWebXML().exportAsString()));
        return war;
    }

    private static WebArchive createBaseDeployment() {
        WebArchive war = ShrinkWrap
                .create(WebArchive.class)
                .addPackage(Package.getPackage("org.jboss.jsfunit.example.hellojsf"))
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/faces-config.xml"), "faces-config.xml")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/local-module-faces-config.xml"),
                        "local-module-faces-config.xml")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/classes/users.properties"), "classes/users.properties")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/classes/roles.properties"), "classes/roles.properties");

        war.addAsWebInfResource(new File("src/main/webapp/WEB-INF/jboss-web.xml"), "jboss-web.xml");

        if (IS_JSF_1_2) {
            war.addAsWebResource(new File("src/main/webapp", "index.jsp"))
                    .addAsWebResource(new File("src/main/webapp", "finalgreeting.jsp"))
                    .addAsWebResource(new File("src/main/webapp", "secured-page.jsp"))
                    .addAsWebResource(new File("src/main/webapp", "NestedNamingContainers.jsp"))
                    .addAsWebResource(new File("src/main/webapp", "indexWithExtraComponents.jsp"))
                    .addAsWebResource(new File("src/main/webapp", "marathons.jsp"))
                    .addAsWebResource(new File("src/main/webapp", "marathons_datatable.jsp"));
        } else {
            war.addAsWebResource(new File("src/main/webapp", "index.xhtml"))
                    .addAsWebResource(new File("src/main/webapp", "finalgreeting.xhtml"))
                    .addAsWebResource(new File("src/main/webapp", "secured-page.xhtml"))
                    .addAsWebResource(new File("src/main/webapp", "NestedNamingContainers.xhtml"))
                    .addAsWebResource(new File("src/main/webapp", "indexWithExtraComponents.xhtml"))
                    .addAsWebResource(new File("src/main/webapp", "marathons.xhtml"))
                    .addAsWebResource(new File("src/main/webapp", "marathons_datatable.xhtml"));
        }
        appendForEmbedded(war);
        return war;
    }

    private static void appendForEmbedded(WebArchive war) {
        if (IS_JETTY || IS_TOMCAT) {
            war.addAsLibraries(DependencyResolvers.use(MavenDependencyResolver.class)
                    .loadMetadataFromPom("src/test/resources-tomcat/pom.xml")
                    .artifacts("javax.annotation:jsr250-api:1.0", "javax.servlet:jstl:1.2").resolveAsFiles());

            // "com.sun.faces:jsf-api:2.0.4-b03", "com.sun.faces:jsf-impl:2.0.4-b03",
        }
        if (IS_JETTY) {
            war.addAsLibraries(DependencyResolvers.use(MavenDependencyResolver.class)
                    .artifacts("org.glassfish.web:el-impl:2.2").resolveAsFiles());
        }
    }

    public static WebAppDescriptor createCDIWebXML() {
        WebAppDescriptor desc = Descriptors.create(WebAppDescriptor.class);

        /*
         * if (IS_JETTY || IS_TOMCAT) { if (IS_TOMCAT) // jetty is added by default. It finds weld-servlet on appCl and insists
         * on loading it. { desc.createListener("org.jboss.weld.environment.servlet.Listener"); }
         *
         * // Node rootNode = ((Node) desc).getRootNode();
         *
         * // rootNode.getOrCreate("/web-app/resource-env-ref").createChild("resource-env-ref-name").text("BeanManager") //
         * .getParent().createChild("resource-env-ref-type").text("javax.enterprise.inject.spi.BeanManager"); }
         */
        appendBaseWebXML(desc);
        return desc;
    }

    public static WebAppDescriptor createWebXML() {
        WebAppDescriptor desc = Descriptors.create(WebAppDescriptor.class);

        appendBaseWebXML(desc);

        return desc;
    }

    private static void appendBaseWebXML(WebAppDescriptor desc) {
        desc.displayName("JSFUnit Arquillian TestCase")
                .createContextParam()
                    .paramName("javax.faces.CONFIG_FILES")
                    .paramValue("/WEB-INF/local-module-faces-config.xml").up()
                 .createWelcomeFileList()
                    .welcomeFile("index.xhtml").up()
                .createServlet()
                    .servletClass("javax.faces.webapp.FacesServlet")
                    .servletName("FacesServlet")
                    .loadOnStartup(1).up()
                .createServletMapping()
                    .servletName("FacesServlet")
                    .urlPattern("*.faces").up()
                /*.createFilter().filterName("JSFUnitCleanupTestTreadFilter").filterClass("org.jboss.jsfunit.arquillian.container.JSFUnitCleanupTestTreadFilter").up()
                .createFilter().filterName("JSFUnitFilter").filterClass("org.jboss.jsfunit.framework.JSFUnitFilter").up()
                .createFilterMapping().filterName("JSFUnitCleanupTestTreadFilter").urlPattern("/*").up().createFilterMapping()
                .filterName("JSFUnitFilter").urlPattern("/*").up()*/
                    ;


        /*if (!(IS_JETTY || IS_TOMCAT)) {
            // SHRINKDESC-48 desc.securityConstraint("Basic Authentication for the Admin")
            desc.securityConstraint().webResourceCollection("Authenticated").urlPatterns("/secured-page.faces")
                    .authConstraint("hellotestadmin").loginConfig(AuthMethodType.BASIC, "Authenticated")
                    .securityRole("hellotestadmin");
        }
        if (IS_JETTY) {
            desc.listener("org.jboss.weld.environment.servlet.Listener");
            desc.listener("com.sun.faces.config.ConfigureListener");
        }
        if (IS_TOMCAT || IS_JBOSS_51) {
            desc.version("2.5");
        }*/
    }
}
