/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.endpoint.web;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.ExposeExcludePropertyEndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointsSupplier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvokerAdvisor;
import org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointDiscoverer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for web {@link Endpoint} support.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 2.0.0
 */
@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter(EndpointAutoConfiguration.class)
@EnableConfigurationProperties(WebEndpointProperties.class)
@ConditionalOnProperty(name = "management.endpoints.web.enabled", matchIfMissing = true)
public class WebEndpointAutoConfiguration {

	private static final List<String> MEDIA_TYPES = Arrays
			.asList(ActuatorMediaType.V2_JSON, "application/json");

	private final ApplicationContext applicationContext;

	private final WebEndpointProperties properties;

	public WebEndpointAutoConfiguration(ApplicationContext applicationContext,
			WebEndpointProperties properties) {
		this.applicationContext = applicationContext;
		this.properties = properties;
	}

	@Bean
	@ConditionalOnMissingBean
	public PathMapper webEndpointPathMapper() {
		return new MappingWebEndpointPathMapper(this.properties.getPathMapping());
	}

	@Bean
	@ConditionalOnMissingBean
	public EndpointMediaTypes endpointMediaTypes() {
		return new EndpointMediaTypes(MEDIA_TYPES, MEDIA_TYPES);
	}

	@Bean
	@ConditionalOnMissingBean(WebEndpointsSupplier.class)
	public WebEndpointDiscoverer webEndpointDiscoverer(
			ParameterValueMapper parameterValueMapper,
			EndpointMediaTypes endpointMediaTypes, PathMapper webEndpointPathMapper,
			ObjectProvider<Collection<OperationInvokerAdvisor>> invokerAdvisors,
			ObjectProvider<Collection<EndpointFilter<ExposableWebEndpoint>>> filters) {
		return new WebEndpointDiscoverer(this.applicationContext, parameterValueMapper,
				endpointMediaTypes, webEndpointPathMapper,
				invokerAdvisors.getIfAvailable(Collections::emptyList),
				filters.getIfAvailable(Collections::emptyList));
	}

	@Bean
	@ConditionalOnMissingBean
	public PathMappedEndpoints pathMappedEndpoints(
			Collection<EndpointsSupplier<?>> endpointSuppliers,
			WebEndpointProperties webEndpointProperties) {
		return new PathMappedEndpoints(webEndpointProperties.getBasePath(),
				endpointSuppliers);
	}

	@Bean
	public ExposeExcludePropertyEndpointFilter<ExposableWebEndpoint> webIncludeExcludePropertyEndpointFilter() {
		Set<String> expose = this.properties.getExpose();
		Set<String> exclude = this.properties.getExclude();
		return new ExposeExcludePropertyEndpointFilter<>(ExposableWebEndpoint.class,
				expose, exclude, "info", "health");
	}

}