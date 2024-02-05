/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.graphql.data.federation;

import java.util.Map;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DelegatingDataFetchingEnvironment;

import org.springframework.core.ResolvableType;
import org.springframework.graphql.data.GraphQlArgumentBinder;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.support.ArgumentMethodArgumentResolver;
import org.springframework.validation.BindException;

/**
 * Resolver for a method parameter annotated with {@link Argument @Argument}.
 * On {@code @EntityMapping} methods, the raw argument value is obtained from
 * the "representation" input map for the entity with entries that identify
 * the entity uniquely.
 *
 * @author Rossen Stoyanchev
 * @since 1.3
 */
final class EntityArgumentMethodArgumentResolver extends ArgumentMethodArgumentResolver {


	EntityArgumentMethodArgumentResolver(GraphQlArgumentBinder argumentBinder) {
		super(argumentBinder);
	}


	@Override
	protected Object doBind(
			DataFetchingEnvironment environment, String name, ResolvableType targetType) throws BindException {

		if (environment instanceof EntityDataFetchingEnvironment entityEnv) {
			Map<String, Object> entityMap = entityEnv.getRepresentation();
			Object rawValue = entityMap.get(name);
			boolean isOmitted = !entityMap.containsKey(name);
			return getArgumentBinder().bind(name, rawValue, isOmitted, targetType);
		}

		throw new IllegalStateException("Expected decorated DataFetchingEnvironment");
	}

	/**
	 * Wrap the environment in order to also expose the entity representation map.
	 */
	public static DataFetchingEnvironment wrap(DataFetchingEnvironment env, Map<String, Object> representation) {
		return new EntityDataFetchingEnvironment(env, representation);
	}


	private static class EntityDataFetchingEnvironment extends DelegatingDataFetchingEnvironment {

		private final Map<String, Object> representation;

		EntityDataFetchingEnvironment(DataFetchingEnvironment env, Map<String, Object> representation) {
			super(env);
			this.representation = representation;
		}

		public Map<String, Object> getRepresentation() {
			return this.representation;
		}
	}

}
